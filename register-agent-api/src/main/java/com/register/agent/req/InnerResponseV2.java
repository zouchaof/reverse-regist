package com.register.agent.req;

import lombok.Data;

import java.util.Map;

@Data
public class InnerResponseV2 extends BaseMessage {

    private int status;

    private Map<String, String> headerMap;

    private byte[] outBytes;

}
