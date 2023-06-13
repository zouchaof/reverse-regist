package com.register.server.core;

import com.register.agent.req.InnerRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ReverserResponseHandler<T> {

    void reverserResponse(InnerRequest request, T msg, HttpServletResponse response) throws IOException;

}
