package com.imslbd.call_center.service;

import com.imslbd.call_center.gv;
import io.crm.FailureCode;
import io.crm.util.ExceptionUtil;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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
        httpClient
            .getAbs(baseUrl + GET_BRS_URI + "?Id=" + message.body().getLong(gv.distributionHouseId), res -> {
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

    public void findBrInfo(Message<JsonObject> message) {

        try {
            if (message.body() == null || message.body().getValue("brId") == null) {
                message.fail(FailureCode.BadRequest.code, "BR ID MISSING.");
                return;
            }
            String baseUrl = message.body().getString("baseUrl");
            message.body().remove("baseUrl");
            String query = "select * from brs where br_id = " + message.body().getString("brId", "0");
            httpClient
                .getAbs(baseUrl + "/sql/query?sql=" + ExceptionUtil.toRuntimeCall(() -> URLEncoder.encode(query, StandardCharsets.UTF_8.name())),
                    res -> res
                        .bodyHandler(b -> {
                            try {
                                message.reply(new JsonObject((Map<String, Object>) new JsonArray(b.toString()).stream().findFirst().orElse(new HashMap<>())));
                            } catch (Exception e) {
                                ExceptionUtil.fail(message, e);
                            }
                        })
                        .exceptionHandler(e -> ExceptionUtil.fail(message, e)))
                .sendHead()
                .putHeader(gv.X_Requested_With, Services.CALL_CENTER_JAVA)
                .exceptionHandler(e -> ExceptionUtil.fail(message, e))
                .end();
        } catch (Exception ex) {
            ExceptionUtil.fail(message, ex);
        }
    }
}
