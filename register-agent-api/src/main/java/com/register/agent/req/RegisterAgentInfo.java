package com.register.agent.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class RegisterAgentInfo extends BaseMessage {

    private String path;

    private String appName;

    private String serverHost;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastRegisterTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUseTime;

    private ChannelHandlerContext ctx;

    private String registerIp;

}
