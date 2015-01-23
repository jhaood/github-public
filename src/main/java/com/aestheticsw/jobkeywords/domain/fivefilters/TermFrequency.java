package com.aestheticsw.jobkeywords.domain.fivefilters;

import java.util.Comparator;

public class TermFrequency {
    private String term;
    private int frequency;
    private int wordCount;
    
    /**
     * A comparator that sorts in descending order. 
     */
    public static class FrequencyComparator implements Comparator<TermFrequency> {

        @Override
        public int compare(TermFrequency term1, TermFrequency term2) {
            // compute negative for descending order
            int frequencyComp = - Integer.compare(term1.getFrequency(), term2.getFrequency());
            if (frequencyComp != 0) {
                return frequencyComp;
            }
            // return negative for descending order
            return - Integer.compare(term1.getWordCount(), term2.getWordCount());
        }
    }

    public static class TermComparator implements Comparator<TermFrequency> {

        @Override
        public int compare(TermFrequency term1, TermFrequency term2) {
            return term1.getTerm().compareTo(term2.getTerm());
        }
    }

    public TermFrequency(String[] innerArray) {
        if (innerArray == null || innerArray.length != 3) {
            throw new RuntimeException("Invalid FiveFilters response structure: " + innerArray);
        }
        term = innerArray[0];
        frequency = Integer.parseInt(innerArray[1]);
        wordCount = Integer.parseInt(innerArray[2]);
    }
    
    @Override 
    public String toString() {
        return super.toString() + " term: " + term + ", freq: " + frequency;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

}
