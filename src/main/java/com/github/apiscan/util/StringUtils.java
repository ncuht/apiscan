package com.github.apiscan.util;

/**
 * 字符串通用类
 */
public class StringUtils {
    /**
     * 是空字符串判断
     *
     * @param str 字符串
     * @return true-空字符串/flase-非空字符串
     */
    public static boolean isBlank(String str) {
        return !isNotBlank(str);
    }

    /**
     * 非空字符串判断
     *
     * @param str 字符串
     * @return true-非空字符串/flase-空字符串
     */
    public static boolean isNotBlank(String str) {
        return str != null && !str.isEmpty() && containsText(str);
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static String formatUrl(String url) {
        if (isBlank(url)) {
            return "";
        }
        return url.replaceAll("/+", "/").trim();
    }
}
