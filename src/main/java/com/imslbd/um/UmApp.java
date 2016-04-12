package com.imslbd.um;

import io.vertx.ext.mongo.MongoClient;

/**
 * Created by shahadat on 4/12/16.
 */
final public class UmApp {
    private static MongoClient mongoClient;

    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    public static void setMongoClient(MongoClient mongoClient) {
        UmApp.mongoClient = mongoClient;
    }
}
