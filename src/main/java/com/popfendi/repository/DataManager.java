package com.popfendi.repository;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;

import com.mongodb.client.result.UpdateResult;
import com.popfendi.bots.SignalBot;
import com.popfendi.client.Client;
import com.popfendi.models.PriceData;
import com.popfendi.models.Signal;
import com.popfendi.models.Stats;
import org.bson.Document;
import org.bson.conversions.Bson;
import  com.mongodb.client.MongoCollection;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

// most of the data handling logic happens in here
public final class DataManager implements PropertyChangeListener{
    private static final Logger LOGGER = Logger.getLogger( DataManager.class.getName() );
    public static DataManager instance;

    private MongoDatabase mongoDatabase = MongoDBClient.getDbInstance();

    // in memory cache for holding most recent price update for each trading pair
    private HashMap<String, PriceData> priceCache = new HashMap<>();
    // in memory cache for holding all active signals (hashmap works for low volume of trades, consider a cache like redis for higher volume)
    private HashMap<String, Signal> signalCache = new HashMap<>();

    private String newLine = System.getProperty("line.separator");

    public void init() throws InterruptedException {
        int count = 0;
        int maxTries = 10;
        while (true){
            try {
                Thread.sleep(1500);
                loadAllActiveTradesIntoCache();
                initCacheTracking();
                break;
            } catch (Exception e){
                if (++count == maxTries) throw e; // initialize with 10 retries (incase db slow to connect)
            }
        }
    }

    public static DataManager getInstance(){
        if(instance == null){
            instance = new DataManager();
        }

        return instance;
    }


    public void updatePrice(PriceData price){
        // if price for current symbol already being tracked & this event isn't old
        if(priceCache.containsKey(price.getSymbol())
                && priceCache.get(price.getSymbol()).getTs() < price.getTs()){
            priceCache.get(price.getSymbol()).setPrice(price.getPrice());
            priceCache.get(price.getSymbol()).setTs(price.getTs());

        }else {
            priceCache.put(price.getSymbol(), price);
        }
    }

    public void insertAndTrackNewSignal(Signal signal){
        LOGGER.info("Adding New Signal: " + signal.getPair() + " " + signal.getDirection());
        signalCache.put(signal.getPair(), signal);
        upsertSignalToDb(signal);
        Client.sendMsg("subscribe", "tickers." + signal.getPair(), signal.getReqId());
    }

    public void closeAndStopTracking(Signal signal){
        LOGGER.info("Closing Trade for Signal: " + signal.getPair() + " " + signal.getDirection());
        signalCache.remove(signal.getPair());
        signal.setOpenPosition(false);
        upsertSignalToDb(signal);
        Client.sendMsg("unsubscribe", "tickers." + signal.getPair(), signal.getReqId());
    }

    public void removeFailedSignal(String reqId){
        LOGGER.info("Subscription failed removing signal with ID: " + reqId);
        Signal signal = findAndDeleteSignalByReqId(reqId);
        signalCache.remove(signal.getPair());
        SignalBot.getBotInstance().sendErrorMessageToUser("I couldn't subscribe to pair ticker for signal: " + signal.getPair() + " please check the spelling and ensure it is formatted correctly (like this BTCUSDT)");
    }


    // observes property change events from Signal objects and price data objects
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        switch (propertyChangeEvent.getPropertyName()) {
            case "price" -> handlePriceChange(propertyChangeEvent);
            case "tp1" -> handleTp1(propertyChangeEvent);
            case "tp2" -> handleTp2(propertyChangeEvent);
            case "tp3" -> handleTp3(propertyChangeEvent);
            case "sl" -> handleSl(propertyChangeEvent);
            case "tp2ThenSl" -> handleTp2ThenSl(propertyChangeEvent);
            case "tp1ThenSl" -> handleTp1ThenSl(propertyChangeEvent);
        }
    }

    private void handlePriceChange(PropertyChangeEvent pce){
        PriceData pd = (PriceData) pce.getSource();
        Signal signal = signalCache.get(pd.getSymbol());
        signal.checkTargets((BigDecimal) pce.getNewValue());
    }

    private void handleTp1(PropertyChangeEvent pce){
        Signal signal = (Signal) pce.getSource();
        upsertSignalToDb(signal);
        String message = String.format(
                "\uD83D\uDD2E"
                        + newLine +
                        "%s Take-Profit target 1 ✅"
                        + newLine +
                        "Profit: %.3f \uD83D\uDCC8"
                        + newLine +
                        "Period: %s ⌚️"
                        + newLine +
                        "Congratulations to all \uD83C\uDF89"
                , signal.getPair(), signal.getProfitPercentage(), signal.calulatePeriod());

        SignalBot.getBotInstance().broadcastToChannel(message);
    }

    private void handleTp2(PropertyChangeEvent pce) {
        Signal signal = (Signal) pce.getSource();
        upsertSignalToDb(signal);
        String message = String.format(
                "\uD83D\uDD2E"
                        + newLine +
                        "%s Take-Profit target 2 ✅"
                        + newLine +
                        "Profit: %.3f \uD83D\uDCC8"
                        + newLine +
                        "Period: %s ⌚️"
                        + newLine +
                        "Congratulations to all \uD83C\uDF89"
                , signal.getPair(), signal.getProfitPercentage(), signal.calulatePeriod());
        SignalBot.getBotInstance().broadcastToChannel(message);
    }

    private void handleTp3(PropertyChangeEvent pce) {
        Signal signal = (Signal) pce.getSource();
        closeAndStopTracking(signal);
        String message = String.format(
                "\uD83D\uDD2E"
                        + newLine +
                        "%s Take-Profit target 3 Trade Now Closed ✅"
                        + newLine +
                        "Profit: %.3f \uD83D\uDCC8"
                        + newLine +
                        "Period: %s ⌚️"
                        + newLine +
                        "Congratulations to all \uD83C\uDF89"
                , signal.getPair(), signal.getProfitPercentage(), signal.calulatePeriod());
        SignalBot.getBotInstance().broadcastToChannel(message);
    }

    private void handleSl(PropertyChangeEvent pce) {
        Signal signal = (Signal) pce.getSource();
        closeAndStopTracking(signal);
        String message = String.format(
                "\uD83D\uDD2E"
                        + newLine +
                        "%s Stop loss ❌"
                        + newLine +
                        "Profit: %.3f \uD83D\uDCC8"
                        + newLine +
                        "Period: %s ⌚️"
                        + newLine +
                        "Next one!\uD83E\uDEE1"
                , signal.getPair(), signal.getProfitPercentage(), signal.calulatePeriod());
        SignalBot.getBotInstance().broadcastToChannel(message);
    }

    private void handleTp1ThenSl(PropertyChangeEvent pce){
        Signal signal = (Signal) pce.getSource();
        closeAndStopTracking(signal);

        SignalBot.getBotInstance().broadcastToChannel(signal.getPair() + " Closed at stoploss after reaching take profit 1 " + signal.getProfitPercentage() + "% ⚠️");
    }

    private void handleTp2ThenSl(PropertyChangeEvent pce){
        Signal signal = (Signal) pce.getSource();
        closeAndStopTracking(signal);
        SignalBot.getBotInstance().broadcastToChannel(signal.getPair() + " Closed at stoploss after reaching take profit 2 " + signal.getProfitPercentage() + "% ⚠️");
    }

    public void upsertSignalToDb(Signal signal){
        ObjectMapper om = new ObjectMapper();
        try {
            String jsonString = om.writeValueAsString(signal);
            Document doc = Document.parse(jsonString);



            Bson filters = Filters.and(
                    Filters.eq("pair", signal.getPair()),
                    Filters.eq("openPosition", true));

            Bson update = new Document("$set", doc);


            UpdateOptions options = new UpdateOptions().upsert(true);

            MongoCollection collection = mongoDatabase.getCollection("signals");
            UpdateResult res = collection.updateOne(filters, update, options);
            System.out.println(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Signal findAndDeleteSignalByReqId(String reqId){
        ObjectMapper om = new ObjectMapper();
        try{
            Bson filter = Filters.eq("reqId", reqId);

            MongoCollection<Document> collection = mongoDatabase.getCollection("signals");
            Document doc = collection.findOneAndDelete(filter);

            assert doc != null;
            return om.readValue(doc.toJson(), Signal.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            LOGGER.info("Couldn't parse Bson to Signal");
            return null;
        }
    }

    public List<Signal> getLastTenSignals(){
        ObjectMapper om = new ObjectMapper();
        MongoCursor<Document> cursor = null;
        ArrayList<Signal> signals = new ArrayList<>();
        try{
            MongoCollection collection = mongoDatabase.getCollection("signals");

            cursor = collection.find(Filters.or(
                            Filters.eq("targetsHit.tp1", true),
                            Filters.eq("targetsHit.sl", true)
                    ))
                    .sort(Sorts.descending("timestamp"))
                    .limit(10)
                    .iterator();

            while (cursor.hasNext()){
                Signal signal = om.readValue(cursor.next().toJson(), Signal.class);
                signals.add(signal);
            }

            return signals;
        } catch (JsonProcessingException e) {
            LOGGER.info("couldn't get last ten signals");
            e.printStackTrace();
        }

        return null;
    }

    public Double getProfitAggregation() {
        MongoCursor<Document> cursor = null;
        ObjectMapper om = new ObjectMapper();
        try {
            MongoCollection collection = mongoDatabase.getCollection("signals");

            cursor = collection.aggregate(Arrays.asList(
                    Aggregates.match(Filters.exists("profitPercentage", true)),
                    Aggregates.group("totalProfitPercentage", Accumulators.sum("profitPercentage", "$profitPercentage"))
            )).iterator();

            while (cursor.hasNext()) {
                double total = om.readTree(cursor.next().toJson()).get("profitPercentage").asDouble();
                return total * 10;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Stats getStatsAggregation() {
        MongoCursor<Document> cursor = null;
        ObjectMapper om = new ObjectMapper();
        MongoCollection collection = mongoDatabase.getCollection("signals");

        int count = (int) collection.countDocuments();


        List<Bson> totalOpen = Arrays.asList(
                Aggregates.match(Filters.eq("openPosition", true)),
                Aggregates.count()
        );

        List<Bson> totalWins = Arrays.asList(
                Aggregates.match(Filters.and(Filters.eq("targetsHit.tp1", true), Filters.eq("targetsHit.sl", false))),
                Aggregates.count()
        );

        List<Bson> totalLoss = Arrays.asList(
                Aggregates.match(Filters.and(Filters.eq("targetsHit.sl", true), Filters.eq("targetsHit.tp1", false))),
                Aggregates.count()
        );

        List<Bson> totalTp1 = Arrays.asList(
                Aggregates.match(Filters.eq("targetsHit.tp1", true)),
                Aggregates.count()
        );

        List<Bson> totalTp2 = Arrays.asList(
                Aggregates.match(Filters.eq("targetsHit.tp2", true)),
                Aggregates.count()
        );

        List<Bson> totalTp3 = Arrays.asList(
                Aggregates.match(Filters.eq("targetsHit.tp3", true)),
                Aggregates.count()
        );

        Bson facet = Aggregates.facet(
                new Facet("totalOpen", totalOpen),
                new Facet("totalWins", totalWins),
                new Facet("totalLoss", totalLoss),
                new Facet("totalTp1", totalTp1),
                new Facet("totalTp2", totalTp2),
                new Facet("totalTp3", totalTp3)
        );




       cursor = collection.aggregate(Collections.singletonList(facet)).iterator();

       while (cursor.hasNext()){
           try {
               JsonNode json = om.readTree(cursor.next().toJson());
               JsonNode totalOpenArray = json.get("totalOpen");
               JsonNode totalWinsArray = json.get("totalWins");
               JsonNode totalLossArray = json.get("totalLoss");
               JsonNode totalTp1Array = json.get("totalTp1");
               JsonNode totalTp2Array = json.get("totalTp2");
               JsonNode totalTp3Array = json.get("totalTp3");

               int open = totalOpenArray.size() > 0 ? totalOpenArray.get(0).get("count").asInt() : 0;
               int wins = totalWinsArray.size() > 0 ? totalWinsArray.get(0).get("count").asInt() : 0;
               int loss = totalLossArray.size() > 0 ? totalLossArray.get(0).get("count").asInt() : 0;
               int tp1 = totalTp1Array.size() > 0 ? totalTp1Array.get(0).get("count").asInt() : 0;
               int tp2 = totalTp2Array.size() > 0 ? totalTp2Array.get(0).get("count").asInt() : 0;
               int tp3 = totalTp3Array.size() > 0 ? totalTp3Array.get(0).get("count").asInt() : 0;

               return new Stats(
                       count,
                       wins,
                       open,
                       loss,
                       tp1,
                       tp2,
                       tp3
               );

           } catch (JsonProcessingException e) {
               e.printStackTrace();
           }
       }
        return null;

    }

    private void loadAllActiveTradesIntoCache() {
        ObjectMapper om = new ObjectMapper();
        MongoCursor<Document> cursor = null;
        try {
            MongoCollection<Document> collection = mongoDatabase.getCollection("signals");

            Bson projections = Projections.excludeId();

            cursor = collection.find(Filters.eq("openPosition", true)).projection(projections).iterator();


            while (cursor.hasNext()) {
                try {
                    Signal signal = om.readValue(cursor.next().toJson(), Signal.class);
                    signalCache.put(signal.getPair(), signal);
                    LOGGER.info("Loading signal from DB: " + signal.getPair() + " " + signal.getDirection());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

        } finally {
            cursor.close();
        }

    }

    // sub to price updates for all active signals in cache
    private void initCacheTracking(){
        signalCache.forEach((k, v) -> { Client.sendMsg("subscribe", "tickers." + v.getPair(), v.getReqId()); });
    }


}
