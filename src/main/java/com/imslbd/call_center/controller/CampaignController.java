package com.imslbd.call_center.controller;

import com.imslbd.call_center.MyEvents;
import com.imslbd.call_center.MyUris;
import io.crm.util.Util;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;

/**
 * Created by shahadat on 1/28/16.
 */
public class CampaignController {
    private final Vertx vertx;

    public CampaignController(Vertx vertx, Router router) {
        this.vertx = vertx;
        findAllCampaigns(router);
    }

    public void findAllCampaigns(Router router) {
        router.get(MyUris.CAMPAIGNS.value).handler(ctx -> {
            Util.<JsonArray>send(vertx.eventBus(), MyEvents.FIND_ALL_CAMPAIGN, WebUtils.toJson(ctx.request().params()))
                .map(Message::body)
                .then(ja -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                .then(ja -> ctx.response().end(ja.encodePrettily()))
                .error(ctx::fail)
            ;
        });
    }
}
