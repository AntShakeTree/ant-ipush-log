package com.ant.ipush.http;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class HttpRes {
    public static HttpRes SUCCESS = new HttpRes(0, "");
    final int error;
    final String message;

    public boolean isError() {
        return error != 0;
    }
}
