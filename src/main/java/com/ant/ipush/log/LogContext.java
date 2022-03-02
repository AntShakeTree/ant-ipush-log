package com.ant.ipush.log;

//import com.alibaba.fastjson.JSON;

import com.ant.ipush.domain.JSON;
import com.ant.ipush.domain.LogAnalytics;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class LogContext {

    static ThreadLocal<LogContext> LOCAL = new ThreadLocal<>();
    private static LogContext INSTANCE = new LogContext();
//    private LogAnalytics logAnalytics;

    @Getter
    private Map<String, Object> mapper;

//    public LogAnalytics getContent() {
//        return this.logAnalytics;
//    }

    private Logger logger;

    private LogContext() {
    }

    public static LogContext instance() {
        if (LOCAL.get() == null) {
            LOCAL.set(new LogContext());
        }
        return LOCAL.get();
    }

    public static Optional find() {
        return Optional.ofNullable(LOCAL);
    }

    public static void clear() {

        if (LOCAL.get() != null && LOCAL.get().getMapper() != null) {
            LOCAL.get().getMapper().clear();
        }
        LOCAL.remove();
    }


//    public LogContext appender(LogAnalytics logAnalytics) {
//        logAnalytics(BeanSupport.copyProperties(logAnalytics, this.logAnalytics == null ? new LogAnalytics() : this.logAnalytics));
//        return this;
//    }

    public LogContext appender(String key, String value) {
        if (this.mapper == null) {
            mapper(new HashMap<>());
        }
        this.mapper.put(key, value);
        return this;
    }

    public void info() {
        infoMapper();
    }

//    public void info(Object bean) {
//        Objects.requireNonNull(this.logger);
//        if (this.mapper == null) {
//            this.mapper(BeanSupport.convertMapFromBean(bean));
//        }
//        appender("ts", System.currentTimeMillis() + "");
//        appender("ds", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
//        this.infoMapper();
//        LogContext.clear();
//    }

    public void info(String json) {
        Objects.requireNonNull(this.logger);
        this.logger.info(json);
        LogContext.clear();
    }


    public void infoMapper() {
        Objects.requireNonNull(this.logger);
        Objects.requireNonNull(this.mapper);
        if (this.getMapper().get("ts") == null)
            appender("ts", System.currentTimeMillis() + "");
        if (this.getMapper().get("ds") == null)
            appender("ds", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        if (this.getMapper().get("channel") == null) {
            appender("channel", "0000");
        }
        this.logger.info(JSON.toJSONString(JSON.convertMap(this.mapper)));
        LogContext.clear();
    }

    public void warn(String json) {
        Objects.requireNonNull(this.logger);
        this.logger.warn(json);
        LogContext.clear();
    }

    public void warn() {
        Objects.requireNonNull(this.logger);
//        Objects.requireNonNull(this.logAnalytics);
//        this.logger.warn(logAnalytics.toString());
        LogContext.clear();
    }


//    public LogContext logAnalytics(LogAnalytics logAnalytics) {
//        this.logAnalytics = logAnalytics;
//        return this;
//    }

    public LogContext mapper(Map<String, Object> mapper) {
        this.mapper = mapper;
        return this;
    }

    public LogContext log(Logger logger) {
        this.logger = logger;
        return this;
    }

    public LogContext log(String name) {
        this.logger = LoggerFactory.getLogger(name);
        return this;
    }


//    @Override
//    public String toString() {
//        if (logAnalytics == null) return "";
//        return JSON.toJSONString(logAnalytics);
//    }


}