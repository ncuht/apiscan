package com.github.apiscan.constant;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * web注解常量类
 */
public class WebBindAnnotationConstant {
    /*========================================方法注解========================================*/
    public static String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";

    public static String GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping";

    public static String POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping";

    public static String PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping";

    public static String DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping";

    public static String PATCH_MAPPING = "org.springframework.web.bind.annotation.PatchMapping";

    public static Set<String> MAPPINGS = Set.of(GET_MAPPING, POST_MAPPING, PUT_MAPPING, DELETE_MAPPING, PATCH_MAPPING);

    public static Set<String> METHODS = Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "TRACE");

    public static Map<String, String> MAPPING_METHOD_MAP = new HashMap<>();

    static {
        MAPPING_METHOD_MAP.put(GET_MAPPING, "GET");
        MAPPING_METHOD_MAP.put(POST_MAPPING, "POST");
        MAPPING_METHOD_MAP.put(PUT_MAPPING, "PUT");
        MAPPING_METHOD_MAP.put(DELETE_MAPPING, "DELETE");
        MAPPING_METHOD_MAP.put(PATCH_MAPPING, "PATCH");
    }

    /*========================================方法参数注解========================================*/
    public static final String REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam";

    public static final String PATH_VARIABLE = "org.springframework.web.bind.annotation.PathVariable";

    public static final String MODEL_ATTRIBUTE = "org.springframework.web.bind.annotation.ModelAttribute";

    /* @RequestBody @RequestPart 互斥 */
    public static final String REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody";

    public static final String REQUEST_PART = "org.springframework.web.bind.annotation.RequestPart";

    public static Set<String> PARAM_ANNOTATIONS = Set.of(REQUEST_PARAM,
            PATH_VARIABLE,
            MODEL_ATTRIBUTE,
            REQUEST_BODY,
            REQUEST_PART);
}