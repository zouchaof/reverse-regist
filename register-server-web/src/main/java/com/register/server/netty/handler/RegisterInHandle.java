package com.register.server.netty.handler;

import com.register.agent.req.RegisterAgentInfo;
import com.register.server.core.RegisterAgentFactory;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegisterInHandle extends ServerInHandleAdapter<RegisterAgentInfo> {

    @Override
    protected void serverRead(ChannelHandlerContext ctx, RegisterAgentInfo msg) {
        RegisterAgentFactory.registerAgent(ctx, msg);
    }


    /**
     * 链接异常时触发
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        RegisterAgentFactory.removeRegisterAgent(ctx);
        super.exceptionCaught(ctx, cause);
    }

    /**
     * 失去链接时触发
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        RegisterAgentFactory.removeRegisterAgent(ctx);
        super.channelInactive(ctx);
    }
}