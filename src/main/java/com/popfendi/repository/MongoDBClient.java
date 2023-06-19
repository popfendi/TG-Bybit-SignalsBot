package com.popfendi.repository;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.popfendi.config.PropertiesLoader;
import org.bson.conversions.Bson;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;


import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoDBClient {
    private static final Logger LOGGER = Logger.getLogger( MongoDBClient.class.getName() );

    public static MongoDatabase instance;

    private String uri = PropertiesLoader.getProperties().getProperty("mongo.db.url");

    // Construct a ServerApi instance using the ServerApi.builder() method
    ServerApi serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build();
    MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(uri))
            .serverApi(serverApi)
            .build();

    public static MongoDatabase getDbInstance(){
        if(instance == null){
            MongoDBClient DB = new MongoDBClient();
            DB.connect();
            return instance;
        }

        return instance;
    }

    public void connect(){
        // Create a new client and connect to the server
        try {
            MongoClient client = MongoClients.create(settings);
            instance = client.getDatabase("signalsBot");
            try {
                // Send a ping to confirm a successful connection
                Bson command = new BsonDocument("ping", new BsonInt64(1));
                Document commandResult = instance.runCommand(command);
                LOGGER.info("Pinged your deployment. You successfully connected to MongoDB!");
            } catch (MongoException me) {
                LOGGER.log(Level.SEVERE, me.getMessage());
            }
        }catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }


}

