package com.register.agent;

import com.register.agent.req.RegisterAgentInfo;
import com.register.agent.utils.IdWork;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@ConditionalOnProperty("register.agent.appName")
@ComponentScan("com.register.agent")
public class RegisterConfiguration {

    private static final long AGENT_CLIENT_ID = IdWork.getId();

    @Value("${register.agent.appName}")
    private String appName;

    @Value("${register.agent.serverHost:http://localhost:81}")
    private String serverHost;


    @Bean
    public RegisterAgentInfo registerAgentInfo(){
        RegisterAgentInfo agentInfo = new RegisterAgentInfo();
        agentInfo.setReqId(AGENT_CLIENT_ID);
        agentInfo.setAppName(appName);
        agentInfo.setPath("/");
//TODO 添加ip
        agentInfo.setServerHost(serverHost);
        return agentInfo;
    }



}
