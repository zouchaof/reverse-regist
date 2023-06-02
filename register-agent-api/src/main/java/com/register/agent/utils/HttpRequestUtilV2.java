package com.register.agent.utils;

import com.register.agent.req.InnerRequest;
import com.register.agent.req.InnerResponseV2;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestUtilV2 {

    private static Logger log = LoggerFactory.getLogger(HttpRequestUtilV2.class);
    private static final int connectionTimeout = 20*1000;
    private static final int connectionRequestTimeout = 10*1000;
    private static final int soTimeout = 20*1000;

    private static PoolingHttpClientConnectionManager poolConnManager;
    private static final int maxTotalPool = 200;
    private static final int maxConPerRoute = 20;

    private static final String DEFAULT_CHARSET = "utf-8";

    static{
        init();
    }

    private static void init(){
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                //信任所有
                public boolean isTrusted(X509Certificate[] chain,
                                         String authType) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslsf)
                    .build();
            poolConnManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            // Increase max total connection to 200
            poolConnManager.setMaxTotal(maxTotalPool);
            // Increase default max connection per route to 20
            poolConnManager.setDefaultMaxPerRoute(maxConPerRoute);
            SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(soTimeout).build();
            poolConnManager.setDefaultSocketConfig(socketConfig);
        } catch (Exception e) {
            log.error("鏈接池初始化異常", e);
        }
    }

    public static CloseableHttpClient getHttpClient(){
        RequestConfig config = RequestConfig.custom()
                .setSocketTimeout(soTimeout)
                .setConnectTimeout(connectionTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setRedirectsEnabled(false)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(poolConnManager).setDefaultRequestConfig(config).build();
        if(poolConnManager!=null&&poolConnManager.getTotalStats()!=null){
            log.info("now client pool "+poolConnManager.getTotalStats().toString());
        }
        return httpClient;
    }


    public static String postMethod(InnerResponseV2 innerRespone, String url, Map<String,String> paramsMap,
                                    String jsonParam, Map<String, String> headerMap){
        String html = "";
        CloseableHttpClient httpClient = getHttpClient();
        if(httpClient == null){
            return html;
        }
        CloseableHttpResponse httpResponse = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            if(headerMap!=null){
                headerMap.forEach(httpPost::addHeader);
                httpPost.removeHeaders("Transfer-Encoding");
                httpPost.removeHeaders("Content-Length");
            }
            if(StringUtils.isNotBlank(jsonParam)){
                StringEntity se = new StringEntity(jsonParam, DEFAULT_CHARSET);
                httpPost.setEntity(se);
            } else if(paramsMap!=null){
                List<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
                paramsMap.forEach((key, value) -> params.add(new BasicNameValuePair(key, value)));
                if(params.size() > 0){
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, DEFAULT_CHARSET);
                    httpPost.setEntity(entity);
                }
            }
            httpResponse = httpClient.execute(httpPost);

            parseHttpResponse(httpResponse, innerRespone);

            return html;
        } catch (Exception e) {
            log.warn(HttpRequestUtilV2.class.getName() + " postMethod error:", e);
        } finally {
            try {
                if(httpResponse!=null){
                    httpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return html;

    }
    /**
     * get请求方法
     * @return
     */
    public static String getMethod(InnerResponseV2 innerRespone, String url, Map<String,String> paramMap, Map<String, String> headerMap){
        String html = "";
        CloseableHttpClient httpClient = getHttpClient();
        if(httpClient == null){
            return html;
        }
        CloseableHttpResponse httpResponse = null;
        try {
            List<NameValuePair> params = new ArrayList<>();
            if(paramMap != null){
                paramMap.forEach((key, value) -> params.add(new BasicNameValuePair(key, value)));
            }
            String queryString = URLEncodedUtils.format(params, DEFAULT_CHARSET);
            URI requestUri = URI.create(url + "?" + queryString);

            HttpGet httpGet = new HttpGet(requestUri);
            if(headerMap!=null){
                headerMap.forEach(httpGet::addHeader);
            }

            httpResponse = httpClient.execute(httpGet);

            parseHttpResponse(httpResponse, innerRespone);

            httpGet.abort();
        } catch (Exception e) {
            log.warn("访问地址" + url + "时报错", e);
        } finally {
            try {
                if(httpResponse!=null){
                    httpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return html;
    }





    private static void parseHttpResponse(CloseableHttpResponse httpResponse, InnerResponseV2 innerRespone) throws IOException {
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        innerRespone.setStatus(statusCode);

        Map<String, String> headerMap = new HashMap<>();
        for (Header header : httpResponse.getAllHeaders()) {
            headerMap.put(header.getName(), header.getValue());
        }
        HttpEntity entity = httpResponse.getEntity();
        innerRespone.setOutBytes(EntityUtils.toByteArray(entity));
    }

    public static void invokeRequest(InnerRequest request, InnerResponseV2 respone) {
        if(StringUtils.isBlank(request.getMethod()) || StringUtils.isBlank(request.getUrl())){
            return;
        }
        Map<String, String> headMap = request.getHeadMap();
        headMap.remove("host");
        //先只支持get,post，其他的都发post
        if(request.getMethod().equalsIgnoreCase("GET")){
            getMethod(respone, request.getUrl(), request.getParamsMap(), headMap);
        }
        postMethod(respone, request.getUrl(), request.getParamsMap(), request.getJsonParam(), headMap);
    }






}
