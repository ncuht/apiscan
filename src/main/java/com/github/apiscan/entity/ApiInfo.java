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
    /**
     * HTTP方法。如：GET、PUT、POST、DELETE
     */
    private Set<String> httpMethods;

    /**
     * url
     */
    private String url;

    /**
     * API所在文件
     */
    private String file;

    /**
     * API所在方法
     */
    private String method;

    /**
     * url请求参数
     */
    private List<ParamInfo> requestParams;

    /**
     * url请求体
     */
    private List<ParamInfo> requestBody;

    /**
     * url响应
     */
    private List<ParamInfo> response;

    /**
     * url请求体实例
     */
    private String bodyText;

    /**
     * url响应实例
     */
    private String responseText;
}
