package com.ant.ipush.keying;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;

public class EventKeyingStrategy implements KeyingStrategy<ILoggingEvent> {
    @Override
    public byte[] createKey(ILoggingEvent iLoggingEvent) {

        JSONObject atype = JSON.parseObject(iLoggingEvent.getFormattedMessage());
        Object atype_ = atype.get("aType");
        if (atype_ != null) {
            return atype_.toString().getBytes(StandardCharsets.UTF_8);
        } else {
            Object ts = atype.get("ts");
            if (ts != null)
                return ts.toString().getBytes(StandardCharsets.UTF_8);

        }
        return new byte[0];
    }
}
