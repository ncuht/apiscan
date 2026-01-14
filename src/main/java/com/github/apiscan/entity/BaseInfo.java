package com.github.apiscan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseInfo {
    private String groupId;

    private String artifactId;

    private String version;

    private String fileName;

    private String basePath;

    private String outputFilePath;
}
