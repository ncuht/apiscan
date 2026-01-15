package com.github.apiscan.util;

import org.apache.maven.plugin.logging.Log;

/**
 * 日志打印
 */
public class LogUtils {
    private static Log log;

    /**
     * 设置Log对象
     *
     * @param log Log对象
     */
    public static void setLog(Log log) {
        LogUtils.log = log;
    }

    /**
     * debug打印
     *
     * @param cs 日志内容
     */
    public static void debug(CharSequence cs) {
        log.debug(cs);
    }

    /**
     * info打印
     *
     * @param cs 日志内容
     */
    public static void info(CharSequence cs) {
        log.info(cs);
    }

    /**
     * warn打印
     *
     * @param cs 日志内容
     */
    public static void warn(CharSequence cs) {
        log.warn(cs);
    }

    /**
     * error打印
     *
     * @param cs 日志内容
     */
    public static void error(CharSequence cs) {
        log.error(cs);
    }
}
