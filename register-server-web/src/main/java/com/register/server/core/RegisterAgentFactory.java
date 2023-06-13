package com.register.server.core;

import com.alibaba.fastjson.JSONObject;
import com.register.agent.req.RegisterAgentInfo;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RegisterAgentFactory {

    /**
     * 执行节点各种策略待定
     */
    private static Map<String, List<RegisterAgentInfo>> registerAgentListMap = new ConcurrentHashMap<>();

    private static Map<String, String> contextAppNameMap = new ConcurrentHashMap<>();


    public static Map<String, List<RegisterAgentInfo>> getRegisterAgentListMap() {
        return registerAgentListMap;
    }

    public static Set<String> getAppNameSet(){
        return registerAgentListMap.keySet();
    }

    public static void registerAgent(Object ctx, RegisterAgentInfo registerAgentInfo){

        log.info("注册客户端信息：{}", JSONObject.toJSONString(registerAgentInfo));
        if(registerAgentInfo == null || StringUtils.isEmpty(registerAgentInfo.getAppName())){
            return;
        }
        registerAgentInfo.setLastRegisterTime(LocalDateTime.now());
        registerAgentInfo.setCtx(ctx);

        String appName = registerAgentInfo.getAppName();
        contextAppNameMap.put(ctx.toString(), appName);
        if(registerAgentListMap.containsKey(appName)){
            List<RegisterAgentInfo> registerAgentInfoList = registerAgentListMap.get(appName);
            boolean hasRegister = false;
            for(RegisterAgentInfo info : registerAgentInfoList){
                if(info.getReqId().equals(registerAgentInfo.getReqId())){
                    info.setLastRegisterTime(registerAgentInfo.getLastRegisterTime());
                    info.setCtx(registerAgentInfo.getCtx());
                    hasRegister = true;
                }
            }
            if(!hasRegister){
                registerAgentListMap.get(appName).add(registerAgentInfo);
            }
        }else{
            List<RegisterAgentInfo> registerAgentInfoList = new ArrayList<>();
            registerAgentInfoList.add(registerAgentInfo);
            registerAgentListMap.put(appName, registerAgentInfoList);
        }
    }

    public static void removeRegisterAgent(Object ctx){
        if(!contextAppNameMap.containsKey(ctx.toString())){
            return;
        }
        String appName = contextAppNameMap.get(ctx.toString());
        List<RegisterAgentInfo> registerAgentInfoList = registerAgentListMap.get(appName);
        registerAgentInfoList.removeIf(item -> item.getCtx() == ctx);
        contextAppNameMap.remove(ctx.toString());
    }

    public static RegisterAgentInfo getExecAgentInfo(String appName){
        if(!registerAgentListMap.containsKey(appName)){
            return null;
        }

        LocalDateTime  halfHourAgo = LocalDateTime.now().minusMinutes(3);
        List<RegisterAgentInfo> registerAgentInfoList = registerAgentListMap.get(appName);
        registerAgentInfoList.removeIf(item -> halfHourAgo.isAfter(item.getLastRegisterTime()));
        return registerAgentInfoList.stream().reduce((one, next) -> {
                    if(one.getLastUseTime() == null){
                        return one;
                    }
                    if(next.getLastUseTime() == null){
                        return next;
                    }
                    if(one.getLastUseTime().isBefore(next.getLastUseTime())){
                        return one;
                    }
                    return next;
                }).get();
    }

    public static void writeData(RegisterAgentInfo agentInfo, Object reqData) {
        if("ctx".equals(agentInfo.getRegisterType())){
            ChannelHandlerContext ctx = (ChannelHandlerContext)agentInfo.getCtx();
            ctx.executor().submit(() -> {
                // 执行具体的写操作
                ctx.writeAndFlush(reqData);
            });
        }
        if("browser".equals(agentInfo.getRegisterType())){
            try {
                WebSocketSession session = (WebSocketSession)agentInfo.getCtx();
                session.sendMessage(new TextMessage(JSONObject.toJSONString(reqData)));
            } catch (IOException e) {
                log.error("浏览器消息写入异常", e);
            }
        }
    }

}
