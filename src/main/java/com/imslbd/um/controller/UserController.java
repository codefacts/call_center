package com.imslbd.um.controller;

import com.imslbd.call_center.template.page.PageUmTmptBuilder;
import com.imslbd.call_center.template.page.SiteUmTmpt;
import com.imslbd.um.UmEvents;
import io.crm.util.Util;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by shahadat on 3/6/16.
 */
public class UserController {
    private final Vertx vertx;

    public UserController(Vertx vertx) {
        this.vertx = vertx;
    }

    public void index(RoutingContext ctx) {

        ctx.response().end(
            new PageUmTmptBuilder()
                .setPageTitle("Users")
                .setBody(new SiteUmTmpt())
                .createPageUmTmpt().render()
        );
    }

    public void findAllUsers(RoutingContext ctx) {
    }

    public void findUser(RoutingContext ctx) {

    }

    public void createUser(RoutingContext ctx) {

    }

    public void updateUser(RoutingContext ctx) {

    }

    public void deleteUser(RoutingContext ctx) {

    }
}
