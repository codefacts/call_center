package com.imslbd.call_center.controller;

import com.imslbd.call_center.MyEvents;
import com.imslbd.call_center.MyUris;
import com.imslbd.call_center.gv;
import com.imslbd.call_center.template.js.CallDetailsSummaryViewJS;
import com.imslbd.call_center.template.js.CallDetailsTemplateJS;
import com.imslbd.call_center.template.js.WorkDayDetailsJS;
import com.imslbd.call_center.template.page.PageTmptBuilder;
import com.imslbd.call_center.template.page.StartPage;
import com.imslbd.call_center.util.MyUtil;
import io.crm.QC;
import io.crm.model.User;
import io.crm.promise.Promises;
import io.crm.util.Util;
import io.crm.web.Uris;
import io.crm.web.controller.AuthController;
import io.crm.web.template.page.ReactDOMBinder;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by someone on 12/11/2015.
 */
public class CallController {
    private final Vertx vertx;

    public CallController(final Vertx vertx, final Router router) {
        this.vertx = vertx;
        step1(router);
        step2(router);
        create(router);
        start(router);
        loginAgent(router);
        logout(router);
    }

    private void start(Router router) {
        router.get(Uris.LOGIN.value).handler(ctx -> Promises.from()
            .mapToPromise(value -> Util.<JsonArray>send(vertx.eventBus(), MyEvents.FIND_ALL_CALL_OPERATOR, null))
            .map(Message::body)
            .then(ja -> ctx.response().end(
                new PageTmptBuilder("Start")
                    .body(new StartPage(ja.stream().map(js -> ((JsonObject) js))
                        .map(js ->
                            new JsonObject().put("id", js.getInteger("CALL_OPERATOR_ID"))
                                .put("name", js.getString("CALL_OPERATOR_NAME")))
                        .sorted((o1, o2) -> o1.getString("name", "")
                            .compareToIgnoreCase(o2.getString("name")))
                        .collect(Collectors.toList()))).build().render()
            )).error(ctx::fail));
    }

    private void loginAgent(Router router) {
        router.post(Uris.LOGIN.value).handler(BodyHandler.create());
        router.post(Uris.LOGIN.value).handler(ctx -> {
            Promises.from()
                .mapToPromise(val -> Util.<JsonObject>send(vertx.eventBus(),
                    MyEvents.FIND_CALL_OPERATOR, WebUtils.toJson(ctx.request().params())))
                .map(Message::body)
                .map(jo -> new JsonObject()
                    .put(QC.username, jo.getString("CALL_OPERATOR_NAME"))
                    .put(QC.userId, jo.getValue("CALL_OPERATOR_ID"))
                    .put(User.mobile, "")
                    .put(QC.userType,
                        new JsonObject()
                            .put(QC.id, 1)
                            .put(QC.name, "Call Agent")))
                .mapToPromise(jo -> AuthController.login(jo, ctx, vertx))
                .error(ctx::fail);
        });
    }

    private void logout(Router router) {
        AuthController.logout(router);
    }

    private void step1(final Router router) {
        router.get(MyUris.STEP_1.value).handler(WebUtils.webHandler(ctx -> {
            ctx.response().end(
                MyUtil.dashboardPage("Step1",
                    ctx.session().get(gv.currentUser), ctx.request().path(),
                    new ReactDOMBinder(
                        new CallDetailsTemplateJS(
                            new CallDetailsSummaryViewJS().render()
                        ).render()
                    )).build().render()
            );
        }));
    }

    private void step2(final Router router) {
        router.get(MyUris.STEP_2.value).handler(WebUtils.webHandler(ctx -> {
            ctx.response().end(
                MyUtil.dashboardPage("Step2",
                    ctx.session().get(gv.currentUser), ctx.request().path(),
                    new ReactDOMBinder(
                        new WorkDayDetailsJS(
                            new CallDetailsSummaryViewJS().render()
                        ).render()
                    )).build().render()
            );
        }));
    }

    private void create(final Router router) {
        router.post(MyUris.CALL_CREATE.value).handler(BodyHandler.create());
        router.post(MyUris.CALL_CREATE.value).handler(ctx -> Promises.from()
            .mapToPromise(v -> Util.<JsonObject>send(vertx.eventBus(), MyEvents.CALL_CREATE, WebUtils.toJson(ctx.request().params())))
            .map(m -> m.body())
            .then(j -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
            .then(js -> ctx.response().end(js.encodePrettily()))
            .error(ctx::fail));
    }

    public static void main(String... args) {
    }
}
