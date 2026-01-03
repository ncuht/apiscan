package com.github.apiscan.util;

public class StringUtils {
    public static boolean isBlank(String str) {
        return !isNotBlank(str);
    }

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

    public static String formatUrl(String url){
        if(isBlank(url)){
            return "";
        }
        return url.replaceAll("/+","/").trim();
    }
}
