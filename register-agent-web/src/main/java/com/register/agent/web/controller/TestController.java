package com.register.agent.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequestMapping
public class TestController {

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(9999));

    private boolean b = false;


    @RequestMapping("timer")
    @ResponseBody
    public String timer(){
        log.info("timer start...");
        b = true;
        if(executor.getTaskCount() > 0){
            return "has begin";
        }
        executor.execute(() -> {
            while (b){
                log.info("executor log print start...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        return "success2";
    }

    @RequestMapping("timerStop")
    @ResponseBody
    public String timerStop(HttpServletResponse response) {
        log.info("timer stop...");
        b = false;
        response.setIntHeader("test",2);
        return "timer stop";
    }

    @RequestMapping("timerR")
    public void timerR(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("timer stop...");
        b = false;
        response.setIntHeader("test",1);

        response.addCookie(new Cookie("test", "2"));
//        request.getRequestDispatcher("/").
        response.sendRedirect("timerStop");
//        return "timer stop";
    }


    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public void testMethod(HttpServletRequest request, HttpServletResponse response) {
        String targetUrl = "https://www.baidu.com/s?wd=1";
        HttpGet httpGet = new HttpGet(targetUrl);
        httpGet.setConfig(RequestConfig.custom().setRedirectsEnabled(false).build());
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(httpGet);
            int statusCode = httpResponse.getStatusLine().getStatusCode();

            // 设置响应状态码
            response.setStatus(statusCode);

            // 设置响应Headers
            for (Header header : httpResponse.getAllHeaders()) {
                response.setHeader(header.getName(), header.getValue());
            }

            // 将响应Entity写入输出流
            HttpEntity entity = httpResponse.getEntity();
//            InputStream inputStream = entity.getContent();
//            IOUtils.copy(inputStream, response.getOutputStream());

            response.getOutputStream().write(EntityUtils.toByteArray(entity));

            response.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpGet.releaseConnection();
            IOUtils.closeQuietly(httpResponse);
            IOUtils.closeQuietly(httpClient);
        }
    }

}
