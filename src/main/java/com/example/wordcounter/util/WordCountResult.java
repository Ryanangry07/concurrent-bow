package com.example.wordcounter.util;

import java.util.Map;

public class WordCountResult {
    private String jsonResults;

    private Map<String, Object> mapResults;
    private int totalWords;
    private long executionTimeMs;

    public WordCountResult(Map<String, Object> mapResults, String jsonResults, int totalWords, long executionTimeMs) {
        this.mapResults = mapResults;
        this.jsonResults = jsonResults;
        this.totalWords = totalWords;
        this.executionTimeMs = executionTimeMs;
    }

    public WordCountResult() {
    }

    public String getJsonResults() {
        return jsonResults;
    }

    public void setJsonResults(String jsonResults){
        this.jsonResults = jsonResults;
    }

    public Map<String, Object> getMapResults(){
        return this.mapResults;
    }

    public void setMapResults(Map<String, Object> mapResults) {
        this.mapResults = mapResults;
    }

    public int getTotalWords() {
        return totalWords;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setTotalWords(int totalWords) {
        this.totalWords = totalWords;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
}