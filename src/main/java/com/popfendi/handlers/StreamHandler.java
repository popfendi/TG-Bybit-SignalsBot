package com.popfendi.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.popfendi.models.PriceData;
import com.popfendi.repository.DataManager;


import java.math.BigDecimal;
import java.util.logging.Logger;

// handles data stream from bybit ws subscriptions
public class StreamHandler {

    private static final Logger LOGGER = Logger.getLogger( StreamHandler.class.getName() );


    private DataManager dm = new DataManager().getInstance();

    public void processMessage(String message) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        JsonNode json = om.readTree(message);

        if(validDataMessage(json)){
            JsonNode data = json.get("data");
            PriceData pd = new PriceData(
                    json.get("topic").asText(),
                    data.get("symbol").asText(),
                    new BigDecimal(data.get("markPrice").asText()),
                    json.get("ts").asLong()
            );

            dm.updatePrice(pd);
        } else {
            if(!json.has("topic")){
                try{
                    boolean success = json.get("success").asBoolean();
                    String op = json.get("op").asText();
                    String reqId = json.get("req_id").asText();

                    if(!success && op.equals("subscribe") && !reqId.equals("")){
                        dm.removeFailedSignal(reqId);
                    }
                } catch (Exception e) {
                    LOGGER.info("Websocket Connection Msg: " + message);
                }
                LOGGER.info("Websocket Connection Msg: " + message);
            }
        }
    }

    // validates if messaage contains price data or not.
    private boolean validDataMessage(JsonNode json){
        if(isDataMsg(json) && json.get("type").asText().equals("snapshot")){
            return true;
        } else if (isDataMsg(json) && json.get("type").asText().equals("delta")){
            JsonNode data = json.get("data");
            if (!data.has("markPrice")){
                return false;
            }
            return true;
        }
        return false;
    }


    private boolean isDataMsg(JsonNode json){
        return json.has("topic") && json.has("type") && json.has("ts") && json.has("data");
    }
}
