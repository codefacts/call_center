package com.imslbd.call_center.controller;

import com.imslbd.call_center.MyApp;
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
import io.crm.promise.Decision;
import io.crm.promise.Promises;
import io.crm.util.Util;
import io.crm.util.touple.immutable.Tpls;
import io.crm.web.Uris;
import io.crm.web.controller.AuthController;
import io.crm.web.template.page.ReactDOMBinder;
import io.crm.web.util.WebUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by someone on 12/11/2015.
 */
public class CallController {
    private final Vertx vertx;
    private static final Set<String> ALLOW_HOSTS = MyApp.loadConfig().getJsonArray("CALL_ALLOW_HOSTS").stream().map(Object::toString).collect(Collectors.toSet());
    private static final Set<String> ALLOW_MASKS = MyApp.loadConfig().getJsonArray("CALL_ALLOW_MASKS").stream().map(Object::toString).collect(Collectors.toSet());

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
        router.get(Uris.LOGIN.value).handler(ctx ->
            Promises.from()
            .mapToPromise(value -> Util.<JsonArray>send(vertx.eventBus(), MyEvents.FIND_ALL_CALL_OPERATOR, null))
            .map(Message::body)
            .mapToPromise(jsonArray -> Util.<JsonArray>send(vertx.eventBus(), MyEvents.FIND_ALL_CAMPAIGN, null)
                .map(Message::body).map(campaigns -> Tpls.of(jsonArray, campaigns)))
            .then(tp -> tp.accept((ja, campaigns) -> ctx.response().end(
                new PageTmptBuilder("Start")
                    .body(new StartPage(ja.stream().map(js -> ((JsonObject) js))
                        .map(js ->
                            new JsonObject().put("id", js.getInteger("CALL_OPERATOR_ID"))
                                .put("name", js.getString("CALL_OPERATOR_NAME")))
                        .sorted((o1, o2) -> o1.getString("name", "")
                            .compareToIgnoreCase(o2.getString("name")))
                        .collect(Collectors.toList()), campaigns.getList())).build().render()
            ))).error(ctx::fail));
    }

    private void loginAgent(Router router) {
        router.post(Uris.LOGIN.value).handler(BodyHandler.create());

//        router.post(Uris.LOGIN.value).handler(ctx -> {
//            Promises.from()
//                .mapToPromise(v -> Util.<JsonObject>send(vertx.eventBus(),
//                    MyEvents.FIND_CALL_OPERATOR,
//                    new JsonObject().put("id",
//                        ctx.request().params().get("agentId"))))
//                .map(Message::body)
//                .map(agent -> {
//                    ctx.session().put("baseUrl", MyUtil.mobiBaseUrl(
//                        MyApp.loadConfig().getString("CALL_REVIEW_HOST"),
//                        MyApp.loadConfig().getInteger("CALL_REVIEW_PORT")));
//                    ctx.session().put(gv.campaign,
//                        new JsonObject()
//                            .put("id", ctx.request().params().get("dataSourceId"))
//                            .put("name", ctx.request().params().get("dataSourceName")));
//                    return agent;
//                })
//                .map(jo -> jo.put("userId", jo.getValue("CALL_OPERATOR_ID"))
//                    .put("username", jo.getValue("CALL_OPERATOR_NAME")))
//                .mapToPromise(jo -> AuthController.login(jo, ctx, vertx))
//                .error(ctx::fail);
//        });

        router.post(Uris.LOGIN.value).handler(ctx ->
            Promises.from()
                .mapToPromise(v ->
                    Promises.when(
                        Util.<JsonObject>send(vertx.eventBus(),
                            MyEvents.FIND_CALL_OPERATOR,
                            new JsonObject().put("id", ctx.request().params().get("agentId"))),
                        Util.<JsonObject>send(vertx.eventBus(),
                            MyEvents.FIND_CAMPAIGN,
                            new JsonObject().put("id", ctx.request().params().get("campaignId"))))
                )
                .map(tpl -> tpl.apply((message1, message21) -> Tpls.of(message1.body(), message21.body())))
                .map(tpl -> tpl.apply((agent, campaign) -> {
                    ctx.session().put("baseUrl",
                        MyUtil.mobiBaseUrl(
                            MyApp.loadConfig().getString("CALL_REVIEW_HOST"),
                            MyApp.loadConfig().getInteger("CALL_REVIEW_PORT")));
                    ctx.session().put(gv.campaign, campaign);
                    return agent;
                }))
                .map(jo -> jo.put("userId", jo.getValue("CALL_OPERATOR_ID"))
                    .put("username", jo.getValue("CALL_OPERATOR_NAME")))
                .mapToPromise(jo -> AuthController.login(jo, ctx, vertx))
                .error(ctx::fail));

//        router.post(Uris.LOGIN.value).handler(ctx -> {
//            Promises.from()
//                .mapToPromise(v ->
//                    Promises.when(
//                        Util.<JsonObject>send(vertx.eventBus(),
//                            MyEvents.FIND_CALL_OPERATOR, new JsonObject().put("id", ctx.request().params().get("agentId"))),
//                        Util.<JsonObject>send(vertx.eventBus(),
//                            MyEvents.FIND_CAMPAIGN, new JsonObject().put("id", ctx.request().params().get("campaignId"))))
//                )
//                .map(tpl -> tpl.apply((message1, message21) -> Tpls.of(message1.body(), message21.body())))
//                .map(tpl -> tpl.apply((agent, campaign) -> {
//                    ctx.session().put("baseUrl", MyUtil.mobiBaseUrl(campaign.getString("host"), campaign.getInteger("port")));
//                    ctx.session().put(gv.campaign, campaign);
//                    return agent;
//                }))
//                .map(jo -> jo.put("userId", jo.getValue("CALL_OPERATOR_ID"))
//                    .put("username", jo.getValue("CALL_OPERATOR_NAME")))
//                .mapToPromise(jo -> AuthController.login(jo, ctx, vertx))
//                .error(ctx::fail);
//        });
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
            .decide(v -> !(ALLOW_HOSTS.contains(ctx.request().remoteAddress().host())
                || ALLOW_MASKS.stream().filter(m -> ctx.request().remoteAddress()
                .host().startsWith(m)).findAny().isPresent()) ? "DENY" : Decision.OTHERWISE)
            .on("DENY", v -> {
                ctx.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code());
                ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON);
                ctx.response().end(new JsonObject().put("status", "error")
                    .put("message", "Unauthorized Access.")
                    .put("url", ctx.request().absoluteURI())
                    .put("host", ctx.request().remoteAddress().host()).encodePrettily());
            })
            .otherwise(v -> Util.<JsonObject>send(vertx.eventBus(), MyEvents.CALL_CREATE, WebUtils.toJson(ctx.request()
                .params())
                .put("DATASOURCE", Util.as(ctx.session().get(gv.campaign), JsonObject.class).getInteger("id"))
                .put("baseUrl", ctx.session().get("baseUrl").toString()))
                .map(m -> m.body())
                .then(j -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                .then(js -> ctx.response().end(js.encodePrettily()))
                .error(ctx::fail)));
    }

    public static void main(String... args) {
    }
}
