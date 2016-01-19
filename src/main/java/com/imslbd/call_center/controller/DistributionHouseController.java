package com.imslbd.call_center.controller;

import com.imslbd.call_center.MyEvents;
import com.imslbd.call_center.MyUris;
import com.imslbd.call_center.gv;
import io.crm.util.Util;
import io.crm.web.util.Converters;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * Created by someone on 08/12/2015.
 */
public class DistributionHouseController {
    private final Vertx vertx;

    public DistributionHouseController(Vertx vertx, Router router) {
        this.vertx = vertx;
        findAll(router);
    }

    public void findAll(Router router) {
        router.get(MyUris.DISTRIBUTION_HOUSES.value).handler(ctx -> {

            final JsonObject entries = new JsonObject();

            long areaId = Converters.toLong(ctx.request().getParam(gv.areaId));
            if (areaId > 0) entries.put(gv.areaId, areaId);

            Util.<JsonObject>send(vertx.eventBus(), MyEvents.FIND_ALL_DISTRIBUTION_HOUSES, entries)
                    .map(m -> m.body())
                    .then(v -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                    .then(js -> ctx.response().end(js.encodePrettily()))
                    .error(ctx::fail)
            ;
        });
    }
}
