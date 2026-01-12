package com.github.apiscan.util;

import com.github.apiscan.constant.JavaType;
import com.github.apiscan.constant.JsonType;
import com.github.apiscan.constant.ValueConstants;
import com.github.apiscan.constant.WebBindAnnotationConstant;
import com.github.apiscan.entity.AnnotationInfo;
import com.github.apiscan.entity.ApiInfo;
import com.github.apiscan.entity.CircularReferenceDetect;
import com.github.apiscan.entity.ParamInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ApiParser {
    private static String URL_SEPARATOR = "/";

    private static String METHOD_SEPARATOR = "@";

    private static String CONTROLLER = "org.springframework.stereotype.Controller";

    public static List<ApiInfo> findUrls(Class<?> clazz, CircularReferenceDetect detect) {
        List<ApiInfo> apis = new ArrayList<>();
        List<AnnotationInfo> annotations = AnnotationUtils.getAnnotations(clazz.getAnnotations(), true);
        if (annotations.stream().noneMatch(annotation -> CONTROLLER.equals(annotation.getName()))) {
            return apis;
        }
        List<ApiInfo> methodApis = new ArrayList<>();
        Set<String> urlPres = parseClassUrls(annotations);
        // controller的方法必须是public类型，所以只需要getMethods()即可
        for (Method method : clazz.getMethods()) {
            List<ApiInfo> methodUrl = parseMethodUrl(method);
            ApiInfo apiInfo = parseMethod(method, detect);
            for (ApiInfo info : methodUrl) {
                info.setRequestParams(apiInfo.getRequestParams());
                info.setRequestBody(apiInfo.getRequestBody());
                info.setResponse(apiInfo.getResponse());
            }
            methodApis.addAll(methodUrl);
        }

        for (String urlPre : urlPres) {
            for (ApiInfo methodApi : methodApis) {
                apis.add(ApiInfo.builder()
                        .url(StringUtils.formatUrl(urlPre + URL_SEPARATOR + methodApi.getUrl()))
                        .methods(methodApi.getMethods())
                        .location(clazz.getTypeName() + METHOD_SEPARATOR + methodApi.getLocation())
                        .requestParams(methodApi.getRequestParams())
                        .requestBody(methodApi.getRequestBody())
                        .response(methodApi.getResponse())
                        .build());
            }
        }
        return apis;
    }

    /**
     * 对于方法参数，有以下规则：
     * 1、没有注解的方法入参默认被@RequestParam（简单类型）或@ModelAttribute（复杂对象），被@RequestParam注解时，参数名与变量名一致，required=false；
     * 2、@RequestParam若不指定请求参数名，则请求参数名与方法入参变量名一致，required=true；
     * 3、@PathVariable若不指定请求参数名，则请求参数名与方法入参变量名一致；
     * 4、@RequestParam注解的参数，若指定了defaultValue，则required始终为false；
     * 5、@RequestParam注解的参数如果是基本类型，若未指定defaultValue，则required始终为true；
     * 6、注解优先级：@RequestParam > @PathVariable > @ModelAttribute > @RequestBody = @RequestPart
     *
     * @param method 方法
     * @param detect 循环引用检测配置
     * @return 方法入参、返回值信息
     */
    private static ApiInfo parseMethod(Method method, CircularReferenceDetect detect) {
        ApiInfo apiInfo = ApiInfo.builder()
                .requestParams(new ArrayList<>())
                .requestBody(new ArrayList<>())
                .response(new ArrayList<>())
                .build();
        for (Parameter parameter : method.getParameters()) {
            parseMethodParam(parameter, apiInfo, detect);
        }

        parseMethodReturn(method, apiInfo, detect);
        return apiInfo;
    }

    private static void parseMethodReturn(Method method, ApiInfo apiInfo, CircularReferenceDetect detect) {
        Class<?> returnType = method.getReturnType();
        List<ParamInfo> list = new ArrayList<>();
        if (JavaType.isSimpleType(returnType)) {
            list.add(ParamInfo.builder()
                    .javaType(returnType.getTypeName())
                    .jsonType(JavaType.getJsonType(returnType))
                    .build());
        } else {
            list = ObjectParser.parseObject(returnType, method.getGenericReturnType(), detect.getEnableResponse());
        }
        apiInfo.setResponse(list);
    }

    private static void parseMethodParam(Parameter parameter, ApiInfo apiInfo, CircularReferenceDetect detect) {
        Class<?> type = parameter.getType();
        if (JavaType.isServlet(type)) {
            return;
        }
        AnnotationInfo annotation = getEffectiveWebBindAnnotationOnParam(parameter);
        if (annotation == null) {
            annotation = AnnotationInfo.builder()
                    .name(JavaType.isSimpleType(parameter.getType()) ?
                            WebBindAnnotationConstant.REQUEST_PARAM : WebBindAnnotationConstant.MODEL_ATTRIBUTE)
                    .properties(new HashMap<>())
                    .build();
        }
        formatAnnotationOnParam(parameter, annotation);

        Class<?> javaType = parameter.getType();
        String javaTypeName = javaType.getTypeName();
        Map<String, Object> properties = annotation.getProperties();
        ParamInfo paramInfo = ParamInfo.builder()
                .name((String) properties.get("name"))
                .javaType(javaTypeName)
                .jsonType(JavaType.getJsonType(javaType))
                .required((Boolean) properties.get("required"))
                .build();
        switch (annotation.getName()) {
            case WebBindAnnotationConstant.REQUEST_PARAM:
                paramInfo.setDefaultValue((String) properties.get("defaultValue"));
            case WebBindAnnotationConstant.REQUEST_PART:
            case WebBindAnnotationConstant.PATH_VARIABLE:
                apiInfo.getRequestParams().add(paramInfo);
                break;
            case WebBindAnnotationConstant.MODEL_ATTRIBUTE: {
                List<ParamInfo> paramInfos = ObjectParser.parseObject(javaType,
                        parameter.getParameterizedType(), detect.getEnableRequestParams());
                paramInfos.forEach(param -> param.setRequired(false));
                apiInfo.getRequestParams().addAll(flatParams(paramInfos));
                break;
            }
            case WebBindAnnotationConstant.REQUEST_BODY: {
                apiInfo.getRequestBody().addAll(ObjectParser.parseObject(javaType,
                        parameter.getParameterizedType(), detect.getEnableRequestBody()));
                break;
            }
        }
    }

    private static Collection<? extends ParamInfo> flatParams(List<ParamInfo> paramInfos) {
        List<ParamInfo> flatList = new ArrayList<>();
        for (ParamInfo paramInfo : paramInfos) {
            doFlatParams(paramInfo, "", flatList);
        }
        return flatList;
    }

    private static void doFlatParams(ParamInfo paramInfo, String prefix, List<ParamInfo> flatList) {
        if (paramInfo.getParams() == null || paramInfo.getParams().isEmpty()) {
            paramInfo.setName(prefix + paramInfo.getName());
            flatList.add(paramInfo);
            return;
        }
        if (JsonType.ARRAY.equals(paramInfo.getJsonType())) {
            prefix = prefix + paramInfo.getName() + "[0]";
        } else {
            prefix = prefix + paramInfo.getName() + ".";
        }

        for (ParamInfo subParamInfo : paramInfo.getParams()) {
            doFlatParams(subParamInfo, prefix, flatList);
        }
    }

    private static void formatAnnotationOnParam(Parameter parameter, AnnotationInfo annotation) {
        if (WebBindAnnotationConstant.REQUEST_BODY.equals(annotation.getName())) {
            return;
        }

        // 设置名称
        Map<String, Object> properties = annotation.getProperties();
        String annotationValue = (String) properties.get("value");
        String annotationName = (String) properties.get("name");
        String parameterName = parameter.getName();
        String name = StringUtils.isNotBlank(annotationValue) ? annotationValue :
                StringUtils.isNotBlank(annotationName) ? annotationName : parameterName;
        properties.put("value", name);
        properties.put("name", name);

        // 设置required字段
        if (WebBindAnnotationConstant.MODEL_ATTRIBUTE.equals(annotation.getName())) {
            properties.put("required", false);
        }
        if (!WebBindAnnotationConstant.REQUEST_PARAM.equals(annotation.getName())) {
            return;
        }
        boolean hasDefaultValue = !ValueConstants.DEFAULT_NONE.equals(properties.get("defaultValue"));
        if (hasDefaultValue) {
            properties.put("required", false);
        } else {
            properties.put("defaultValue", null);
            if (JavaType.isBaseType(parameter.getType())) {
                properties.put("required", true);
            }
        }
    }

    private static AnnotationInfo getEffectiveWebBindAnnotationOnParam(Parameter parameter) {
        List<AnnotationInfo> annotations = AnnotationUtils.getAnnotations(parameter.getAnnotations());
        List<AnnotationInfo> effectAnnotations = new ArrayList<>();
        for (AnnotationInfo annotation : annotations) {
            if (WebBindAnnotationConstant.PARAM_ANNOTATIONS.contains(annotation.getName())) {
                effectAnnotations.add(annotation);
            }
        }
        if (effectAnnotations.isEmpty()) {
            return null;
        }
        effectAnnotations.sort(Comparator.comparingInt(o -> getParamAnnotationPriority(o.getName())));
        return effectAnnotations.get(0);
    }

    private static int getParamAnnotationPriority(String name) {
        switch (name) {
            case WebBindAnnotationConstant.REQUEST_PARAM:
                return 1;
            case WebBindAnnotationConstant.PATH_VARIABLE:
                return 2;
            case WebBindAnnotationConstant.MODEL_ATTRIBUTE:
                return 3;
            case WebBindAnnotationConstant.REQUEST_BODY:
                return 4;
            case WebBindAnnotationConstant.REQUEST_PART:
                return 5;
            default:
                return 10;
        }
    }

    /**
     * 优先级：
     * 1、方法上如果有多个路径映射注解，@RequestMapping优先级最高，然后是@GetMapping、@PostMapping等组合注解
     * 2、组合注解有多个时（@RequestMapping注解只能有一个），以最先声明（最上面）的为准
     *
     * @param method 方法
     * @return url路径
     */
    private static List<ApiInfo> parseMethodUrl(Method method) {
        List<AnnotationInfo> annotations = AnnotationUtils.getAnnotations(method.getAnnotations());
        AnnotationInfo requestMapping = null;
        AnnotationInfo combinationMapping = null;
        for (AnnotationInfo annotation : annotations) {
            if (WebBindAnnotationConstant.REQUEST_MAPPING.equals(annotation.getName()) && requestMapping == null) {
                requestMapping = annotation;
                continue;
            }
            if (WebBindAnnotationConstant.MAPPINGS.contains(annotation.getName()) && combinationMapping == null) {
                combinationMapping = annotation;
            }
        }
        Set<String> paths;
        Set<String> methods = new HashSet<>();
        if (requestMapping != null) {
            Map<String, Object> properties = requestMapping.getProperties();
            paths = getPath(properties);
            Object[] requestMethods = (Object[]) properties.get("method");
            if (requestMethods == null || requestMethods.length == 0) {
                methods = WebBindAnnotationConstant.METHODS;
            } else {
                for (Object requestMethod : requestMethods) {
                    methods.add(requestMethod.toString());
                }
            }
        } else if (combinationMapping != null) {
            Map<String, Object> properties = combinationMapping.getProperties();
            paths = getPath(properties);
            String value = WebBindAnnotationConstant.MAPPING_METHOD_MAP.get(combinationMapping.getName());
            if (value != null) {
                methods.add(value);
            }
        } else {
            return new ArrayList<>();
        }

        List<ApiInfo> apis = new ArrayList<>();
        for (String path : paths) {
            apis.add(ApiInfo.builder()
                    .url(path)
                    .methods(methods)
                    .location(method.getName())
                    .build());
        }
        return apis;
    }

    private static Set<String> parseClassUrls(List<AnnotationInfo> annotations) {
        Optional<AnnotationInfo> optional = annotations.stream()
                .filter(annotation -> WebBindAnnotationConstant.REQUEST_MAPPING.equals(annotation.getName()))
                .findFirst();
        if (optional.isEmpty()) {
            return new HashSet<>();
        }
        // RequestMapping只能有一个
        Map<String, Object> properties = optional.get().getProperties();
        return getPath(properties);
    }

    /**
     * path的优先级高于value
     *
     * @param properties 注解属性
     * @return url值
     */
    private static Set<String> getPath(Map<String, Object> properties) {
        Set<String> urls = new HashSet<>();
        String[] classMappings = (String[]) properties.get("path");
        if (classMappings == null || classMappings.length == 0) {
            classMappings = (String[]) properties.get("value");
        }
        if (classMappings == null || classMappings.length == 0) {
            urls.add(URL_SEPARATOR);
        } else {
            for (String classMapping : classMappings) {
                urls.add(StringUtils.formatUrl(classMapping));
            }
        }
        return urls;
    }
}
