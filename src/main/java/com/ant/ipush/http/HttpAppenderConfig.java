package com.ant.ipush.http;

import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.spi.AppenderAttachable;
import com.ant.ipush.keying.KeyingStrategy;
import com.ant.ipush.keying.NoKeyKeyingStrategy;
import lombok.Getter;
import lombok.Setter;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;


@Getter
@Setter
public abstract class HttpAppenderConfig<E> extends UnsynchronizedAppenderBase<E> implements AppenderAttachable<E> {
    protected final static String TOKEN = "Token";
    protected String accessKey;
    protected String secretKey;
    protected String secretKeyPrefix = "QWJGBVDSEDFGHJNBRYYUT";

    protected KeyingStrategy<? super E> keyingStrategy = null;
    protected String server;
    protected String modelName;
    protected int retryTime;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public int getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(int retryTime) {
        this.retryTime = retryTime;
    }

    protected String ip;
    protected String token;
    protected Encoder<E> encoder;


    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return "";
    }

    protected boolean checkPrerequisites() {
        boolean errorFree = true;

//        if (modelName == null) {
//            addError("No \"" + modelName + "\" set for the appender named [\""
//                    + name + "\"].");
//            errorFree = false;
//        }
        if (retryTime == 0) {
            retryTime = 10;
        }

        if (accessKey == null) {
            addError("No accessKey set for the appender named [\"" + name + "\"].");
            errorFree = false;
        }

        if (secretKey == null) {
            addError("No secretKey set for the appender named [\"" + name + "\"].");
            errorFree = false;
        }
        if (server == null) {
            addError("No server set for the appender named [\"" + server + "\"].");
            errorFree = false;
        }

        if (ip == null) {
            ip = getIpAddress();
            if ("".equals(ip)) {
                addError("No secretKey set for the appender named [\"" + ip + "\"].");
                return false;
            }
        }

        if (token == null) {
            addInfo("No  token set for the appender named [\"" + token + "\"]. ");
            return false;
        }
        if (keyingStrategy == null) {
            addInfo("No  keyingStrategy set for the appender named [\"" + keyingStrategy + "\"]. ");
            keyingStrategy = new NoKeyKeyingStrategy();
        }

        return errorFree;
    }




}
