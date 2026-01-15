package com.github.apiscan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 入参属性
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParamInfo {
    /**
     * 入参名称
     */
    private String name;

    /**
     * Java类型
     */
    private String javaType;

    /**
     * Json类型
     */
    private String jsonType;

    /**
     * 是否必填字段
     */
    private Boolean required;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 下层参数
     */
    private List<ParamInfo> params;
}
