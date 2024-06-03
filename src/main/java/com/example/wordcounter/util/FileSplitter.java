package com.example.wordcounter.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileSplitter {
    public static final long SLICE_SIZE = 16 * 1024 * 1024;
    public static class FileSlice {
        private final File file;
        private final long start;
        private final long end;

        public FileSlice(File file, long start, long end) {
            this.file = file;
            this.start = start;
            this.end = end;
        }
        public File getFile() {
            return file;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }
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
