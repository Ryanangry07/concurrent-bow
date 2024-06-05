package com.example.wordcounter.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.wordcounter.util.WordCountManager.CUSTOM_IGNORED;
import static com.example.wordcounter.util.WordCountManager.DEFAULT_IGNORE;

public class WordCounter {
    public static int totalWordsSequential = 0;
    public static AtomicInteger totalWordsHashMap = new AtomicInteger(0);
    public static AtomicInteger totalWordsSkipList = new AtomicInteger(0);

    public static void countWordsSequential(String content, String ignoreOption, List<String> customIgnoreWords, Map<String, Integer> finalWordCounts) {

        Set<String> ignored;
        if (CUSTOM_IGNORED.equals(ignoreOption)) {
            ignored = new HashSet<>(customIgnoreWords);
        } else {
            ignored = DEFAULT_IGNORE;
        }

        String[] words = content.replaceAll("\\d+", "")
                .replaceAll("[^\\p{L}\\p{Nd}'’-]+", " ")
                .replaceAll("--+", " ")
                .split("\\s+");

        for (String word : words) {
            word = word.toLowerCase();
            if (!word.isEmpty() && !ignored.contains(word)) {
                totalWordsSequential++;
                finalWordCounts.put(word, finalWordCounts.getOrDefault(word, 0) + 1);
            }
        }


    }


    /**
     * Counts the frequency of each word in the provided text content.
     * Also, counts the total number of words using an AtomicInteger.
     * Words are defined as sequences of characters separated by any type of whitespace.
     *
     * @param content The text content to be processed.
     * @return A map of words to their corresponding counts.
     */
    public static Map<String, Integer> countWordsHashMap(String content, String ignoreOption, List<String> customIgnoreWords) {
        Map<String, Integer> wordCount = new ConcurrentHashMap<>();

        Set<String> ignored;
        if (CUSTOM_IGNORED.equals(ignoreOption)) {
            ignored = new HashSet<>(customIgnoreWords);
        } else {
            ignored = DEFAULT_IGNORE;
        }

        String[] words = content.replaceAll("\\d+", "")
                .replaceAll("[^\\p{L}\\p{Nd}'’-]+", " ")
                .replaceAll("--+", " ")
                .split("\\s+");

        for (String word : words) {
            word = word.toLowerCase();

            // keep the "\\u0027" unicode because browsers can parse it into an apostrophe
            // String cleanedWord = word.toLowerCase().replace("\\u0027", "'");

            if (!word.isEmpty() && !ignored.contains(word)) {
                // Increment total word count for each word processed
                totalWordsHashMap.incrementAndGet();
                // Merge word count into the map
                wordCount.merge(word, 1, Integer::sum);
            }
        }


        return wordCount;
    }

    public static Map<String, Integer> countWordsSkipList(String content, String ignoreOption, List<String> customIgnoreWords, Map<String, Integer> finalWordCounts) {
        Set<String> ignored;
        if (CUSTOM_IGNORED.equals(ignoreOption)) {
            ignored = new HashSet<>(customIgnoreWords);
        } else {
            ignored = DEFAULT_IGNORE;
        }
        String[] words = content.replaceAll("\\d+", "")
                .replaceAll("[^\\p{L}\\p{Nd}'’-]+", " ")
                .replaceAll("--+", " ")
                .split("\\s+");

        for (String word : words) {
            word = word.toLowerCase();
            if (!word.isEmpty() && !ignored.contains(word)) {
                totalWordsSkipList.incrementAndGet();
                finalWordCounts.merge(word, 1, Integer::sum);
            }

        }
        return finalWordCounts;
    }
}