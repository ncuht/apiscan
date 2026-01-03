package com.github.apiscan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * api信息
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiInfo {
    private Set<String> methods;

    private String url;

    private String location;

    private List<ParamInfo> requestParams;

    private List<ParamInfo> requestBody;

    private List<ParamInfo> response;
}
