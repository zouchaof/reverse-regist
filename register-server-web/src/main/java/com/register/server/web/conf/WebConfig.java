package com.register.server.web.conf;

import com.register.server.web.interceptor.ReverseRequestInterceptor;
import com.register.server.web.socket.ReverseWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebConfig implements WebMvcConfigurer, WebSocketConfigurer {

    @Autowired
    private ReverseRequestInterceptor reverseRequestInterceptor;

    @Autowired
    private ReverseWebSocketHandler reverseWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(reverseWebSocketHandler, "/rv-websocket").setAllowedOrigins("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(reverseRequestInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/rv-manage/**", "/static/**", "/error");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/static/**").addResourceLocations("classPath:/META-INF/static/");
    }

}
