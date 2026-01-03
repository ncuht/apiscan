package com.github.apiscan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 注解信息
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnnotationInfo {
    /**
     * 注解对象
     */
    private Annotation annotation;

    /**
     * 注解名
     */
    private String name;

    /**
     * 注解简写名
     */
    private String simpleName;

    /**
     * 注解属性
     */
    private Map<String, Object> properties;
}
