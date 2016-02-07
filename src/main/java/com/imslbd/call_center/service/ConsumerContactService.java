package com.imslbd.call_center.service;

import com.google.common.collect.ImmutableList;
import com.imslbd.call_center.MainVerticle;
import com.imslbd.call_center.MyApp;
import com.imslbd.call_center.gv;
import io.crm.promise.Promises;
import io.crm.promise.intfs.Defer;
import io.crm.promise.intfs.Promise;
import io.crm.util.ExceptionUtil;
import io.crm.util.Util;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by someone on 13/12/2015.
 */
public class ConsumerContactService {
    public static final String CALL_SEARCH_LOAD_DATA_URI = "/Call/SearchLoadData";
    public static final String CALL_SEARCH_STEP_2_URI = "/Call/searchResult";
    private final HttpClient httpClient;

    public ConsumerContactService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void consumerContactsCallStep_1(Message<JsonObject> message) {
        Promises.from(message.body())
            .then(criteria -> {
                String baseUrl = criteria.getString("baseUrl");
                criteria.remove("baseUrl");
                httpClient
                    .getAbs(baseUrl + CALL_SEARCH_LOAD_DATA_URI + queryString(criteria),
                        res -> res
                            .bodyHandler(b -> {
                                try {
                                    message.reply(new JsonObject(b.toString()));
                                } catch (Exception ex) {
                                    ExceptionUtil.fail(message, ex);
                                }
                            })
                            .exceptionHandler(e ->
                                ExceptionUtil.fail(message, e)))
                    .sendHead()
                    .putHeader(gv.X_Requested_With, Services.CALL_CENTER_JAVA)
//                .putHeader("Host", MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
//                .putHeader("Referer", "http://" + MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
                    .exceptionHandler(e ->
                        ExceptionUtil.fail(message, e))
                    .end();
            })
            .error(e ->
                ExceptionUtil.fail(message, e));
    }

    public void consumerContactsCallStep_2(Message<JsonObject> message) {
        Promises.from(message.body())
            .then(criteria -> {
                String baseUrl = criteria.getString("baseUrl");
                criteria.remove("baseUrl");
                httpClient.getAbs(baseUrl + CALL_SEARCH_STEP_2_URI + queryString_2(criteria),
                    res -> res.bodyHandler(b -> {
                        try {
                            message.reply(new JsonObject(b.toString()));
                        } catch (Exception ex) {
                            ExceptionUtil.fail(message, ex);
                        }
                    })
                        .exceptionHandler(e -> ExceptionUtil.fail(message, e)))
                    .sendHead()
                    .putHeader(gv.X_Requested_With, Services.CALL_CENTER_JAVA)
//                .putHeader("Host", MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
//                .putHeader("Referer", "http://" + MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
                    .exceptionHandler(e -> ExceptionUtil.fail(message, e))
                    .end();
            })
            .error(e -> ExceptionUtil.fail(message, e));
    }


    public void brActivitySummary(Message<JsonObject> message) {
        try {
            final Defer<JsonObject> defer1 = Promises.defer();
            final Defer<JsonObject> defer2 = Promises.defer();

            final JsonObject entries = message.body();
            String baseUrl = entries.getString("baseUrl");
            entries.remove("baseUrl");
            httpClient.getAbs(baseUrl + "/Call/brReportDaily" + "?br=" + entries.getValue("brId") + "&date=" + entries.getValue("workDate"),
                res -> res.bodyHandler(b -> {
                    try {
                        defer1.complete(new JsonObject(b.toString()));
                    } catch (Exception ex) {
                        ExceptionUtil.fail(message, ex);
                    }
                }).exceptionHandler(defer1::fail))
                .sendHead()
                .putHeader(gv.X_Requested_With, Services.CALL_CENTER_JAVA)
//                .putHeader("Host", MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
//                .putHeader("Referer", "http://" + MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
                .exceptionHandler(defer1::fail)
                .end();

            httpClient.getAbs(baseUrl + "/Call/brReportTotal" + "?br=" + entries.getValue("brId") + "&from=" + entries.getValue("workDate.__from") + "&to=" + entries.getValue("workDate.__to"),
                res -> res.bodyHandler(b -> {
                    try {
                        defer2.complete(new JsonObject(b.toString()));
                    } catch (Exception ex) {
                        ExceptionUtil.fail(message, ex);
                    }
                }).exceptionHandler(defer2::fail))
                .sendHead()
                .putHeader(gv.X_Requested_With, Services.CALL_CENTER_JAVA)
//                .putHeader("Host", MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
//                .putHeader("Referer", "http://" + MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
                .exceptionHandler(defer2::fail)
                .end();

            Promises.when(defer1.promise(), defer2.promise())
                .then(val -> message.reply(new JsonObject().put("daily", val.getT1()).put("total", val.getT2())))
                .error(e -> ExceptionUtil.fail(message, e));
        } catch (Exception ex) {
            ExceptionUtil.fail(message, ex);
        }
    }

    private String queryString_2(JsonObject criteria) {
        return "?area=" + criteria.getValue(gv.areaId)
            + "&distribution=" + criteria.getValue(gv.distributionHouseId)
            + "&br=" + criteria.getValue(gv.brId)
            + "&startdate=" + criteria.getValue("workDate")
            + "&ptr=" + criteria.getValue("ptr")
            + "&swp=" + criteria.getValue("swp")
            + "&refreshment=" + criteria.getValue("refreshment")
            + "&giveAway=" + criteria.getValue("giveAway")
            + "&packsell=" + criteria.getValue("packsell")
            + "&showTools=" + criteria.getValue("showTools")
            + "&showVideo=" + criteria.getValue("showVideo")
            + "&showVideo=" + criteria.getValue("showVideo")
            + "&recallMode=" + criteria.getValue("recallMode")
            ;
    }

    private String queryString(JsonObject criteria) {

        return "?area=" + criteria.getValue(gv.areaId)
            + "&distribution=" + criteria.getValue(gv.distributionHouseId)
            + "&br=" + criteria.getValue(gv.brId)
            + "&report_start_date=" + ExceptionUtil.toRuntimeCall(() -> URLEncoder.encode(criteria.getString("workDateFrom", ""), StandardCharsets.UTF_8.name()))
            + "&report_end_date=" + ExceptionUtil.toRuntimeCall(() -> URLEncoder.encode(criteria.getString("workDateTo", ""), StandardCharsets.UTF_8.name()))
            + "&ACTIVE=1&ptr_from=" + criteria.getValue("ptrFrom")
            + "&ptr_to=" + criteria.getValue("ptrTo")
            + "&success_from=" + criteria.getValue("successFrom")
            + "&success_to=" + criteria.getValue("successTo")
            + "&call_status="
            + "&recallMode=" + criteria.getValue("recallMode")
            + "&page=" + criteria.getValue("page")
            + "&size=" + criteria.getValue("size")
            + "&DATASOURCE=" + criteria.getValue("DATASOURCE");
    }

    public void contactDetails(Message<JsonObject> message) {
        Promises.from(message.body()).then(entries -> {
            String baseUrl = entries.getString("baseUrl");
            entries.remove("baseUrl");
            httpClient.getAbs(baseUrl + "/Call/contactDetails" + "?sms_id=" + entries.getValue("sms_id"),
                res -> res.bodyHandler(b -> {
                    try {
                        message.reply(new JsonObject(b.toString()));
                    } catch (Exception ex) {
                        ExceptionUtil.fail(message, ex);
                    }
                }).exceptionHandler(e -> ExceptionUtil.fail(message, e)))
                .sendHead()
                .putHeader(gv.X_Requested_With, Services.CALL_CENTER_JAVA)
//                .putHeader("Host", MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
//                .putHeader("Referer", "http://" + MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
                .exceptionHandler(e -> ExceptionUtil.fail(message, e))
                .end();
        }).error(e -> ExceptionUtil.fail(message, e));
    }

    public void findCallOperator(Message<JsonObject> message) {
        Promises.from(message.body()).then(entries -> {
            httpClient.get("/Call/callOperator" + "?id=" + entries.getValue("id"),
                res -> res.bodyHandler(b -> {
                    try {
                        message.reply(new JsonObject(b.toString()));
                    } catch (Exception ex) {
                        ExceptionUtil.fail(message, ex);
                    }
                }).exceptionHandler(e -> ExceptionUtil.fail(message, e)))
                .sendHead()
                .putHeader(gv.X_Requested_With, Services.CALL_CENTER_JAVA)
//                .putHeader("Host", MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
//                .putHeader("Referer", "http://" + MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
                .exceptionHandler(e -> ExceptionUtil.fail(message, e))
                .end();
        }).error(e -> ExceptionUtil.fail(message, e));
    }

    public void findBrand(Message<JsonObject> message) {
        Promises.from(message.body()).then(entries -> {
            String baseUrl = entries.getString("baseUrl");
            entries.remove("baseUrl");
            httpClient.getAbs(baseUrl + "/Call/brands" + "?id=" + entries.getValue("id"),
                res -> res.bodyHandler(b -> {
                    try {
                        message.reply(new JsonArray(b.toString()));
                    } catch (Exception ex) {
                        ExceptionUtil.fail(message, ex);
                    }
                }).exceptionHandler(e -> ExceptionUtil.fail(message, e)))
                .sendHead()
                .putHeader(gv.X_Requested_With, Services.CALL_CENTER_JAVA)
//                .putHeader("Host", MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
//                .putHeader("Referer", "http://" + MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
                .exceptionHandler(e -> ExceptionUtil.fail(message, e))
                .end();
        }).error(e -> ExceptionUtil.fail(message, e));
    }

    public void createCall(Message<JsonObject> message) {
        Promises.from(message.body()).then(entries -> {
            String encode = entries.encode();
            String baseUrl = entries.getString("baseUrl");
            entries.remove("baseUrl");
            httpClient.postAbs(baseUrl + "/Call/createCall",
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

    public void findAllCallOperator(Message<JsonObject> message) {
        Promises.from(message.body()).then(entries -> {
            httpClient.get("/Call/callOperatorList",
                res -> res.bodyHandler(b -> {
                    try {
                        message.reply(new JsonArray(b.toString()));
                    } catch (Exception ex) {
                        ExceptionUtil.fail(message, ex);
                    }
                }).exceptionHandler(e -> ExceptionUtil.fail(message, e)))
                .sendHead()
                .putHeader(gv.X_Requested_With, Services.CALL_CENTER_JAVA)
//                .putHeader("Host", MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
//                .putHeader("Referer", "http://" + MyApp.loadConfig().getString(MainVerticle.PROP_CALL_REVIEW_HOST) + ":" + MyApp.loadConfig().getInteger(MainVerticle.PROP_CALL_REVIEW_PORT))
                .exceptionHandler(e -> ExceptionUtil.fail(message, e))
                .end();
        }).error(e -> ExceptionUtil.fail(message, e));
    }
}
