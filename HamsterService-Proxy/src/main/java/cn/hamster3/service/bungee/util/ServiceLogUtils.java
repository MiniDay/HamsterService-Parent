package cn.hamster3.service.bungee.util;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public abstract class ServiceLogUtils {
    private static Logger logger;

    public static void setLogger(Logger logger) {
        ServiceLogUtils.logger = logger;
    }

    public static void info(String info) {
        logger.info(info);
    }

    public static void info(String info, Object... params) {
        logger.info(String.format(info, params));
    }

    public static void warning(String warning) {
        logger.warning(warning);
    }

    public static void warning(String warning, Object... params) {
        logger.warning(String.format(warning, params));
    }

    public static void error(Throwable e, String message) {
        warning(message);
        e.printStackTrace();
    }

    public static void error(Throwable e, String message, Object... args) {
        warning(message, args);
        e.printStackTrace();
    }
}
