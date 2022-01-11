package com.ant.ipush.http;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * http工具类
 */
public class HttpUtil {

    private static final int SIZE = 1024 * 1024;
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;
    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE_TEXT_JSON = "text/json";
    private final CloseableHttpClient httpclient;
    private final RequestConfig requestDefaultConfig = RequestConfig.custom().setSocketTimeout(3000)
            .setConnectTimeout(1000).setConnectionRequestTimeout(1000).build();

    private HttpUtil() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        // 将最大连接数增加到600
        cm.setMaxTotal(600);
        // 将每个路由基础的连接增加到200
        cm.setDefaultMaxPerRoute(200);
        cm.setValidateAfterInactivity(3000);
        // 链接超时setConnectTimeout ，读取超时setSocketTimeout
        RequestConfig defaultRequestConfig = null;
        defaultRequestConfig = RequestConfig.custom().setConnectionRequestTimeout(3000).setConnectTimeout(3000)
                .setExpectContinueEnabled(true).setSocketTimeout(3000).build();

        httpclient = HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(defaultRequestConfig)
                .build();

        new IdleConnectionMonitorThread(cm).start();
    }

    public static HttpUtil getIntance() {
        return HttpUtilHolder.INSTANCE;
    }

    public HttpRes send(String url, String payload, Map<String, Object> header) {
        System.out.println(payload + "===========>" + JSON.toJSONString(header));
        return HttpRes.SUCCESS;
    }

    /**
     * 编码默认UTF-8
     *
     * @param url
     * @return
     */
    public String get(String url) {
        return this.get(url, CHARSET_UTF8.toString());
    }

    /**
     * 重试机制的请求url
     *
     * @return
     */
    public String getWithRetry(String url, int index, int times) {
        if (index >= times) {
            return null;
        }
        try {
            String res = this.get(url);
            if (res == null) {
                index++;
                return this.getWithRetry(url, index, times);
            }
            return res;
        } catch (Exception e) {
            index++;
            return this.getWithRetry(url, index, times);
        }
    }

    /**
     * @param url
     * @param code
     * @return
     */
    public String get(String url, final String code) {
        String res = null;

        try {
            HttpGet httpget = new HttpGet(url);
            res = doGet(httpget, code);
        } catch (Exception e) {
            logger.error("url:{}", url, e);
        }
        return res;
    }

    /**
     * 执行get请求
     *
     * @param httpGet
     * @param code
     * @return
     */
    private String doGet(HttpGet httpGet, final String code) throws IOException {
        ResponseHandler<String> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, code) : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };

        return httpclient.execute(httpGet, responseHandler);
    }

    /**
     * get请求 包含header 默认utf-8
     *
     * @param url
     * @param header
     * @return
     */
    public HttpRes getHeader(String url, Map<String, Object> header) {
        return getHeader(url, header, "UTF-8");
    }

    /**
     * get请求 包含header 默认utf-8
     *
     * @param url
     * @param code
     * @return
     */
    public HttpRes getHeader(String url, Map<String, Object> header, final String code) {
        String res = null;

        try {
            HttpGet httpget = new HttpGet(url);
            if (header != null) {
                header.forEach((k, v) -> httpget.addHeader(k, v == null ? "" : v.toString()));
            }
            res = doGet(httpget, code);
        } catch (Exception e) {
            return HttpRes.builder().message(e.getMessage()).error(-1).build();
        }
        return HttpRes.SUCCESS;
    }

    public List<String> getList(String url) {
        List<String> res = null;
        try {
            HttpGet httpget = new HttpGet(url);
            ResponseHandler<List<String>> responseHandler = new ResponseHandler<List<String>>() {
                @Override
                public List<String> handleResponse(final HttpResponse response)
                        throws IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        List<String> result = new ArrayList<String>();
                        HttpEntity entity = response.getEntity();
                        if (entity == null) {
                            return result;
                        }
                        BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()), SIZE);
                        while (true) {
                            String line = in.readLine();
                            if (line == null) {
                                break;
                            } else {
                                result.add(line);
                            }
                        }
                        in.close();
                        return result;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            res = httpclient.execute(httpget, responseHandler);
        } catch (Exception e) {
            logger.error(url, e);
        }
        return res;
    }

    /**
     * 编码默认UTF-8
     *
     * @param url
     * @param json
     */
    public String postJSON(String url, String json) {
        return this.postJSON(url, json, CHARSET_UTF8.toString());
    }

    /**
     * 编码默认UTF-8
     *
     * @param url
     * @param json
     */
    public String postJSON(String url, String json, Map<String, ?> header) {
        return this.postJSON(url, json, CHARSET_UTF8.toString(), header);
    }

    /**
     * @param url
     * @param header
     * @param params
     * @param code
     * @return
     */
    private String post(String url, Map<String, ?> header, List<NameValuePair> params, String code) {
        String res = null;
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            if (params != null) {
                httpPost.setEntity(new UrlEncodedFormEntity(params, code));
            }

            if (header != null) {
                header.forEach((k, v) -> httpPost.addHeader(k, v == null ? "" : v.toString()));
            }

            response = httpclient.execute(httpPost);
            HttpEntity entity2 = response.getEntity();
            res = EntityUtils.toString(entity2, code);
            EntityUtils.consume(entity2);
        } catch (Exception e) {
            logger.error(url, e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                }
            }
        }
        return res;
    }

    public String postJSON(String url, String json, String code) {
        String res = null;
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);

            StringEntity se = new StringEntity(json);
            se.setContentType(CONTENT_TYPE_TEXT_JSON);
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
            httpPost.setEntity(se);
            response = httpclient.execute(httpPost);
            HttpEntity entity2 = response.getEntity();
            res = EntityUtils.toString(entity2, code);
            EntityUtils.consume(entity2);
        } catch (Exception e) {
            logger.error(url, e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                }
            }
        }
        return res;
    }

    public String postJSON(String url, String json, String code, Map<String, ?> header) {
        String res = null;
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
            if (header != null) {
                header.forEach((k, v) -> httpPost.addHeader(k, v == null ? "" : v.toString()));
            }

            StringEntity se = new StringEntity(json);
            se.setContentType(CONTENT_TYPE_TEXT_JSON);
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
            httpPost.setEntity(se);
            response = httpclient.execute(httpPost);
            HttpEntity entity2 = response.getEntity();
            res = EntityUtils.toString(entity2, code);
            EntityUtils.consume(entity2);
        } catch (Exception e) {
            logger.error(url, e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                }
            }
        }
        return res;
    }

    /**
     * 默认UTF-8
     *
     * @param url
     * @param params
     * @return
     */
    public String post(String url, Map<String, ?> params) {
        return this.post(url, params, CHARSET_UTF8.toString());
    }

    /**
     * 默认UTF-8
     *
     * @param url
     * @param params
     * @param header
     * @return
     */
    public String postHeader(String url, Map<String, ?> header, Map<String, ?> params) {
        return this.post(url, header, params, CHARSET_UTF8.toString());
    }

    /**
     * 带有header的post请求
     *
     * @param url
     * @param header
     * @param params
     * @param code
     * @return
     */
    private String post(String url, Map<String, ?> header, Map<String, ?> params, String code) {
        List<NameValuePair> nvps = null;
        if (params != null && params.size() > 0) {
            nvps = Lists.newArrayList();
            for (Entry<String, ?> entry : params.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
        }
        return this.post(url, header, nvps, code);
    }

    /**
     * @param url
     * @param params
     * @param code
     * @return
     */
    public String post(String url, Map<String, ?> params, String code) {
        List<NameValuePair> nvps = null;
        if (params != null && params.size() > 0) {
            nvps = Lists.newArrayList();
            for (Entry<String, ?> entry : params.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(),
                        entry.getValue() == null ? "" : entry.getValue().toString()));
            }
        }
        return this.post(url, null, nvps, code);
    }

    public String postBody(String url, String body) {
        String res = null;
        try {
            HttpPost httppost = new HttpPost(url);
            if (body != null && !"".equals(body)) {
                httppost.setEntity(new StringEntity(body, CHARSET_UTF8));
            }
            CloseableHttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                res = EntityUtils.toString(resEntity, CHARSET_UTF8);
                EntityUtils.consume(resEntity);
            }
        } catch (Exception e) {
            logger.error(url, e);
        }

        return res;
    }

    /**
     * 默认编码UTF-8
     *
     * @param url
     * @Date 2017/8/8
     */
    public String sendGet(String url) {
        return this.sendGet(url, CHARSET_UTF8.toString());
    }

    /**
     * get请求，不使用连接池,
     *
     * @param url
     * @param code
     * @Date 2017/8/8
     */
    public String sendGet(String url, String code) {

        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        String responseContent = null;
        try {
            // 创建默认的httpClient实例.
            httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestDefaultConfig);
            // 执行请求
            response = httpClient.execute(httpGet);
            entity = response.getEntity();
            responseContent = EntityUtils.toString(entity, code);
        } catch (Exception e) {
            logger.error(url, e);
        } finally {
            // 关闭连接
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return responseContent;
    }

    /**
     * 默认编码UTF-8
     *
     * @param url
     * @param json
     * @Date 2017/8/8
     */
    public String sendPost(String url, String json) {
        return this.sendPost(url, json, CHARSET_UTF8.toString());
    }

    /**
     * post请求, 不使用连接池
     *
     * @param url
     * @param json
     * @Date 2017/8/8
     */
    public String sendPost(String url, String json, String code) {

        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String responseContent = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestDefaultConfig);
            httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
            StringEntity se = new StringEntity(json);
            se.setContentType(CONTENT_TYPE_TEXT_JSON);
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
            httpPost.setEntity(se);
            httpClient = HttpClients.createDefault();
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            responseContent = EntityUtils.toString(entity, code);
            EntityUtils.consume(entity);
        } catch (Exception e) {
            logger.error(url, e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return responseContent;
    }

    private static class HttpUtilHolder {
        private static final HttpUtil INSTANCE = new HttpUtil();
    }

    // 监控有异常的链接
    private static class IdleConnectionMonitorThread extends Thread {

        private final HttpClientConnectionManager connMgr;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        // 关闭失效的连接
                        connMgr.closeExpiredConnections();
                        // 可选的, 关闭30秒内不活动的连接
                        connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}