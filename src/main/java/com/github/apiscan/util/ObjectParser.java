package com.github.apiscan.util;

import com.github.apiscan.constant.JavaType;
import com.github.apiscan.constant.JsonType;
import com.github.apiscan.entity.ParamInfo;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 对象属性解析器
 */
public class ObjectParser {
    private static final String NO_NAME = "";

    private static final String REF = "$ref:";

    /**
     * 解析POJO，映射Java属性为JSON属性
     *
     * @param clazz                         类class
     * @param enableCircularReferenceDetect 开启循环引用检测
     * @return Java、JSON属性映射
     */
    public static List<ParamInfo> parseObject(Class<?> clazz, Type type, boolean enableCircularReferenceDetect) {
        if (clazz == null) {
            return new ArrayList<>();
        }
        List<ParamInfo> list = new ArrayList<>();
        Set<Class<?>> parsedPojoSet = new HashSet<>();
        if (clazz.isArray() || JavaType.isCollection(clazz)) {
            parseByGenericType(type, NO_NAME, list, parsedPojoSet, enableCircularReferenceDetect);
        } else {
            for (Field declaredField : clazz.getDeclaredFields()) {
                doParseObject(declaredField.getType(), declaredField, list, parsedPojoSet, enableCircularReferenceDetect);
            }
        }
        return list;
    }

    private static void doParseObject(Class<?> clazz, Field field, List<ParamInfo> list, Set<Class<?>> parsedPojoSet,
                                      boolean enableCircularReferenceDetect) {
        if (JavaType.isSimpleType(clazz) || JavaType.isMap(clazz) || field == null) {
            ParamInfo info = ParamInfo.builder()
                    .name(field == null ? NO_NAME : field.getName())
                    .javaType(clazz.getTypeName())
                    .jsonType(JavaType.getJsonType(clazz))
                    .build();
            list.add(info);
            return;
        }
        Type genericType = field.getGenericType();
        parseByGenericType(genericType, field.getName(), list, parsedPojoSet, enableCircularReferenceDetect);
    }

    private static void parseByGenericType(Type genericType, String name, List<ParamInfo> list,
                                           Set<Class<?>> parsedPojoSet, boolean enableCircularReferenceDetect) {
        if (genericType instanceof ParameterizedType parameterizedType) {
            // List<List<String>>、List<String[]>
            Type type = parameterizedType.getActualTypeArguments()[0];
            ParamInfo info = ParamInfo.builder()
                    .name(name)
                    .javaType(genericType.getTypeName())
                    .jsonType(JsonType.ARRAY)
                    .params(new ArrayList<>())
                    .build();
            list.add(info);
            if (parameterizedType.getActualTypeArguments().length == 0) {
                return;
            }
            parseByGenericType(type, NO_NAME, info.getParams(), parsedPojoSet, enableCircularReferenceDetect);
        } else if (genericType instanceof GenericArrayType genericArrayType) {
            // 泛型数组 List<T>[] T[]
            ParamInfo info = ParamInfo.builder()
                    .name(name)
                    .javaType(genericType.getTypeName())
                    .jsonType(JsonType.ARRAY)
                    .params(new ArrayList<>())
                    .build();
            list.add(info);
            parseByGenericType(genericArrayType.getGenericComponentType(), NO_NAME, info.getParams(), parsedPojoSet,
                    enableCircularReferenceDetect);
        } else if (genericType instanceof WildcardType wildcardType) {
            // ? extends Object
            Type[] upperBounds = wildcardType.getUpperBounds();
            // 虽然到目前为止，通配符最多只能有一个上限，但应该编写此方法以适应多个界限
            if (upperBounds.length == 0) {
                return;
            }
            Type upperBound = upperBounds[0];
            parseByGenericType(upperBound, name, list, parsedPojoSet, enableCircularReferenceDetect);
        } else if (genericType instanceof TypeVariable) {
            // 泛型，如T K V E等
            list.add(ParamInfo.builder()
                    .name(NO_NAME)
                    .javaType(genericType.getTypeName())
                    .jsonType(JsonType.OBJECT)
                    .build());
        } else if (genericType instanceof Class<?> clazz) {
            // 一般类型。数组、对象等
            if (clazz.isArray()) {
                ParamInfo info = ParamInfo.builder()
                        .name(name)
                        .javaType(genericType.getTypeName())
                        .jsonType(JsonType.ARRAY)
                        .params(new ArrayList<>())
                        .build();
                list.add(info);
                parseByGenericType(clazz.getComponentType(), NO_NAME, info.getParams(), parsedPojoSet, enableCircularReferenceDetect);
            } else if (!JavaType.isSimpleType(clazz)) {
                boolean isCollection = JavaType.isCollection(clazz);
                boolean alreadyParsed = parsedPojoSet.contains(clazz);
                if (alreadyParsed || isCollection) {
                    String javaType = alreadyParsed ? REF + genericType.getTypeName() : genericType.getTypeName();
                    list.add(ParamInfo.builder()
                            .name(name)
                            .javaType(javaType)
                            .jsonType(isCollection ? JsonType.ARRAY : JsonType.OBJECT)
                            .build());
                    return;
                }

                ParamInfo info = ParamInfo.builder()
                        .name(name)
                        .javaType(genericType.getTypeName())
                        .jsonType(JsonType.OBJECT)
                        .params(new ArrayList<>())
                        .build();
                list.add(info);

                if (enableCircularReferenceDetect && isPojo(clazz)) {
                    parsedPojoSet.add(clazz);
                }

                for (Field declaredField : clazz.getDeclaredFields()) {
                    if (declaredField.getName().startsWith("this$")) {
                        continue;
                    }
                    doParseObject(declaredField.getType(), declaredField, info.getParams(), parsedPojoSet, enableCircularReferenceDetect);
                }
            } else {
                doParseObject(clazz, null, list, parsedPojoSet, enableCircularReferenceDetect);
            }
        }
    }

    private static boolean isPojo(Class<?> type) {
        return !(JavaType.isSimpleType(type)
                || Object.class.equals(type)
                || JavaType.isCollection(type)
                || JavaType.isMap(type));
    }

    /**
     * API参数实例化
     *
     * @param params API参数
     * @return 实例化的Json字符串
     */
    public static String createJson(List<ParamInfo> params) {
        Map<String, Object> cache = new HashMap<>();
        if (params.size() == 1 && JsonType.ARRAY.equals(params.get(0).getJsonType())) {
            return JsonUtils.toPrettyString(createArray(params.get(0), cache));
        } else {
            return JsonUtils.toPrettyString(createObject(params, cache));
        }
    }

    private static Object createObject(List<ParamInfo> params, Map<String, Object> cache) {
        if (params == null || params.isEmpty()) {
            return new Object();
        }

        if (params.size() == 1) {
            ParamInfo param = params.get(0);
            switch (param.getJsonType()) {
                case JsonType.STRING:
                    return "string";
                case JsonType.NUMBER:
                    return 0;
                case JsonType.BOOLEAN:
                    return false;
                case JsonType.NULL:
                    return "null";
                case JsonType.UNSUPPORTED:
                    return param.getJavaType();
                default:
                    break;
            }
        }

        Map<String, Object> map = new HashMap<>();

        for (ParamInfo param : params) {
            String name = param.getName();
            if (param.getJavaType().startsWith(REF)) {
                map.put(name, cache.get(getTypeName(param.getJavaType())));
                continue;
            }

            switch (param.getJsonType()) {
                case JsonType.STRING -> map.put(name, "string");
                case JsonType.NUMBER -> map.put(name, 0);
                case JsonType.BOOLEAN -> map.put(name, false);
                case JsonType.NULL -> map.put(name, "null");
                case JsonType.OBJECT -> {
                    Object subObj = createObject(param.getParams(), cache);
                    cache.put(param.getJavaType(), subObj);
                    map.put(name, subObj);
                }
                case JsonType.ARRAY -> map.put(name, createArray(param, cache));
                case JsonType.UNSUPPORTED -> map.put(name, param.getJavaType());
            }
        }
        return map;
    }

    private static Object createArray(ParamInfo param, Map<String, Object> cache) {
        if (JsonType.ARRAY.equals(param.getJsonType())) {
            List<Object> list = new ArrayList<>();
            for (ParamInfo subParam : param.getParams()) {
                list.add(createArray(subParam, cache));
            }
            return list;
        }
        if (param.getJavaType().startsWith(REF)) {
            return cache.get(getTypeName(param.getJavaType()));
        }
        return createObject(param.getParams(), cache);
    }

    private static String getTypeName(String refType) {
        return refType.substring(REF.length());
    }
}
