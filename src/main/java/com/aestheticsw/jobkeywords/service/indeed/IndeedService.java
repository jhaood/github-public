package com.aestheticsw.jobkeywords.service.indeed;

import java.io.IOException;
import java.util.Collections;

import net.exacode.spring.logging.inject.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.xml.xpath.XPathOperations;

import com.aestheticsw.jobkeywords.domain.indeed.JobListResponse;
import com.aestheticsw.jobkeywords.domain.termfrequency.SearchParameters;
import com.aestheticsw.jobkeywords.service.rest.XUserAgentInterceptor;

@Component
public class IndeedService {

    @Log
    private Logger log;

    private RestTemplate restTemplate;

    private XPathOperations xpathTemplate;

    @Autowired
    public IndeedService(RestTemplate restTemplate, XPathOperations xpathTemplate) {
        this.restTemplate = restTemplate;
        this.xpathTemplate = xpathTemplate;

        // Not needed now that converters are working properly.. but might be useful later to tweak config
        // 
        // List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        // for (HttpMessageConverter<?> converter : messageConverters) {
        // if (converter.canRead(Source.class, MediaType.APPLICATION_XML)) {
        // // converter.canRead(Source.class, MediaType.TEXT_HTML);
        // if (! (converter instanceof SourceHttpMessageConverter<?>)) {
        // throw new RuntimeException("Unknown XML Converter type: " +
        // converter.getClass().getName());
        // }
        //
        // SourceHttpMessageConverter<?> xmlConverter = (SourceHttpMessageConverter<?>) converter;
        // List<MediaType> mediaTypes = new
        // ArrayList<MediaType>(xmlConverter.getSupportedMediaTypes());
        // mediaTypes.add(MediaType.TEXT_HTML);
        // xmlConverter.setSupportedMediaTypes(mediaTypes);
        // }
        // }

        // restTemplate.setMessageConverters(messageConverters);
    }

    public JobListResponse getIndeedJobList(SearchParameters params) {
        /*
         * More search parameters...
         * String query =
         * "http://api.indeed.com/ads/apisearch?publisher=1652353865637104&q=java&l=austin%2C+tx" +
         * "&sort=&radius=&st=&jt=&start=&limit=&fromage=&filter=&latlong=1&co=us&chnl=&userip=1.2.3.4"
         * + "&useragent=Mozilla/%2F4.0%28Firefox%29&v=2";
         */
        StringBuilder queryUrl = new StringBuilder();
        String query = encodeIndeedQuery(params.getQuery());

        queryUrl.append("http://api.indeed.com/ads/apisearch?publisher=1652353865637104&v=2&q=").append(query);
        queryUrl.append("&limit=").append(params.getJobCount());
        queryUrl.append("&start=").append(params.getStart());
        if (params.getLocale() != null) {
            queryUrl.append("&co=").append(params.getLocale().getCountry());
        }
        if (params.getCity() != null) {
            queryUrl.append("&l=").append(params.getCity());
        }
        if (params.getRadius() > 0) {
            queryUrl.append("&radius=").append(params.getRadius());
        }
        if (params.getSort() != null) {
            queryUrl.append("&sort=").append(params.getSort());
        }

        log.debug("Indeed job-list query: " + queryUrl);

        restTemplate.setInterceptors(Collections.singletonList(new XUserAgentInterceptor()));

        /*
         * More HttpClient attempts at controlling headers.
         * HttpHeaders headers = new HttpHeaders();
         * headers.setAccept(Arrays.asList(MediaType.APPLICATION_XML));
         * HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
         * String stringResponse = restTemplate.exchange(query, HttpMethod.GET, entity,
         * String.class).getBody();
         * String stringResponse = restTemplate.getForObject(queryUrl, String.class);
         */

        /*
         * http://api.indeed.com/ads/apisearch?publisher=1652353865637104&v=2&q=senior+java+%
         * 28engineering+or+developer+or+engineer%29&limit=2&start=0&co=US&l=San Francisco
         */
        JobListResponse jobListResponse = restTemplate.getForObject(queryUrl.toString(), JobListResponse.class);
        log.debug("Response: " + jobListResponse);

        if (jobListResponse.getTotalResults() == 0) {
            throw new IllegalArgumentException("Query retrieved no results from Indeed: " + queryUrl);
        }

        return jobListResponse;
    }

    private String encodeIndeedQuery(String query) {
        /*
         * query = query.replaceAll(" ", "+");
         * query = query.replaceAll("\\(", "%28");
         * query = query.replaceAll("\\)", "%29");
         */
        return query;
    }

    /**
     * This method is dependent upon the JSoup library which can consume malformed 
     * HTML and XML with invalid syntax. 
     * 
     * JSoup can't be tested easily.
     * 
     * TODO convert JSoup to HtmlCleaner
     */
    public String getIndeedJobDetails(String url) throws IOException {
        log.debug("Indeed job-details query: " + url);
        Document doc = Jsoup.connect(url).get();
        Elements jobHeader = doc.select("#job_header > b > font");
        Elements jobSummary = doc.select("#job_summary");
        StringBuilder sb = new StringBuilder();
        sb.append(jobHeader.toString()).append("\n");
        sb.append(jobSummary.toString());
        return sb.toString();
    }

}
