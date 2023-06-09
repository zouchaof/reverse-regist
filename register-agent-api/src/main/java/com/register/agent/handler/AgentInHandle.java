package com.register.agent.handler;

import com.register.agent.core.AgentClientMain;
import com.register.agent.req.InnerRequest;
import com.register.agent.req.InnerResponse;
import com.register.agent.req.InnerResponseV2;
import com.register.agent.req.RegisterAgentInfo;
import com.register.agent.spring.SpringApplicationContextHolder;
import com.register.agent.utils.HttpRequestUtil;
import com.register.agent.utils.HttpRequestUtilV2;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AgentInHandle extends ChannelInboundHandlerAdapter {

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(99999));

    /**
     * Calls {@link ChannelHandlerContext#fireChannelRead(Object)} to forward
     * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * <p>
     * Sub-classes may override this method to change behavior.
     *
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        InnerRequest request = (InnerRequest)msg;
        //读数据绑定单线程，所以真实解析数据返回使用多线程提升性能
        executor.execute(() -> {
            Object obj = execRequest(request);
            //保证数据不错乱，提交netty线程任务来写数据
            ctx.executor().execute(()->{
                ctx.writeAndFlush(obj);
            });
        });
    }

    private Object execRequest(InnerRequest request) {
        //第一版，直接http转
//        InnerResponse response = new InnerResponse();
//        response.setReqId(request.getReqId());
//        response.setContent(HttpRequestUtil.invokeRequest(request));

        //第二版，http，跟随状态的(实现跳转时，很多网站做了跨域验证，有时候跳不过去)
        InnerResponseV2 response = new InnerResponseV2();
        response.setReqId(request.getReqId());
        HttpRequestUtilV2.invokeRequest(request, response);

        return response;
    }

    //客户端连接成功后，向服务器发送数据
    public void channelActive(ChannelHandlerContext ctx) {
        //启动心跳检测任务
        ctx.executor().scheduleAtFixedRate(
                () -> ctx.channel().writeAndFlush(getAgentInfo()),
                0, 30, TimeUnit.SECONDS);
    }

    private RegisterAgentInfo getAgentInfo(){
        RegisterAgentInfo agentInfo = SpringApplicationContextHolder.getBean(RegisterAgentInfo.class);
        agentInfo.setLastRegisterTime(LocalDateTime.now());
        return agentInfo;
    }

    //当连接断开时，重新连接
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("与服务器断开连接，将进行重连...");
        AgentClientMain client = SpringApplicationContextHolder.getBean(AgentClientMain.class);
        client.startNettyAgent();
    }

    //异常处理
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}