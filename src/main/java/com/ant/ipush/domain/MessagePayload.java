package com.ant.ipush.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessagePayload implements Serializable {
    private static final long serialVersionUID = -8296350547944518344L;
    private String event;
    private String scene;
    private String biz;
    private String ds;
    private String topicName;
    private long ts;
}
