package com.imslbd.call_center.service;

import com.imslbd.call_center.gv;
import io.crm.promise.Promises;
import io.crm.util.ExceptionUtil;
import io.crm.util.Util;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;

/**
 * Created by shahadat on 1/17/16.
 */
public class AuthService {
    private static final String LOGIN_URL = "/login/apiLogin";
    private final HttpClient httpClient;

    public AuthService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void login(Message<JsonObject> message) {
        Promises.from(message.body()).then(entries -> {
            String encode = entries.encode();
            httpClient.post(LOGIN_URL,
                res -> res.bodyHandler(b -> {
                    try {
                        message.reply(new JsonObject(b.toString()));
                    } catch (Exception ex) {
                        ExceptionUtil.fail(message, ex);
                    }
                }).exceptionHandler(e -> ExceptionUtil.fail(message, e)))
                .exceptionHandler(e -> ExceptionUtil.fail(message, e))
                .sendHead()
                .putHeader(gv.X_Requested_With, Services.CALL_CENTER_JAVA)
                .putHeader(HttpHeaders.CONTENT_LENGTH, Util.toString(encode.length()))
//                .putHeader("Host", MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
//                .putHeader("Referer", "http://" + MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
//                .setChunked(true)
                .write(encode)
                .end();
        }).error(e -> ExceptionUtil.fail(message, e));
    }
}
