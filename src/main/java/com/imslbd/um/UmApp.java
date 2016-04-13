package com.imslbd.um;

import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mongo.MongoClient;

/**
 * Created by shahadat on 4/12/16.
 */
final public class UmApp {
    private static MongoClient mongoClient;
    private static MailClient mailClient;

    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    public static void setMongoClient(MongoClient mongoClient) {
        UmApp.mongoClient = mongoClient;
    }

    public static MailClient getMailClient() {
        return mailClient;
    }

    public static void setMailClient(MailClient mailClient) {
        UmApp.mailClient = mailClient;
    }
}
