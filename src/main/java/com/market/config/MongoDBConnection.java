package com.market.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

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
            Path envFile = Path.of(".env");
            if (Files.isRegularFile(envFile)) {
                Properties properties = new Properties();
                try (Reader reader = Files.newBufferedReader(envFile, StandardCharsets.UTF_8)) {
                    properties.load(reader);
                    uri = properties.getProperty("MONGODB_URI");
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to read MONGODB_URI from .env.", e);
                }
            }
        }
        if (uri == null || uri.isBlank()) {
            throw new IllegalStateException("MONGODB_URI is not set in the environment or .env file.");
        }
        return uri;
    }
}
