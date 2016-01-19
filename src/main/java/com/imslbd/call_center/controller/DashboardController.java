package com.imslbd.call_center.controller;

import com.imslbd.call_center.MyUris;
import com.imslbd.call_center.template.page.PageTmptBuilder;
import com.imslbd.call_center.template.page.SiteTmpt;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

/**
 * Created by someone on 06/12/2015.
 */
public class DashboardController {
    private final Vertx vertx;

    public DashboardController(Vertx vertx, Router router) {
        this.vertx = vertx;
        index(router);
    }

    private void index(Router router) {
        router.get(MyUris.DASHBOARD.value).handler(ctx -> {
            ctx.response().end(
                    new PageTmptBuilder("Call Center")
                            .body(new SiteTmpt())
                            .build().render()
            );
        });
    }

    public static void main(String... args) {
        System.out.println(
                new PageTmptBuilder("Call Center")
                        .body(new SiteTmpt())
                        .build().render()
        );
    }
}
