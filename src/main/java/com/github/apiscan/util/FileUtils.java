package com.github.apiscan.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class FileUtils {
    public static List<File> scanClassFile(String baseDir) {
        List<File> files = scanFiles(baseDir);
        return filterFileByFormat(files, ".class");
    }

    public static List<File> filterFileByFormat(List<File> files, String format) {
        if (files == null || format == null) {
            return files == null ? new ArrayList<>() : files;
        }
        return files.stream()
                .filter(file -> file.getName().toLowerCase(Locale.ENGLISH).endsWith(format))
                .collect(Collectors.toList());
    }

    public static List<File> scanFiles(String baseDir) {
        List<File> files = new ArrayList<>();
        File file = new File(baseDir);
        if (!file.exists()) {
            return files;
        }
        doGetFiles(file, files);
        return files;
    }

    private static void doGetFiles(File currentFile, List<File> files) {
        if (currentFile == null || !currentFile.exists()) {
            return;
        }
        if (currentFile.isDirectory()) {
            File[] subs = currentFile.listFiles();
            if (subs == null) {
                return;
            }
            for (File sub : subs) {
                doGetFiles(sub, files);
            }
        } else if (currentFile.isFile()) {
            files.add(currentFile);
        }
    }
}
