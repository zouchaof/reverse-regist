package com.register.agent;

import com.register.agent.req.RegisterAgentInfo;
import com.register.agent.utils.IdWork;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.net.Inet4Address;
import java.net.UnknownHostException;

@ConditionalOnProperty("register.agent.appName")
@ComponentScan("com.register.agent")
public class RegisterConfiguration {

    private static final String AGENT_CLIENT_ID = IdWork.getId();

    @Value("${register.agent.appName}")
    private String appName;

    @Value("${register.agent.serverHost:http://localhost:81}")
    private String serverHost;


    @Bean
    public RegisterAgentInfo registerAgentInfo() throws UnknownHostException {
        RegisterAgentInfo agentInfo = new RegisterAgentInfo();
        agentInfo.setReqId(AGENT_CLIENT_ID);
        agentInfo.setAppName(appName);
        agentInfo.setPath("/");
        agentInfo.setRegisterIp(Inet4Address.getLocalHost().getHostAddress());
        agentInfo.setServerHost(serverHost);
        agentInfo.setRegisterType("ctx");
        return agentInfo;
    }



}
