package com.example.wordcounter.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileSplitter {
    public static final long SLICE_SIZE = 8 * 1024 * 1024; // 8 MB

    public record FileSlice(File file, long start, long end) {
    }

    public static List<FileSlice> splitFiles(File[] files) throws IOException {
        List<FileSlice> fileSlices = new ArrayList<>();


        for (File file : files) {
            long fileSize = file.length();
            if (fileSize <= SLICE_SIZE) {
                fileSlices.add(new FileSlice(file, 0, fileSize));
            } else {
                long numSlices = (fileSize + SLICE_SIZE - 1) / SLICE_SIZE;

                for (long i = 0; i < numSlices; i++) {
                    long start = i * SLICE_SIZE;
                    long end = Math.min((i + 1) * SLICE_SIZE, fileSize);
                    fileSlices.add(new FileSlice(file, start, end));
                }
            }
        }

        return fileSlices;
    }


}
