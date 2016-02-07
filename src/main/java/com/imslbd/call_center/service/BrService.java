package com.imslbd.call_center.service;

import com.imslbd.call_center.gv;
import io.crm.util.ExceptionUtil;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.nio.file.Paths;

/**
 * Created by someone on 08/12/2015.
 */
public class BrService {
    private static final String GET_BRS_URI = "/Home/GetBR";
    private final Vertx vertx;
    private final HttpClient httpClient;

    public BrService(Vertx vertx, HttpClient httpClient) {
        this.vertx = vertx;
        this.httpClient = httpClient;
    }

    public void findAll(Message<JsonObject> message) {
        String baseUrl = message.body().getString("baseUrl");
        message.body().remove("baseUrl");
        httpClient.getAbs(baseUrl + GET_BRS_URI + "?Id=" + message.body().getLong(gv.distributionHouseId), res -> {
            res
                .bodyHandler(b -> {
                    try {
                        final JsonArray jsonArray = new JsonArray();
                        final JsonArray list = new JsonArray(b.toString());
                        for (int i = 0; i < list.size(); i++) {
                            jsonArray.add(new JsonObject()
                                .put(gv.id, list.getJsonObject(i).getLong("ID"))
                                .put(gv.name, list.getJsonObject(i).getString("Name")));
                        }
                        message.reply(new JsonObject()
                            .put("data", jsonArray));
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
    }
}
