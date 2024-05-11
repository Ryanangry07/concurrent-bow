package com.example.wordcounter.controller;

import com.example.wordcounter.service.WordCountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class WordCountController {

    private final WordCountService wordCountService;

    @Autowired
    public WordCountController(WordCountService wordCountService) {
        this.wordCountService = wordCountService;
    }

    private final String jsonFilePath = "C:\\Users\\lenovo\\Desktop\\CP_Group_Project\\src\\main\\resources\\static\\resultPage\\response.json";
//    private final String jsonFilePath = "E:\\ZSemester6\\Cocurrent Programming\\test\\hi.json";

    @GetMapping("/response.json")
    public ResponseEntity<Resource> getDynamicJson() {
        Resource file = new FileSystemResource(jsonFilePath);
        return ResponseEntity.ok().body(file);
    }

    @PostMapping("/wordcount")
    public ResponseEntity<Map<String, Object>> performWordCount(
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam("ignoreOption") String ignoreOption,
            @RequestParam("customIgnoreWords") List<String> customIgnoreWords,
            @RequestParam("analysisMethod") List<String> analysisMethod) {

        Map<String, Object> responseMap = wordCountService.performWordCount(files, ignoreOption, customIgnoreWords, analysisMethod);


        // Save the result to a JSON file
        saveResultsToFile(responseMap);

//        return ResponseEntity.ok(results);
        return ResponseEntity.ok(responseMap);
    }

    private void saveResultsToFile(Map<String, Object> results) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // path to the resources folder
            Path path = Paths.get(jsonFilePath);

            // convert the results object to JSON string
            String jsonString = mapper.writeValueAsString(results);

            // Write the JSON string to the file
            // check path
            try {
                if (Files.notExists(path)) {
                    Files.createDirectories(path.getParent());
                    Files.createFile(path);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Files.write(path, jsonString.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





}