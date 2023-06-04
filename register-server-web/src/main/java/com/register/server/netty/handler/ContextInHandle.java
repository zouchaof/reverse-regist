package com.register.server.netty.handler;

import com.register.agent.req.BaseMessage;
import com.register.agent.req.InnerRequest;
import com.register.agent.req.InnerResponse;
import com.register.server.web.handler.ResponseHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class ContextInHandle extends ServerInHandleAdapter<InnerResponse> {

    @Override
    protected void serverRead(ChannelHandlerContext ctx, InnerResponse msg) {
        ResponseHandler.handleResponse(msg, this);
    }

    @Override
    public void reverserResponse(InnerRequest request, InnerResponse msg, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().write(msg.getContent());
    }
} 