package com.popfendi.client;

import java.io.IOException;
import java.net.URI;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.websocket.*;

import com.popfendi.config.PropertiesLoader;
import org.json.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

// bybit client for handling WS conn and subscriptions
public class Client {

    static String api_key = PropertiesLoader.getProperties().getProperty("api.key");
    static String api_secret = PropertiesLoader.getProperties().getProperty("api.secret");
    static Session session;

    public static String generate_signature(String expires){ return sha256_HMAC("GET/realtime"+ expires, api_secret); }

    private static String byteArrayToHexString(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b!=null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1)
                hs.append('0');
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }

    public static String sha256_HMAC(String message, String secret) {
        String hash = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(message.getBytes());
            hash = byteArrayToHexString(bytes);
        } catch (Exception e) {
            System.out.println("Error HmacSHA256 ===========" + e.getMessage());
        }
        return hash;

    }

    public static String getAuthMessage(){
        JSONObject req=new JSONObject();
        req.put("op", "auth");
        List<String> args = new LinkedList<String>();
        String expires = String.valueOf(System.currentTimeMillis()+10000);
        args.add(api_key);
        args.add(expires);
        args.add(generate_signature(expires));
        req.put("args", args);
        return (req.toString());
    }

    public static String subscribe(String op, String argv, String reqId){
        JSONObject req=new JSONObject();
        req.put("op", op);
        List<String> args = new LinkedList<String>();
        args.add(argv);
        req.put("args", args);
        if(reqId != null){
            req.put("req_id", reqId);
        }
        return req.toString();
    }

    public static void sendMsg(String op, String arg, String reqId){
        try {
            System.out.println("sending: " + subscribe(op, arg, reqId));
            session.getBasicRemote().sendText(subscribe(op, arg, reqId));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void connectWebsocket(){

        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            String uri = PropertiesLoader.getProperties().getProperty("api.url");
            container.connectToServer(BybitWebsocket.class, URI.create(uri));
            session.getBasicRemote().sendText("{\"op\":\"ping\"}");
            session.getBasicRemote().sendText(getAuthMessage());

            java.io.BufferedReader r=new  java.io.BufferedReader(new java.io.InputStreamReader( System.in));
            while(true){
                String line=r.readLine();
                if(line.equals("quit")) break;
                session.getBasicRemote().sendText(line);
            }

        } catch ( Exception ex) {
            ex.printStackTrace();
        }
    }
}