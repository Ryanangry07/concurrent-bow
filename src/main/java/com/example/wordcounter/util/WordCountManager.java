package com.example.wordcounter.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class WordCountManager {

    public static final HashSet<String> DEFAULT_IGNORE = new HashSet<>(Arrays.asList(
            "the", "and", "a", "an", "of", "in", "for", "is", "to", "with",
            "that", "i", "was", "he", "it", "had", "her", "she", "you", "his",
            "not", "as", "but", "at", "which", "on", "be", "him", "have", "my",
            "me", "by", "from", "this", "they", "all", "so", "were", "would",
            "who", "one", "what", "no", "when", "if", "them", "or", "been",
            "we", "there", "their", "are", "could", "more", "did", "like",
            "do", "your", "out", "about", "will", "any", "than", "some", "then"
    ));
    public static final String CUSTOM_IGNORED = "custom";

    public static final String SEQUENTIAL = "sequential";
    public static final String CONCURRENT_HASHMAP = "concurrent_hashmap";
    public static final String CONCURRENT_SKIP_LIST = "concurrent_skip_list";
    private final File[] files;
    private final long startTime;

    public WordCountManager(File[] files) {
        this.files = files;
        this.startTime = System.currentTimeMillis();
    }

    public WordCountResult countWords(AnalysisMethod analysisMethod, String ignoreOption, List<String> customIgnoreWords, Map<String, Object> resultsMap) throws InterruptedException, IOException {
        WordCountResult result = null;
        switch (analysisMethod) {
            case SEQUENTIAL:
                result = countWordsSequential(ignoreOption, customIgnoreWords);
                resultsMap.put("Sequential", result.getMapResults());
                break;
            case CONCURRENT_HASHMAP:
                result = countWordsHashMap(ignoreOption, customIgnoreWords);
                resultsMap.put("Concurrent_Hash_Map", result.getMapResults());
                break;
            case CONCURRENT_SKIP_LIST:
                result = countWordsSkipList(ignoreOption, customIgnoreWords);
                resultsMap.put("Concurrent_Skip_List", result.getMapResults());
                break;
        }
        return result;
    }

    public WordCountResult countWordsSequential(String ignoreOption, List<String> customIgnoreWords) throws IOException {
        long startSequentialTime = System.currentTimeMillis();
        HashMap<String, Integer> finalWordCounts = new HashMap<>();
        List<FileSplitter.FileSlice> fileSlices = FileSplitter.splitFiles(files);
        for (FileSplitter.FileSlice fs : fileSlices) {
            FileUtils.readFileContent(fs, line -> WordCounter.countWordsSequential(line, ignoreOption, customIgnoreWords, finalWordCounts));
        }
        long endSequentialTime = System.currentTimeMillis();
        return endAlgorithm(SEQUENTIAL, finalWordCounts, startSequentialTime, endSequentialTime);
    }

    public WordCountResult countWordsHashMap(String ignoreOption, List<String> customIgnoreWords) throws InterruptedException {
        long startConcurrentHashMapTime = System.currentTimeMillis();
        Map<String, Integer> finalWordCounts = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            List<Future<?>> futures = new ArrayList<>();
            List<FileSplitter.FileSlice> fileSlices = FileSplitter.splitFiles(files);
            for (FileSplitter.FileSlice fs : fileSlices) {
                futures.add(executor.submit(() -> {
                    FileUtils.readFileContent(fs, line -> {
                        Map<String, Integer> counts = WordCounter.countWordsHashMap(line, ignoreOption, customIgnoreWords);
                        counts.forEach((key, value) -> finalWordCounts.merge(key, value, Integer::sum));
                    });
                }));
            }

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    throw new RuntimeException("Error occurred while processing files.", e.getCause());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }

        long endConcurrentHashMapTime = System.currentTimeMillis();
        return endAlgorithm(CONCURRENT_HASHMAP, finalWordCounts, startConcurrentHashMapTime, endConcurrentHashMapTime);
    }

    public WordCountResult countWordsSkipList(String ignoreOption, List<String> customIgnoreWords) throws InterruptedException, IOException {
        long startConcurrentSkipListTime = System.currentTimeMillis();
        Map<String, Integer> finalWordCounts = new ConcurrentSkipListMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            List<FileSplitter.FileSlice> fileSlices = FileSplitter.splitFiles(files);
            for (FileSplitter.FileSlice fs : fileSlices) {
                executor.submit(() -> {
                    FileUtils.readFileContent(fs, line -> WordCounter.countWordsSkipList(line, ignoreOption, customIgnoreWords, finalWordCounts));
                });
            }
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        long endConcurrentSkipListTime = System.currentTimeMillis();
        return endAlgorithm(CONCURRENT_SKIP_LIST, finalWordCounts, startConcurrentSkipListTime, endConcurrentSkipListTime);
    }

    public WordCountResult endAlgorithm(String algorithm, Map<String, Integer> finalWordCounts, long startAlgorithmTime, long endAlgorithmTime) {
        int totalWords = 0;
        if (SEQUENTIAL.equals(algorithm)) {
            totalWords = WordCounter.totalWordsSequential;
            WordCounter.totalWordsSequential = 0;
        } else if (CONCURRENT_HASHMAP.equals(algorithm)) {
            totalWords = WordCounter.totalWordsHashMap.get();
            WordCounter.totalWordsHashMap.set(0);
        } else if (CONCURRENT_SKIP_LIST.equals(algorithm)) {
            totalWords = WordCounter.totalWordsSkipList.get();
            WordCounter.totalWordsSkipList.set(0);
        }

        Map<String, Object> mapOutput = ResultPresenter.generateResults(finalWordCounts, startTime, startAlgorithmTime, endAlgorithmTime, totalWords);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonResults = gson.toJson(mapOutput);

        return new WordCountResult(mapOutput, jsonResults, totalWords, endAlgorithmTime - startAlgorithmTime);
    }

    public long getStartTime() {
        return startTime;
    }

    public enum AnalysisMethod {
        SEQUENTIAL, CONCURRENT_HASHMAP, CONCURRENT_SKIP_LIST
    }
}
