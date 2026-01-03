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
    private String name;

    private String javaType;

    private String jsonType;

    private Boolean required;

    private String defaultValue;

    private List<ParamInfo> params;
}
