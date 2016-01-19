package com.imslbd.call_center.controller;

import com.imslbd.call_center.MyEvents;
import com.imslbd.call_center.MyUris;
import com.imslbd.call_center.gv;
import io.crm.promise.Promises;
import io.crm.util.Util;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * Created by shahadat on 1/14/16.
 */
public class CallOperator {
    private final Vertx vertx;

    public CallOperator(Vertx vertx, Router router) {
        this.vertx = vertx;
        callOperator(router);
        currentUser(router);
    }

    private void callOperator(Router router) {
        router.get(MyUris.CALL_OPERATOR.value).handler(ctx -> Promises.from()
            .mapToPromise(v -> Util.<JsonObject>send(vertx.eventBus(),
                MyEvents.FIND_CALL_OPERATOR, new JsonObject()
                    .put("id", ((JsonObject) ctx.session().get(gv.currentUser))
                        .getValue(gv.userId))))
            .map(m -> m.body())
            .then(j -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
            .then(js -> ctx.response().end(js.encodePrettily()))
            .error(ctx::fail));
    }

    private void currentUser(Router router) {
        router.get(MyUris.CURRENT_USER.value).handler(ctx -> {
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON);
            ctx.response().end(((JsonObject) ctx.session().get(gv.currentUser)).encodePrettily());
        });
    }
}
