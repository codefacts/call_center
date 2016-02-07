package com.imslbd.call_center.controller;

import com.imslbd.call_center.MyEvents;
import com.imslbd.call_center.MyUris;
import com.imslbd.call_center.util.MyUtil;
import io.crm.util.Util;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * Created by someone on 08/12/2015.
 */
public class AreaController {
    private final Vertx vertx;

    public AreaController(Vertx vertx, Router router) {
        this.vertx = vertx;
        findAll(router);
    }

    public void findAll(Router router) {
        router.get(MyUris.AREAS.value).handler(ctx -> {
            Util.<JsonObject>send(vertx.eventBus(), MyEvents.FIND_ALL_AREAS, new JsonObject().put("baseUrl", ctx.session().get("baseUrl").toString()))
                .map(m -> m.body())
                .then(v -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                .then(js -> ctx.response().end(js.encodePrettily()))
                .error(ctx::fail)
            ;
        });
    }
}
