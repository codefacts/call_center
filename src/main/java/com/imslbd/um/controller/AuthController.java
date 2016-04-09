package com.imslbd.um.controller;

import com.imslbd.um.Tables;
import com.imslbd.um.model.User;
import io.crm.promise.Decision;
import io.crm.web.util.WebUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by shahadat on 4/8/16.
 */
public class AuthController {
    private static final String USER_NOT_FOUND = "user.not.found";
    private static final String PASSWORD_REQUIRED = "password.required";
    private static final String PASSWORD_MISMATCH = "password.mismatch";
    private final JDBCClient jdbcClient;
//    private final JWTAuth authProvider;

    public AuthController(Vertx vertx, JDBCClient jdbcClientUm) {
//        JsonObject authConfig = new JsonObject().put("keyStore", new JsonObject()
//            .put("type", "jceks")
//            .put("path", "keystore.jceks")
//            .put("password", "ihjaeskesbik1bdbp6558934q9sdkas6srtpoae9i"));
//
//        authProvider = JWTAuth.create(vertx, authConfig);
        jdbcClient = jdbcClientUm;
    }

    public void login(RoutingContext ctx) {

        try {

            final JsonObject req = ctx.getBodyAsJson();
            final String username = req.getString(User.USERNAME).trim().toLowerCase();
            String psd = req.getString(User.PASSWORD);

            if (psd == null || psd.isEmpty()) {
                ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
                ctx.response().end(
                    new JsonObject()
                        .put("messageCode", PASSWORD_REQUIRED)
                        .put("message", "Password required.").encode());
                return;
            }

            final String password = psd.trim();

            WebUtils.query("select * from " + Tables.users + " where LOWER(username) = ?",
                new JsonArray().add(username), jdbcClient)
                .decide(
                    resultSet -> resultSet.getNumRows() <= 0
                        ? USER_NOT_FOUND : Decision.OTHERWISE)
                .on(USER_NOT_FOUND, v -> {
                    ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
                    ctx.response().end(
                        new JsonObject()
                            .put("messageCode", USER_NOT_FOUND)
                            .put("message", "User not fount.").encode());
                })
                .otherwise(rs -> {
                    final JsonObject user = rs.getRows().get(0);

                    if (!password.equals(user.getString(User.PASSWORD))) {
                        ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
                        ctx.response().end(
                            new JsonObject()
                                .put("messageCode", PASSWORD_MISMATCH)
                                .put("message", "Password does not match.").encode());
                    } else {

                        user.remove(User.PASSWORD);

                        ctx.response().end(user.encode());
                    }
                })
                .error(ctx::fail)
            ;

        } catch (Exception ex) {
            ctx.fail(ex);
        }
    }

    public void logout(RoutingContext ctx) {

    }

    public void currentUser() {

    }
}
