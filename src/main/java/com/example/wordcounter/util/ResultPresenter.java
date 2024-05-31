package com.example.wordcounter.util;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ResultPresenter {
    /**
     * Displays the results of word counts in a sorted JSON format. This method sorts the word counts,
     * calculates the total number of words, and measures the execution time since a provided start time.
     * Finally, it prints the results in JSON format with pretty printing.
     *
     * @param wordCounts A map of words to their corresponding counts.
     * @param startTime  The start time in milliseconds used to calculate the total execution time.
     */
    public static Map<String, Object> generateResults(Map<String, Integer> wordCounts, long startTime, long startAlgorithmTime, long endAlgorithmTime, Integer totalWords) {
        // convert and sort final word counts to list
        List<Map.Entry<String, Integer>> entries = getEntries(wordCounts);

        AtomicInteger totalWordCount = new AtomicInteger(0);

        // create a LinkedHashMap to store the sorted order
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
            totalWordCount.addAndGet(entry.getValue());
        }

        // create a JSON using Gson
        Map<String, Object> mapOutput = new HashMap<>();
        mapOutput.put("totalWords", totalWordCount);
        mapOutput.put("wordCount", sortedMap);
        mapOutput.put("uniqueWords", sortedMap.size());
        mapOutput.put("algoTimeInMs", endAlgorithmTime - startAlgorithmTime);
        mapOutput.put("totalTimeInMs", System.currentTimeMillis() - startAlgorithmTime);

        return mapOutput;
    }


    /**
     * Sorts the entries of a word count map first by the count in descending order, and then by the word
     * in alphabetical order if the counts are the same. This method is used to prepare data for display
     *
     * @param wordCounts The map of words with their counts to be sorted.
     * @return A list of entries sorted according to the specified criteria.
     */
    private static List<Map.Entry<String, Integer>> getEntries(Map<String, Integer> wordCounts) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(wordCounts.entrySet());

        // lambda expression to sort
        entries.sort((o1, o2) -> {
            // first sort in descending order of value
            int valueCompare = o2.getValue().compareTo(o1.getValue());
            if (valueCompare != 0) {
                return valueCompare;
            }
            // for the same value cases, sort in the alphabetical order of keys
            return o1.getKey().compareTo(o2.getKey());
        });
        return entries;
    }
}
