package com.example.wordcounter.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.InputStreamReader;

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

    public static void readFileContentSequentially(File file, LineProcessor lineProcessor) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineProcessor.process(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file content", e);
        }
    }

    public static void readFileContent(FileSplitter.FileSlice fileSlice, LineProcessor lineProcessor) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(fileSlice.file().toPath()), StandardCharsets.UTF_8))) {
            reader.skip(fileSlice.start());

            char[] buffer = new char[1024 * 16]; // 16KB buffer
            long bytesRead = 0;
            long bytesToRead = fileSlice.end() - fileSlice.start();
            StringBuilder lineBuilder = new StringBuilder();

            while (bytesRead < bytesToRead) {
                int charsRead = reader.read(buffer, 0, (int) Math.min(buffer.length, bytesToRead - bytesRead));
                if (charsRead == -1) {
                    break;
                }
                bytesRead += charsRead;
                lineBuilder.append(buffer, 0, charsRead);

                int lastNewLineIndex = lineBuilder.lastIndexOf(System.lineSeparator());
                if (lastNewLineIndex != -1) {
                    String[] lines = lineBuilder.substring(0, lastNewLineIndex + System.lineSeparator().length()).split(System.lineSeparator());
                    for (String line : lines) {
                        lineProcessor.process(line);
                    }
                    lineBuilder.delete(0, lastNewLineIndex + System.lineSeparator().length());
                }
            }

            if (!lineBuilder.isEmpty()) {
                lineProcessor.process(lineBuilder.toString());
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading file content", e);
        }
    }
    public interface LineProcessor {
        void process(String line);
    }
}
