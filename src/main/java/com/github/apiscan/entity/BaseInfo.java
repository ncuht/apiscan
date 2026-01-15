package com.github.apiscan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseInfo {
    /**
     * groupId
     */
    private String groupId;

    /**
     * artifactId
     */
    private String artifactId;

    /**
     * version
     */
    private String version;

    /**
     * 项目根目录
     */
    private Path basePath;

    /**
     * 输出目录
     */
    private Path outputPath;

    /**
     * 是否开启debug
     */
    private Boolean debug;

    /**
     * urlClassLoader
     */
    private URLClassLoader urlClassLoader;

    /**
     * 项目的Bean名称
     */
    private List<String> beanNames;

    /**
     * 循环引用检测开关
     */
    private CircularReferenceDetect detect;
}
