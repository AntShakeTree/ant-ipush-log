package com.ant.ipush.domain;


import com.ant.ipush.log.CachingSupplier;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@ToString
@Data
@NoArgsConstructor
@Slf4j
public class EventMessageData {
    private static final long serialVersionUID = -8296350547944518544L;
    private static Clock clock = Clock.systemUTC();
    private static final Supplier<Instant> timestampSupplier = CachingSupplier.of(clock.instant());


    private Map<String, Object> metaData;
    private MessagePayload payload;
    private String identifier;

    public EventMessageData(MessagePayload payload) {
        this.payload = payload;
    }


    public String getIdentifier() {
        if (this.identifier == null) {
            return UUID.randomUUID().toString();
        }
        return this.identifier;
    }


    public MessagePayload getPayload() {
        if (this.payload != null) {
            if (this.payload.getDs() == null) {
                this.payload.setDs(DateTimeFormatter.BASIC_ISO_DATE.format(LocalDateTime.now()));
            }
            if (this.payload.getTs() == 0) {
                this.payload.setTs(timestampSupplier.get().toEpochMilli());
            }
        }
        return this.payload;
    }


    public Class getPayloadType() {
        return MessagePayload.class;
    }


    public Instant getTimestamp() {
        return timestampSupplier.get();
    }

    public EventMessageData event(String event, String scene) {
        this.getPayload().setEvent(event);
        this.getPayload().setScene(scene == null ? "general" : scene);
        this.getPayload().setBiz(metaData.get("biz") + "");
        this.getPayload().setDs(metaData.get("ds") == null ? LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) : metaData.get("ds").toString());
        this.getPayload().setTs(metaData.get("ts") == null ? timestampSupplier.get().toEpochMilli() : (long) metaData.get("ts"));
        return this;
    }

    public EventMessageData event(String event) {
        this.getPayload().setEvent(event);
        return this;
    }

    public void mergeMetaData(Map<String, Object> map) {
        if (getMetaData() == null) {
            setMetaData(map);
        } else {
            if (map != null && !map.isEmpty()) {
                for (String key : map.keySet()) {
                    if (map.get(key) != null) {
                        getMetaData().put(key, map.get(key));
                    }
                }
            }
        }
    }

//    public static void main(String[] args) {
//        Map<String,Object> map=new HashMap<>();
//        map.put("k","v");
//        map.put("event","aaa");
//        System.out.println(JSON.parseObject(JSON.toJSONString(generalFromMap(map)),EventMessageData.class));

}


