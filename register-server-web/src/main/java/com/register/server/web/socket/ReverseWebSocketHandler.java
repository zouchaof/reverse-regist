package com.register.server.web.socket;

import com.alibaba.fastjson.JSONObject;
import com.register.agent.req.InnerRequest;
import com.register.agent.req.InnerResponseV2;
import com.register.agent.req.RegisterAgentInfo;
import com.register.server.core.RegisterAgentFactory;
import com.register.server.core.ReverserResponseHandler;
import com.register.server.web.handler.ResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class ReverseWebSocketHandler extends TextWebSocketHandler implements ReverserResponseHandler<InnerResponseV2> {


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket 连接已关闭：{}", session.getId());
        RegisterAgentFactory.removeRegisterAgent(session);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket 连接已建立：{}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String text  = message.getPayload();
        log.info("收到 WebSocket 消息：{}", text);

        JSONObject jsonObject = JSONObject.parseObject(text);
        if("browser".equals(jsonObject.getString("registerType"))){
            RegisterAgentInfo agentInfo = JSONObject.parseObject(text, RegisterAgentInfo.class);
            RegisterAgentFactory.registerAgent(session, agentInfo);
            return;
        }
        InnerResponseV2 responseV2 = JSONObject.parseObject(text, InnerResponseV2.class);
        ResponseHandler.handleResponse(responseV2, this);
//        session.sendMessage(new TextMessage("服务器已收到消息1111：" + message.getPayload()));
    }

    @Override
    public void reverserResponse(InnerRequest request, InnerResponseV2 msg, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().write(msg.getContent());
    }
}