//package com.ant.ipush.log;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//
//public abstract class ILoggerFactory {
////    private static final String LOGGER_NAME = "org";
////    private static final String ORDER = "orders";
////    private static final String MEMBER = "org";
////    private static final String GOODS = "goods";
////
////    static Map<String, Logger> loggerMaps = new ImmutableMap.Builder<String, Logger>().put(
////            ORDER, LoggerFactory.getLogger(ORDER)
////    ).put(MEMBER, LoggerFactory.getLogger(MEMBER)).put(GOODS, LoggerFactory.getLogger(GOODS)).put(LOGGER_NAME, LoggerFactory.getLogger(LOGGER_NAME)).build();
//
//    static Map<String, Logger> loggerMap = new ConcurrentHashMap<>();
//
////    public static void info(LogAnalytics logAnalytics) {
////        LogContext.instance().logAnalytics(logAnalytics).info();
////    }
//
//    public static LogContext create(String name) {
//        if (loggerMap.get(name) == null)
//            loggerMap.put(name, LoggerFactory.getLogger(name));
//        return LogContext.instance().log(loggerMap.get(name));
//    }
//
//    public static LogContext get() {
//        return LogContext.instance();
//    }
//
////    public static void warn(LogAnalytics logAnalytics) {
////        LogContext.instance().logAnalytics(logAnalytics).warn();
////        LogContext.clear();
////    }
//
//
//
//    public static LogContext appender(Map<String,Object> stringMap) {
//        return LogContext.instance().mapper(stringMap);
//    }
//
////    public static <T> LogContext appender(Function<T, LogAnalytics> function, T paramter) {
////        return appender(function.apply(paramter));
////    }
//
//}