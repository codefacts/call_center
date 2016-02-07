package com.imslbd.call_center.controller;

import com.imslbd.call_center.MyEvents;
import com.imslbd.call_center.MyUris;
import io.crm.promise.Promises;
import io.crm.util.Util;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * Created by shahadat on 1/29/16.
 */
public class DBController {
    private final Vertx vertx;

    public DBController(Vertx vertx, Router router) {
        this.vertx = vertx;
        dataSources(router);
    }

    public void dataSources(Router router) {
        router.get(MyUris.DATA_SOURCES.value).handler(ctx -> Promises.from()
            .mapToPromise(v -> Util.<JsonArray>send(vertx.eventBus(), MyEvents.FIND_ALL_DATA_SOURCES,
                WebUtils.toJson(ctx.request().params())))
            .map(Message::body)
            .then(js -> {
                ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON);
                ctx.response().end(js.encodePrettily());
            })
            .error(ctx::fail));
    }


    public static void main(String... args) {
        String sql = "SELECT " +
            "SMS_INBOX.DATASOURCE_ID, " +
            "CALLS.CALL_DATE, Count(SMS_INBOX.SMS_ID) AS TotalData, " +
            "Count(CALLS.CALL_ID) AS TotalCalls, Nz([TotalData]-[TotalCalls],0) AS Call_Due, " +
            "Count(IIf([CALL_STATUS_ID]=1,True,Null)) AS Success, " +
            "Count(IIf([CALL_STATUS_ID]=1 And [Q01_BR_CONTACT]=True And [TALKING_ABOUT_WHAT]=1,True,Null)) AS TalkedAboutStickDesign, " +
            "Count(IIf([CALL_STATUS_ID]=1 And [Q01_BR_CONTACT]=True And [TALKING_ABOUT_WHAT]=2,True,Null)) AS CantRemember, " +
            "Count(IIf([CALL_STATUS_ID]=1 And [Q01_BR_CONTACT]=True And ([TALKING_ABOUT_WHAT]=1 Or [Q02_NOTICED_NEW_CHANGE]=True),True,Null)) AS Aware, " +
            "Count(IIf([CALL_STATUS_ID]=1 And [Q01_BR_CONTACT]=True,True,Null))-[Aware] AS Unaware, " +
            "Count(IIf([CALL_STATUS_ID]=1 And [Q01_BR_CONTACT]=True And ([TALKING_ABOUT_WHAT]=1 Or [Q02_NOTICED_NEW_CHANGE]=True) And [Q04_OPINION_ABOUT_MODERN_STICK_DESIGN]=1,True,Null)) AS Good, " +
            "Count(IIf([CALL_STATUS_ID]=1 And [Q01_BR_CONTACT]=True And ([TALKING_ABOUT_WHAT]=1 Or [Q02_NOTICED_NEW_CHANGE]=True) And [Q04_OPINION_ABOUT_MODERN_STICK_DESIGN]=2,True,Null)) AS OKorAverage, " +
            "Count(IIf([CALL_STATUS_ID]=1 And [Q01_BR_CONTACT]=True And ([TALKING_ABOUT_WHAT]=1 Or [Q02_NOTICED_NEW_CHANGE]=True) And [Q04_OPINION_ABOUT_MODERN_STICK_DESIGN]=3,True,Null)) AS Bad, " +
            "Count(IIf([CALL_STATUS_ID]=1 And [Q01_BR_CONTACT]=True And ([TALKING_ABOUT_WHAT]=1 Or [Q02_NOTICED_NEW_CHANGE]=True) And [IS_CONSUMER_SAID_ABOUT_TASTE]=True,True,Null)) AS TasteMentioned, " +
            "Nz([Aware]-([Good]+[OKorAverage]+[Bad]),0) AS Error " +
            "FROM SMS_INBOX LEFT JOIN CALLS ON SMS_INBOX.SMS_ID = CALLS.CALL_ID " +
            "GROUP BY SMS_INBOX.DATASOURCE_ID, CALLS.CALL_DATE " +
            "HAVING (((SMS_INBOX.DATASOURCE_ID)=2))";
    }
}
