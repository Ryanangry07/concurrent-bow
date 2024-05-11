package com.example.wordcounter.service;

import com.example.wordcounter.util.FileUtils;
import com.example.wordcounter.util.WordCountManager;
import com.example.wordcounter.util.WordCountResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.wordcounter.util.WordCountManager.DEFAULT_IGNORE;

@Service
public class WordCountService {

    public Map<String, Object> performWordCount(List<MultipartFile> files, String ignoreOption, List<String> customIgnoreWords, List<String> analysisMethods) {
        // Convert MultipartFile to File
        File[] fileArray = files.stream()
                .map(FileUtils::convertMultipartFileToFile)
                .toArray(File[]::new);

        // Set up the WordCountManager with the loaded files
        WordCountManager manager = new WordCountManager(fileArray);


        // Perform word count analysis
        WordCountResult results = null;
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, Object> resultsMap = new HashMap<>();

        for(String analysisMethod : analysisMethods){
            // Set up the analysis method
            WordCountManager.AnalysisMethod analysisMethodEnum = WordCountManager.AnalysisMethod.valueOf(analysisMethod.toUpperCase());

            try {
                results = manager.countWords(analysisMethodEnum, ignoreOption, customIgnoreWords, resultsMap);
            } catch (Exception e) {
                throw new RuntimeException("Error occurred while processing files.", e);
            }
        }



        responseMap.put("results", resultsMap);
        responseMap.put("ignored", "custom".equals(ignoreOption) ? customIgnoreWords : DEFAULT_IGNORE);
        responseMap.put("error", "No error caught during this execution.");
//        responseMap.put("totalWords", results.getTotalWords());
//        responseMap.put("executionTimeMs", results.getExecutionTimeMs());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(responseMap);
        results.setJsonResults(jsonOutput);
        return responseMap;
    }
}