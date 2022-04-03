package com.ant.ipush.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.ant.ipush.asyn.AsyncKafkaLoggerAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class ILoggerContextFactory {


    private static Map<String, Logger> loggerMap = new ConcurrentHashMap<>();

    public static LogContext create(String name) {
        return LogContext.instance(loggerMap.computeIfAbsent(name, key -> LoggerFactory.getLogger(key)));
    }


    public static LogContext find(String name) {
        if (LogContext.find().isPresent()) {
            return LogContext.find().get();
        } else {
            return create(name);
        }
    }

    public static LogContext find() {
        if (LogContext.find().isPresent()) {
            return LogContext.find().get();
        }
        return loggerWrapper(LoggerFactory.getILoggerFactory().getLogger(Logger.ROOT_LOGGER_NAME));
    }

    public static LogContext loggerWrapper(Logger logger) {
        return LogContext.instance(loggerMap.computeIfAbsent(logger.getName(), key -> wrapperAsynAppender(logger)));
    }
    public static LogContext loggerWrapper(Logger logger,Level level) {
        return LogContext.instance(loggerMap.computeIfAbsent(logger.getName(), key -> wrapperAsynAppender(logger,level)));
    }
    private static Logger wrapperAsynAppender(Logger logger) {
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger logger1 = (ch.qos.logback.classic.Logger) logger;
            AsyncKafkaLoggerAppender loggerAppender=new AsyncKafkaLoggerAppender<>();
            LoggerContext logContext=logger1.getLoggerContext();
            loggerAppender.setContext(logContext);
            logger1.addAppender(loggerAppender);
            return logger1;
        }
        return logger;
    }

    private static Logger wrapperAsynAppender(Logger logger,Level level) {
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger logger1 = (ch.qos.logback.classic.Logger) logger;
            AsyncKafkaLoggerAppender loggerAppender=new AsyncKafkaLoggerAppender<>();
            LoggerContext logContext=logger1.getLoggerContext();
            loggerAppender.setContext(logContext);
            logger1.setLevel(level);
            logger1.addAppender(loggerAppender);
            return logger1;
        }
        return logger;
    }


    public static void main(String[] args) {
        System.out.println(ILoggerContextFactory.create("name").event("event").topic(""));
    }
}