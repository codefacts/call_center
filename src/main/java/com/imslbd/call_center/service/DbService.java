package com.imslbd.call_center.service;

import io.crm.promise.Promises;
import io.crm.util.ExceptionUtil;
import io.crm.web.util.WebUtils;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;

/**
 * Created by shahadat on 1/29/16.
 */
public class DbService {
    private final JDBCClient jdbcClient;

    public DbService(JDBCClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void findAllDataSources(Message<JsonObject> message) {
        WebUtils.query("select * from dataSources", jdbcClient)
            .map(ResultSet::getRows)
            .then(ja -> message.reply(new JsonArray(ja)))
            .error(e -> ExceptionUtil.fail(message, e));
    }
}
