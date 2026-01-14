package com.github.apiscan.util;

import org.apache.maven.plugin.logging.Log;

public class LogUtils {
    private static Log log;

    public static void setLog(Log log) {
        LogUtils.log = log;
    }

    public static void debug(CharSequence cs) {
        log.debug(cs);
    }

    public static void info(CharSequence cs) {
        log.info(cs);
    }

    public static void warn(CharSequence cs) {
        log.warn(cs);
    }

    public static void error(CharSequence cs) {
        log.error(cs);
    }
}
