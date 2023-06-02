package com.register.server.netty.handler;

import com.register.agent.req.InnerResponse;
import com.register.agent.req.InnerResponseV2;
import com.register.server.web.handler.ResponseHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContextInHandleV2 extends ServerInHandleAdapter<InnerResponse> {

    @Override
    protected void serverRead(ChannelHandlerContext ctx, InnerResponseV2 msg) {
        ResponseHandler.handleResponse(msg);
    }

}