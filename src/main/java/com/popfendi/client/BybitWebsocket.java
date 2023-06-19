package com.popfendi.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.popfendi.handlers.StreamHandler;

import javax.websocket.*;

@ClientEndpoint
public class BybitWebsocket {

    private StreamHandler sh = new StreamHandler();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to endpoint: " + session.getBasicRemote());
        try {
            Client.session=session;
            System.out.println(Client.session);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OnMessage
    public void processMessage(String message) {
        try {
            sh.processMessage(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void processError(Throwable t) {
        t.printStackTrace();
    }


}
