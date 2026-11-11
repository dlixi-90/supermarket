package com.market.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {

    private static final MongoClient mongoClient =
        MongoClients.create(requireMongoUri());

    private static final MongoDatabase database =
        mongoClient.getDatabase("marketdb");

    public static MongoDatabase getDatabase() {
        return database;
    }
    
    public static MongoClient getClient() {

        return mongoClient;
    }

    private static String requireMongoUri() {
        String uri = System.getenv("MONGODB_URI");
        if (uri == null || uri.isBlank()) {
            throw new IllegalStateException("MONGODB_URI environment variable is not set.");
        }
        return uri;
    }
}
