package com.imslbd.call_center.service;

import com.imslbd.call_center.MainVerticle;
import com.imslbd.call_center.MyApp;
import com.imslbd.call_center.controller.Controllers;
import com.imslbd.call_center.gv;
import io.crm.util.ExceptionUtil;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * Created by someone on 08/12/2015.
 */
public class DistributionHouseService {
    private static final String GET_DISTRIBUTION_HOUSES_URI = "/Home/GetDistribution";
    private final Vertx vertx;
    private final HttpClient httpClient;

    public DistributionHouseService(Vertx vertx, HttpClient httpClient) {
        this.vertx = vertx;
        this.httpClient = httpClient;
    }

    public void findAll(Message<JsonObject> message) {
        httpClient.get(GET_DISTRIBUTION_HOUSES_URI + "?Id=" + message.body().getLong("areaId"), res -> {
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
//            .putHeader("Host", MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
//            .putHeader("Referer", "http://" + MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
//            .putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON)
            .exceptionHandler(e -> ExceptionUtil.fail(message, e))
            .end();
    }
}
