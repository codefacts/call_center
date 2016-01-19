package com.imslbd.call_center.controller;

import com.imslbd.call_center.MyEvents;
import com.imslbd.call_center.MyUris;
import io.crm.promise.Promises;
import io.crm.util.Util;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * Created by shahadat on 1/14/16.
 */
public class BrandController {
    private final Vertx vertx;

    public BrandController(Vertx vertx, Router router) {
        this.vertx = vertx;
        findBrand(router);
    }

    private void findBrand(Router router) {
        router.get(MyUris.BRANDS.value).handler(ctx -> Promises.from()
            .mapToPromise(v -> Util.<JsonArray>send(vertx.eventBus(),
                MyEvents.FIND_BRAND, WebUtils.toJson(ctx.request().params())))
            .map(m -> m.body())
            .then(j -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
            .then(js -> ctx.response().end(js.encodePrettily()))
            .error(ctx::fail));
    }
}
