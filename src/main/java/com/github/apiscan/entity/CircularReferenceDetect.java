package com.github.apiscan.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 循环引用检测开关
 */
@Getter
@Setter
public class CircularReferenceDetect {
    /**
     * 循环引用检测开关-url请求参数
     */
    private Boolean enableRequestParams;

    /**
     * 循环引用检测开关-url请求体
     */
    private Boolean enableRequestBody;

    /**
     * 循环引用检测开关-url响应
     */
    private Boolean enableResponse;
}
