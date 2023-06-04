package com.register.server.netty.handler;

import com.register.agent.req.InnerRequest;
import com.register.agent.req.InnerResponse;
import com.register.agent.req.InnerResponseV2;
import com.register.server.web.handler.ResponseHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class ContextInHandleV2 extends ServerInHandleAdapter<InnerResponseV2> {

    @Override
    protected void serverRead(ChannelHandlerContext ctx, InnerResponseV2 msg) {
        ResponseHandler.handleResponse(msg, this);
    }

    @Override
    public void reverserResponse(InnerRequest request, InnerResponseV2 msg, HttpServletResponse response) throws IOException {
        // 设置响应状态码
        response.setStatus(msg.getStatus());
        // 设置响应Headers
        if(msg.getHeaderMap() != null){
            msg.getHeaderMap().forEach((key, value) -> {
                response.setHeader(key, value.replace(request.getServerHost(), request.getOriginHost()));
            });
        }
        response.getOutputStream().write(msg.getContent().replace(request.getServerHost(), request.getOriginHost()).getBytes());
        response.flushBuffer();
    }

}