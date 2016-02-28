package com.imslbd.call_center.controller;

import com.imslbd.call_center.MyEvents;
import com.imslbd.call_center.MyUris;
import com.imslbd.call_center.gv;
import com.imslbd.call_center.util.MyUtil;
import io.crm.promise.Promises;
import io.crm.util.Util;
import io.crm.web.util.Converters;
import io.crm.web.util.WebUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.stream.Collectors;

import static io.crm.util.Util.*;
import static io.crm.web.util.WebUtils.toJson;

/**
 * Created by someone on 13/12/2015.
 */
public class ConsumerContactController {
    private final Vertx vertx;

    public ConsumerContactController(Vertx vertx, Router router) {
        this.vertx = vertx;
        consumerContactsCallStep_1(router);
        consumerContactsCallStep_2(router);
        brActivitySummary(router);
        contactDetails(router);
    }

    private void contactDetails(Router router) {
        router.get(MyUris.CONTACT_DETAILS.value).handler(ctx -> {
            Util.<JsonObject>send(vertx.eventBus(), MyEvents.CONTACT_DETAILS, WebUtils.toJson(ctx.request().params()).put("baseUrl", ctx.session().get("baseUrl").toString()))
                .map(m -> m.body())
                .then(j -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                .then(js -> ctx.response().end(js.encodePrettily()))
                .error(ctx::fail);
        });
    }

    public void consumerContactsCallStep_1(Router router) {

        router.get(MyUris.CONSUMER_CONTACTS_CALL_STEP_1.value).handler(ctx -> {
            final JsonObject criteria = new JsonObject();

            final MultiMap params = ctx.request().params();
            criteria.put(gv.areaId, Converters.toLong(params.get(gv.areaId)));
            criteria.put(gv.distributionHouseId, Converters.toLong(params.get(gv.distributionHouseId)));
            criteria.put(gv.brId, Converters.toLong(params.get(gv.brId)));

            if (!(criteria.getLong(gv.areaId, 0L) > 0 && criteria.getLong(gv.distributionHouseId, 0L) > 0)) {
                ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end(
                    new JsonObject()
                        .put("status", "error")
                        .put("message", "Please select Area And Distributin House to load data.").encodePrettily()
                );
                return;
            }

            MyUtil.splitPair(params.get("work-date-range"), ":").accept((v1, v2) -> {
                criteria.put(gv.workDateFrom, MyUtil.formatDate(Converters.toDate(v1), null));
                criteria.put(gv.workDateTo, MyUtil.formatDate(Converters.toDate(v2), null));
            });

            criteria.put("recallMode", params.get(gv.recallMode));

            MyUtil.splitPair(params.get(gv.success_range), "-").accept((v1, v2) -> {
                criteria.put(gv.successFrom, isEmptyOrNullOrSpaces(v1) ? null : Converters.toInt(v1));
                criteria.put(gv.successTo, isEmptyOrNullOrSpaces(v2) ? null : Converters.toInt(v2));
            });

            MyUtil.splitPair(params.get(gv.ptr_range), "-").accept((v1, v2) -> {
                criteria.put("ptrFrom", isEmptyOrNullOrSpaces(v1) ? null : Converters.toInt(v1));
                criteria.put("ptrTo", isEmptyOrNullOrSpaces(v2) ? null : Converters.toInt(v2));
            });

            MyUtil.splitPair(params.get(gv.swp_range), "-").accept((v1, v2) -> {
                criteria.put("swpFrom", isEmptyOrNullOrSpaces(v1) ? null : Converters.toInt(v1));
                criteria.put("swpTo", isEmptyOrNullOrSpaces(v2) ? null : Converters.toInt(v2));
            });

            MyUtil.splitPair(params.get(gv.refreshment_range), "-").accept((v1, v2) -> {
                criteria.put("refreshmentFrom", isEmptyOrNullOrSpaces(v1) ? null : Converters.toInt(v1));
                criteria.put("refreshmentTo", isEmptyOrNullOrSpaces(v2) ? null : Converters.toInt(v2));
            });

            MyUtil.splitPair(params.get(gv.give_away_range), "-").accept((v1, v2) -> {
                criteria.put("giveAwayFrom", isEmptyOrNullOrSpaces(v1) ? null : Converters.toInt(v1));
                criteria.put("giveAwayTo", isEmptyOrNullOrSpaces(v2) ? null : Converters.toInt(v2));
            });

            MyUtil.splitPair(params.get(gv.pack_sell_range), "-").accept((v1, v2) -> {
                criteria.put("packsellFrom", isEmptyOrNullOrSpaces(v1) ? null : Converters.toInt(v1));
                criteria.put("packsellTo", isEmptyOrNullOrSpaces(v2) ? null : Converters.toInt(v2));
            });

            MyUtil.splitPair(params.get(gv.show_tools_range), "-").accept((v1, v2) -> {
                criteria.put("showToolsFrom", isEmptyOrNullOrSpaces(v1) ? null : Converters.toInt(v1));
                criteria.put("showToolsTo", isEmptyOrNullOrSpaces(v2) ? null : Converters.toInt(v2));
            });

            MyUtil.splitPair(params.get(gv.show_video_range), "-").accept((v1, v2) -> {
                criteria.put("showVideoFrom", isEmptyOrNullOrSpaces(v1) ? null : Converters.toInt(v1));
                criteria.put("showVideoTo", isEmptyOrNullOrSpaces(v2) ? null : Converters.toInt(v2));
            });

            criteria.put("page", Util.or(params.get("page"), ""))
                .put("size", Util.or(params.get("size"), ""))
                .put("baseUrl", ctx.session().get("baseUrl").toString());

            JsonObject newCriteria = new JsonObject(criteria.stream().filter(e -> e.getValue() != null).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));

            Promises.from()
                .mapToPromise(v -> Util.<JsonObject>send(vertx.eventBus(), MyEvents.CONSUMER_CONTACT_CALL_STEP_1,
                    newCriteria, new DeliveryOptions().setSendTimeout(5 * 60 * 1000)))
                .map(Message::body)
                .then(j -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                .then(js -> ctx.response().end(js.encodePrettily()))
                .error(ctx::fail)
                .error(e ->
                    System.out.println(e));
        });
    }

    public void consumerContactsCallStep_2(Router router) {
        router.get(MyUris.CONSUMER_CONTACTS_CALL_STEP_2.value).handler(ctx -> {

            Promises.from()
                .mapToPromise(v -> Util.<JsonObject>send(vertx.eventBus(), MyEvents.CONSUMER_CONTACT_CALL_STEP_2, WebUtils.toJson(ctx.request().params()).put("baseUrl", ctx.session().get("baseUrl").toString())))
                .map(m -> m.body())
                .then(j -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                .then(js -> ctx.response().end(js.encodePrettily()))
                .error(ctx::fail);
        });
    }

    void brActivitySummary(Router router) {
        router.get(MyUris.BR_ACTIVITY_SUMMARY.value).handler(ctx -> {
            final JsonObject entries = toJson(ctx.request().params());
            WebUtils.splitRange(entries.getString("workDate.__range", ""), ":").accept((s, s2) -> {
                entries.put("workDate.__from", s);
                entries.put("workDate.__to", s2);
            });

            Util.<JsonObject>send(vertx.eventBus(), MyEvents.BR_ACTIVITY_SUMMARY, entries.put("baseUrl", ctx.session().get("baseUrl").toString()))
                .map(m -> m.body())
                .then(js -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                .then(js -> ctx.response().end(js.encodePrettily()))
                .error(ctx::fail)
            ;
        });
    }
}
