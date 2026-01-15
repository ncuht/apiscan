package com.github.apiscan.util;

import com.github.apiscan.entity.BaseInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 文件通用类
 */
public class FileUtils {
    /**
     * 扫描class文件
     *
     * @param baseDir 根目录
     * @return 根目录下所有的class文件
     */
    public static List<File> scanClassFile(String baseDir) {
        List<File> files = scanFiles(baseDir);
        return filterFileByFormat(files, ".class");
    }

    /**
     * 根据文件格式过滤
     *
     * @param files  文件
     * @param format 格式
     * @return 指定格式的文件
     */
    public static List<File> filterFileByFormat(List<File> files, String format) {
        if (files == null || format == null) {
            return files == null ? new ArrayList<>() : files;
        }
        return files.stream()
                .filter(file -> file.getName().toLowerCase(Locale.ENGLISH).endsWith(format))
                .collect(Collectors.toList());
    }

    /**
     * 扫描所有文件
     *
     * @param baseDir 根目录
     * @return 所有文件
     */
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

    /**
     * 写文件
     *
     * @param file    文件
     * @param context 文件内容
     */
    public static void write(File file, String context) {
        try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(context);
            fw.flush();
        } catch (IOException exception) {
            exception.getMessage();
        }
    }
}
