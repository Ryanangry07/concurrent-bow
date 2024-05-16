package com.example.wordcounter.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

public class WordCountManager {

    public static final HashSet<String> DEFAULT_IGNORE = new HashSet<String>(Arrays.asList("the", "and", "a", "an", "of", "in", "for", "is", "to", "with"));
    public static final String CUSTOM_IGNORED = "custom";

    public static final String SEQUENTIAL = "sequential";
    public static final String CONCURRENT_HASHMAP = "concurrent_hashmap";
    public static final String CONCURRENT_SKIP_LIST = "concurrent_skip_list";
    private final File[] files;
    private long startTime;
    private long startAlgorithmTime;

    /**
     * Constructor to initialize the WordCountManager with a set of files.
     *
     * @param files Array of files to be processed.
     */
    public WordCountManager(File[] files) {
        this.files = files;
        this.startTime = System.currentTimeMillis();
    }

    public WordCountResult countWords(AnalysisMethod analysisMethod, String ignoreOption, List<String> customIgnoreWords, Map<String, Object> resultsMap) throws InterruptedException{
        WordCountResult result = null;
        switch (analysisMethod){
            case SEQUENTIAL:
                result = countWordsSequential(ignoreOption, customIgnoreWords);
                resultsMap.put("sequential", result.getMapResults());
                break;
            case CONCURRENT_HASHMAP:
                result = countWordsHashMap(ignoreOption, customIgnoreWords);
                resultsMap.put("concurrentHashMap", result.getMapResults());
                break;
            case CONCURRENT_SKIP_LIST:
                result = countWordsSkipList(ignoreOption, customIgnoreWords);
                resultsMap.put("skipList", result.getMapResults());
                break;
        }
        return result;
    }


    public WordCountResult countWordsSequential(String ignoreOption, List<String> customIgnoreWords){
        startAlgorithmTime = System.currentTimeMillis();
        HashMap<String, Integer> finalWordCounts = new HashMap<>();
        for (File file : files) {
            String fileContent = FileUtils.readFileContent(file); // Implement this method to read file content
            WordCounter.countWordsSequential(fileContent, ignoreOption, customIgnoreWords, finalWordCounts);
        }

        return endAlgorithm(SEQUENTIAL, finalWordCounts);
    }

    /**
     * Aggregates word counts from multiple files using a concurrent approach.
     * This method employs multiple threads to enhance performance and manage large sets of data.
     *
     * @param ignoreOption   The option to ignore specific words.
     * @param customIgnoreWords A list of custom ignore words.
     * @return An instance of WordCountResult containing the consolidated word counts and additional metadata.
     * @throws InterruptedException If the thread executing the task is interrupted.
     * @throws RuntimeException     If an error occurs during the processing of any file.
     */
    public WordCountResult countWordsHashMap(String ignoreOption, List<String> customIgnoreWords) throws InterruptedException {
        startAlgorithmTime = System.currentTimeMillis();
        // Thread-safe map to store final counts
        Map<String, Integer> finalWordCounts = new ConcurrentHashMap<>();

        // Thread pool creation
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            List<Future<Map<String, Integer>>> futures = new ArrayList<>();
            for (File file : files) {
                // Submit word counting tasks to the executor
                futures.add(executor.submit(() -> {
                    String fileContent = FileUtils.readFileContent(file); // Implement this method to read file content
                    return WordCounter.countWordsHashMap(fileContent, ignoreOption, customIgnoreWords);
                }));
            }

            for (Future<Map<String, Integer>> future : futures) {
                try {
                    // Retrieve result from future
                    Map<String, Integer> wordCount = future.get();
                    synchronized (finalWordCounts) {
                        // Merge results into the final map
                        wordCount.forEach((key, value) -> finalWordCounts.merge(key, value, Integer::sum));
                    }
                } catch (ExecutionException e) {
                    System.err.println("Error processing file: " + e.getCause().getMessage());
                    throw new RuntimeException("Error occurred while processing files.", e.getCause());
                }
            }
        } finally {
            // Initiate shutdown
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                // Force shutdown if tasks did not terminate
                executor.shutdownNow();
            }
        }

        return endAlgorithm(CONCURRENT_HASHMAP, finalWordCounts);
    }


    public WordCountResult countWordsSkipList(String ignoreOption, List<String> customIgnoreWords) throws InterruptedException {
        startAlgorithmTime = System.currentTimeMillis();
        // Thread-safe map to store final counts
        Map<String, Integer> finalWordCounts = new ConcurrentHashMap<>();
        // Thread pool creation
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            for (File file : files) {
                if (file.isFile()) {
                    executor.submit(() -> {
                        String fileContent = FileUtils.readFileContent(file); // Implement this method to read file content
                        return WordCounter.countWordsSkipList(fileContent, ignoreOption, customIgnoreWords, finalWordCounts);
                    });
                }
            }

            /*for (Future<Map<String, Integer>> future : futures) {
                try {
                    // Retrieve result from future
                    Map<String, Integer> wordCount = future.get();
                    synchronized (finalWordCounts) {
                        // Merge results into the final map
                        wordCount.forEach((key, value) -> finalWordCounts.merge(key, value, Integer::sum));
                    }
                } catch (ExecutionException e) {
                    System.err.println("Error processing file: " + e.getCause().getMessage());
                    throw new RuntimeException("Error occurred while processing files.", e.getCause());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }*/
        } finally {
            // Initiate shutdown
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                // Force shutdown if tasks did not terminate
                executor.shutdownNow();
            }
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        return endAlgorithm(CONCURRENT_SKIP_LIST, finalWordCounts);
    }

    public WordCountResult endAlgorithm(String algorithm, Map<String, Integer> finalWordCounts){
        long endAlgorithmTime = System.currentTimeMillis();
        // reset total words for each algorithm
        int totalWords = 0;
        if(SEQUENTIAL.equals(algorithm)){
            totalWords = WordCounter.totalWordsSequential;
            WordCounter.totalWordsSequential = 0;
        }else if(CONCURRENT_HASHMAP.equals(algorithm)){
            totalWords = WordCounter.totalWordsHashMap.get();
            WordCounter.totalWordsHashMap.set(0);
        }else if(CONCURRENT_SKIP_LIST.equals(algorithm)){
            totalWords = WordCounter.totalWordsSkipList.get();
            WordCounter.totalWordsSkipList.set(0);
        }

        // Generate the results using ResultPresenter
        Map<String, Object> mapOutput = ResultPresenter.generateResults(finalWordCounts, startTime, startAlgorithmTime, endAlgorithmTime, totalWords);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonResults = gson.toJson(mapOutput);

        return new WordCountResult(mapOutput, jsonResults, totalWords, endAlgorithmTime - startTime);
    }



    public long getStartTime() {
        return startTime;
    }
    public enum AnalysisMethod {
        SEQUENTIAL, CONCURRENT_HASHMAP, CONCURRENT_SKIP_LIST
    }
}