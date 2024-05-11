package com.example.wordcounter.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileUtils {

    public static File convertMultipartFileToFile(MultipartFile multipartFile) {
        try {
            Path tempFilePath = Files.createTempFile("temp-", ".txt");
            Files.copy(multipartFile.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            return tempFilePath.toFile();
        } catch (IOException e) {
            throw new RuntimeException("Error converting multipart file to file", e);
        }
    }

    public static String readFileContent(File file) {
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file content", e);
        }
    }
}