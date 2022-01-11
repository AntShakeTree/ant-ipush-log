package com.ant.ipush.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class BaseLogger {
    /**
     * 埋点事件触发事件（YYYYMMDD）
     */
    String ds;

    /**
     * 触发事件毫秒级时间戳
     */
    String ts;

    /**
     * 事件类型
     * @deprecated
     */
    @JSONField(name="aType")
    String aType;

    /**
     * 事件类型
     */
    String event;

    /**
     * 事件行为体
     */
    String entity;

    /**
     * 事件渠道
     */
    String channel;
}
