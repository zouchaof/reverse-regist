package com.register.server.web.handler;

import com.register.agent.req.BaseMessage;
import com.register.agent.req.InnerRequest;
import com.register.agent.req.InnerResponse;
import com.register.agent.req.InnerResponseV2;
import com.register.server.core.ReverserResponseHandler;
import com.register.server.netty.handler.ServerInHandleAdapter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@Slf4j
public class ResponseHandler {


    private static final Map<String, SkInnerRequest> listenMap = new ConcurrentHashMap<>();
    private static int reqTimeout = 30;
    private static ResponseHandler handler;

    private static void addListen(InnerRequest request){
        if(handler == null){
            handler = new ResponseHandler();
        }
        SkInnerRequest skInnerRequest = handler.new SkInnerRequest();
        skInnerRequest.setRequest(request);
        listenMap.put(request.getReqId(), skInnerRequest);
        try {
            skInnerRequest.getLatch().await(reqTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("add listen error", e);
        }
    }


    public static void handleResponse(BaseMessage msg,
                                      ReverserResponseHandler adapter){
        SkInnerRequest skRequest = listenMap.get(msg.getReqId());
        if(skRequest == null){
            return;
        }
        skRequest.setHandleAdapter(adapter);
        skRequest.setMsg(msg);
        skRequest.getLatch().countDown();
    }


    public static void parseResponse(InnerRequest request, HttpServletResponse response) throws IOException {
        addListen(request);
        SkInnerRequest skRequest = listenMap.get(request.getReqId());
        if(skRequest == null){
            return;
        }
        skRequest.getHandleAdapter().reverserResponse(skRequest.getRequest(), skRequest.getMsg(), response);
        listenMap.remove(request.getReqId());
    }

    @Data
    private class SkInnerRequest{
        private InnerRequest request;
        private BaseMessage msg;
        private CountDownLatch latch = new CountDownLatch(1);
        private ReverserResponseHandler handleAdapter;
    }


}
