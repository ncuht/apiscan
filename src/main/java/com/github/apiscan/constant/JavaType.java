package com.github.apiscan.constant;

import com.github.apiscan.util.LogUtils;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Java类型
 */
public class JavaType {
    private static final Set<Class<?>> SIMPLE_TYPES = Set.of(
            Boolean.class,
            Byte.class,
            Character.class,
            Double.class,
            Float.class,
            Integer.class,
            Long.class,
            Short.class,
            Void.class,
            Void.TYPE,

            URI.class,
            URL.class,
            UUID.class,
            Locale.class,
            Pattern.class,
            Class.class);

    private static final Set<Class<?>> SIMPLE_TYPE_ASSIGNABLE_FROMS = Set.of(
            Enum.class,
            CharSequence.class,
            Number.class,
            Date.class,
            Temporal.class,
            ZoneId.class,
            TimeZone.class,
            File.class,
            Path.class,
            Charset.class,
            Currency.class,
            InetAddress.class,

            InputStream.class,
            OutputStream.class,
            ServletRequest.class,
            ServletResponse.class);

    public static Map<String, String> JAVA_JSON_TYPE_MAP = new HashMap<>();

    private static Method isSimpleValueType = null;

    /**
     * 获取spring的BeanUtils
     *
     * @param classLoader classLoader
     */
    public static void setBeanUtils(ClassLoader classLoader) {
        try {
            Class<?> beanUtils = classLoader.loadClass("org.springframework.beans.BeanUtils");
            isSimpleValueType = beanUtils.getMethod("isSimpleValueType", Class.class);
        } catch (ReflectiveOperationException exception) {
            LogUtils.warn("获取Spring的简单类型判断方法出错：" + exception.getMessage());
            throw new RuntimeException(exception);
        }
    }

    static {
        JAVA_JSON_TYPE_MAP.put("byte", JsonType.NUMBER);
        JAVA_JSON_TYPE_MAP.put("short", JsonType.NUMBER);
        JAVA_JSON_TYPE_MAP.put("int", JsonType.NUMBER);
        JAVA_JSON_TYPE_MAP.put("long", JsonType.NUMBER);
        JAVA_JSON_TYPE_MAP.put("char", JsonType.STRING);
        JAVA_JSON_TYPE_MAP.put("boolean", JsonType.BOOLEAN);
        JAVA_JSON_TYPE_MAP.put("float", JsonType.NUMBER);
        JAVA_JSON_TYPE_MAP.put("double", JsonType.NUMBER);

        JAVA_JSON_TYPE_MAP.put("java.lang.Byte", JsonType.NUMBER);
        JAVA_JSON_TYPE_MAP.put("java.lang.Short", JsonType.NUMBER);
        JAVA_JSON_TYPE_MAP.put("java.lang.Integer", JsonType.NUMBER);
        JAVA_JSON_TYPE_MAP.put("java.lang.Long", JsonType.NUMBER);
        JAVA_JSON_TYPE_MAP.put("java.lang.Character", JsonType.STRING);
        JAVA_JSON_TYPE_MAP.put("java.lang.Boolean", JsonType.BOOLEAN);
        JAVA_JSON_TYPE_MAP.put("java.lang.Float", JsonType.NUMBER);
        JAVA_JSON_TYPE_MAP.put("java.lang.Double", JsonType.NUMBER);

        JAVA_JSON_TYPE_MAP.put("java.lang.String", JsonType.STRING);

        JAVA_JSON_TYPE_MAP.put("java.math.BigInteger", JsonType.NUMBER);
        JAVA_JSON_TYPE_MAP.put("java.math.BigDecimal", JsonType.NUMBER);
    }

    /**
     * 判断是否是基本类型
     *
     * @param clazz 类class
     * @return true=基本类型/false=包装类or对象
     */
    public static boolean isBaseType(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return clazz.isPrimitive();
    }

    /**
     * 判断是否是简单类型
     *
     * @param clazz 类class
     * @return true=简单类型/false=复杂类型
     */
    public static boolean isSimpleType(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        useSpring:
        {
            // 使用springboot自带的判断器
            if (isSimpleValueType == null) {
                break useSpring;
            }
            try {
                Boolean simplePredicate = (Boolean) isSimpleValueType.invoke(null, clazz);
                if (simplePredicate == null) {
                    break useSpring;
                }
                return simplePredicate;
            } catch (IllegalAccessException | InvocationTargetException exception) {
                LogUtils.warn("useSpring异常：" + exception.getMessage());
                throw new RuntimeException(exception);
            }
        }
        // 使用兜底逻辑判断
        if (clazz.isPrimitive() || SIMPLE_TYPES.contains(clazz)) {
            return true;
        }
        for (Class<?> assignableFrom : SIMPLE_TYPE_ASSIGNABLE_FROMS) {
            if (assignableFrom.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回java类型对应的json类型
     *
     * @param clazz 类class
     * @return json类型
     */
    public static String getJsonType(Class<?> clazz) {
        if (clazz == null) {
            return JsonType.NULL;
        }
        if (isMap(clazz)) {
            return JsonType.OBJECT;
        }
        if (isCollection(clazz) || clazz.isArray()) {
            return JsonType.ARRAY;
        }
        String jsonType = JAVA_JSON_TYPE_MAP.get(clazz.getName());
        if (jsonType != null) {
            return jsonType;
        }
        if (Number.class.isAssignableFrom(clazz)) {
            return JsonType.NUMBER;
        }
        if (Boolean.class.isAssignableFrom(clazz)) {
            return JsonType.BOOLEAN;
        }
        if (InputStream.class.isAssignableFrom(clazz)
                || OutputStream.class.isAssignableFrom(clazz)
                || ServletRequest.class.isAssignableFrom(clazz)
                || ServletResponse.class.isAssignableFrom(clazz)) {
            return JsonType.UNSUPPORTED;
        }
        return isSimpleType(clazz) ? JsonType.STRING : JsonType.OBJECT;
    }

    /**
     * 判断是否是Map
     *
     * @param clazz 类class
     * @return true=是Map/false=不是Map
     */
    public static boolean isMap(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return Map.class.isAssignableFrom(clazz);
    }

    /**
     * 判断是否是Collection
     *
     * @param clazz 类class
     * @return true=是Collection/false=不是Collection
     */
    public static boolean isCollection(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return Collection.class.isAssignableFrom(clazz);
    }

    /**
     * 判断是否是ServletRequest
     *
     * @param clazz 类class
     * @return true=是ServletRequest/false=不是ServletRequest
     */
    public static boolean isServlet(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return ServletRequest.class.isAssignableFrom(clazz) || ServletResponse.class.isAssignableFrom(clazz);
    }
}
