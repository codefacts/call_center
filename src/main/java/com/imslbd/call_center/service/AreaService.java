package com.imslbd.call_center.service;

import com.imslbd.call_center.gv;
import io.crm.util.ExceptionUtil;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.nio.file.Paths;

/**
 * Created by someone on 08/12/2015.
 */
public class AreaService {
    public static final String GET_AREAS_URI = "/Call/GetAreas";
    private final Vertx vertx;
    private final HttpClient httpClient;

    public AreaService(Vertx vertx, HttpClient httpClient) {
        this.vertx = vertx;
        this.httpClient = httpClient;
    }

    public void findAll(Message<JsonObject> message) {
        try {
            String baseUrl = message.body().getString("baseUrl");
            message.body().remove("baseUrl");
            httpClient.getAbs(baseUrl + GET_AREAS_URI.toString(), res -> {
                res
                    .bodyHandler(b -> {
                        try {
                            message.reply(new JsonObject()
                                .put("data", new JsonArray(b.toString())));
                        } catch (Exception e) {
                            ExceptionUtil.fail(message, e);
                        }
                    })
                    .exceptionHandler(e -> ExceptionUtil.fail(message, e));
            })
                .sendHead()
                .putHeader(gv.X_Requested_With, Services.CALL_CENTER_JAVA)
                .exceptionHandler(e -> ExceptionUtil.fail(message, e))
                .end();
        } catch (Exception ex) {
            ExceptionUtil.fail(message, ex);
        }
    }
}
