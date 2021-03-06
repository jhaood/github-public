/*
 * Copyright 2015 Jim Alexander, Aesthetic Software, Inc. (jhaood@gmail.com)
 * Apache Version 2 license: http://www.apache.org/licenses/LICENSE-2.0
 */
package com.aestheticsw.jobkeywords.service.termextractor.impl.fivefilters;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.aestheticsw.jobkeywords.service.termextractor.domain.TermFrequency;
import com.aestheticsw.jobkeywords.service.termextractor.domain.TermFrequencyList;
import com.aestheticsw.jobkeywords.shared.config.Log;

/**
 * The FiveFiltersClient provides a single method for extracting keywords from a large piece of
 * text. The text may contain simple HTML tags which the service will remove before extracting the
 * terms.<p/>
 * 
 * The client of this Service must isolate a reasonably specific piece of text from which terms will
 * be extracted. The Five-Filters service does not ignore XML tags. Five-Filters is not tolerant.<p/>
 * 
 * TODO convert from Five-Filters to a real commercial, multi-language term extractor such as
 * AlchemyAPI or Maui
 * 
 * @author Jim Alexander (jhaood@gmail.com)
 */
@Component
public class FiveFiltersClient {

    @Log
    private Logger log;

    private RestTemplate restTemplate;

    Map<Pattern, String> regExMap = initRegExMap();

    // English blacklist
    // TODO move the blacklist up to the TermExtractorServiceImpl
    private String blacklistRegEx =
        "experience|work|team|software engineer|services|systems|data|design|ability|candidate|knowledge|customers|applications|software|"
            + "computer science|products|building|technologies|qualifications|projects|requirements|position|support|solutions|ceo|expertise|"
            + "cloud|employment|world|platform|company|understanding|skills|software development experience|software development projects|"
            + "software development|environment|infrastructure|opportunity|applicants|people|engineers|part|today|capital|practices|architecture|"
            + "role|leadership|field|years|manner|service|thousands|generation|teams|code|research|candidates|responsibilities|leader|models|day|"
            + "developers|billions|color|problems|approach|industry|machine|tools|features|religion|race|risk|implementation|quality|technology|"
            + "analysis|software community|customer relationship management|bain capital ventures|subject matter experts|environment";

    // The French blacklist - but FiveFilters doesn't handle French in any type of grammatical sense
    // so this isn't used now.
    /*
     * String frenchBlacklistRegEx =
     * "|sein|conception|missions|maitrise|poste|charge|production|afin|environnement|avez|equipe|realisation|mise|logiciels|groupe|informatique|"
     * +
     * "gestion|conseil|rediger|nos clients|ajoutee|formation|votre mission|signalisation ferroviaire|des connaissances|accompagnons nos clients|"
     * +
     * "cadre du developpement|'industrie|developpement logiciel|l offre yourcegid|des expertises metier|fonde son savoir-faire|performance des entreprises|"
     * +
     * "integrer l 'equipe|venez relever ce|des conditions egales|votre mission consiste|groupe cegid compte|sommes partenaires des|"
     * +
     * "assistance technique bureau|premier editeur francais|renforcons nos equipes|competences techniques plurielles|analyses fonctionnelles organiques|"
     * + "standards du client|un|une|le|la|les|l";
     */

    private Pattern blacklistPattern;

    @Autowired
    public FiveFiltersClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TermFrequencyList getTermFrequencyList(String content, Locale locale) {

        content = removeHtmlTagsAndOtherBogusContent(content, locale);

        content = removeBlacklistTerms(content);

        String[][] stringArray = executeFiveFiltersPost(content);
        
        if (stringArray == null) {
            if (content.length() < 10000) {
                testContentForInvalidCharacters(content);
            }
            throw new RuntimeException("Found invalid content, string-length=" + content.length());
        }
        List<TermFrequency> terms = new ArrayList<TermFrequency>();
        for (String[] innerArray : stringArray) {
            terms.add(new TermFrequency(innerArray));
        }
        TermFrequencyList termFrequencyList = new TermFrequencyList(terms);
        TermFrequencyList sortedTermFrequencyList = termFrequencyList.sort(new TermFrequency.FrequencyComparator());
        
        log.debug("\n  RegEx expression for NEW terms: " + sortedTermFrequencyList.createRegExpForNewTerms(blacklistRegEx));

        return sortedTermFrequencyList;
    }

    // public access ONLY for testing...
    public String[][] executeFiveFiltersPost(String content) {
        String query = "http://termextract.fivefilters.org/extract.php";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("text", content);
        params.add("output", "json");
        params.add("max", "300");

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, String>> requestEntity =
            new HttpEntity<MultiValueMap<String, String>>(params, requestHeaders);
        
        ResponseEntity<String[][]> stringArrayEntty =
            restTemplate.exchange(query, HttpMethod.POST, requestEntity, String[][].class);
        
        String[][] stringArray = stringArrayEntty.getBody();

        return stringArray;
    }

    private String removeBlacklistTerms(String content) {
        if (blacklistPattern == null) {
            return content;
        }
        content = blacklistPattern.matcher(content).replaceAll(" ");
        return content;
    }

    private String removeHtmlTagsAndOtherBogusContent(String content, Locale locale) {

        content = content.toLowerCase(locale);
        // remote accented characters
        content = Normalizer.normalize(content, Normalizer.Form.NFD);

        for (Pattern key : regExMap.keySet()) {
            content = key.matcher(content).replaceAll(regExMap.get(key));
        }
        return content;
    }

    @PostConstruct
    private Map<Pattern, String> initRegExMap() {
        if (StringUtils.hasLength(blacklistRegEx)) {
            blacklistPattern = Pattern.compile("\\W(" + blacklistRegEx + ")\\W");
        }

        Map<Pattern, String> regExMap = new HashMap<>();

        regExMap.put(Pattern.compile("<font[^>]*>"), "");
        regExMap.put(Pattern.compile("</font>"), "");
        regExMap.put(Pattern.compile("<span[^>]*>"), "");
        regExMap.put(Pattern.compile("</span>"), "");
        regExMap.put(Pattern.compile("<ul[^>]*>"), "");
        regExMap.put(Pattern.compile("</ul>"), "");
        regExMap.put(Pattern.compile("<li[^>]*>"), " ");
        regExMap.put(Pattern.compile("</li>"), ". ");
        regExMap.put(Pattern.compile("<div[^>]*>"), " ");
        regExMap.put(Pattern.compile("</div>"), ". ");
        regExMap.put(Pattern.compile("<p>"), ". ");
        regExMap.put(Pattern.compile("<p [^>]*>"), ". ");
        regExMap.put(Pattern.compile("</p>"), ". ");
        regExMap.put(Pattern.compile("<br>"), " ");
        regExMap.put(Pattern.compile("<br [^>]*>"), " ");
        regExMap.put(Pattern.compile("<p/>"), " ");
        regExMap.put(Pattern.compile("<b>"), "");
        regExMap.put(Pattern.compile("<b [^>]*>"), "");
        regExMap.put(Pattern.compile("</b>"), "");
        regExMap.put(Pattern.compile("<em>"), "");
        regExMap.put(Pattern.compile("<em [^>]*>"), "");
        regExMap.put(Pattern.compile("</em>"), "");

        regExMap.put(Pattern.compile("&[^;]*;"), " ");
        regExMap.put(Pattern.compile("- ·"), " ");
        regExMap.put(Pattern.compile("·"), " ");
        regExMap.put(Pattern.compile("\\(s\\)"), "");
        regExMap.put(Pattern.compile("­"), ""); // wacky character

        regExMap.put(Pattern.compile("\\W"), " ");

        // <U+0091> and other characters that are common in the UK

        regExMap.put(Pattern.compile("[\\u0091\\u0092]"), "'");

        regExMap.put(Pattern.compile("[§®«»\\u0095]"), "");
        // regExMap.put(Pattern.compile("®"), "");
        // regExMap.put(Pattern.compile("«"), "");
        // regExMap.put(Pattern.compile("»"), "");

        // remove accent characters that were separated from the associated character by
        // Normalize.normalize()
        regExMap.put(Pattern.compile("\\p{M}"), "");

        // Non-printable characters are not a significant problem in the US, UK or France
        // regExMap.put(Pattern.compile("[\\x00\\x08\\x0B\\x0C\\x0E-\\x1F]"), "");

        return regExMap;
    }

    /**
     * test the content for invalid characters
     */
    private void testContentForInvalidCharacters(String content) {
        int subSize = 200;
        for (int len = 0; len < (content.length() - subSize); len += subSize) {
            String subContent = content.substring(len, len + subSize);

            // delay so this doesn't smell like a DoS attack.
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }

            String[][] stringArray = executeFiveFiltersPost(subContent);
            if (stringArray == null) {
                throw new RuntimeException("Found invalid content, text=" + subContent);
            }
        }
    }

}