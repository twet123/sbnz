package com.ftn.sbnz.service.configuration;

import com.ftn.sbnz.service.WsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final WsHandler wsHandler;

    @Autowired
    public WebSocketConfiguration(WsHandler wsHandler) {
        this.wsHandler = wsHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(wsHandler, "/temperature")
                .setAllowedOrigins("*");
    }
}
