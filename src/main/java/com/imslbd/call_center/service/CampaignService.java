package com.imslbd.call_center.service;

import io.crm.util.ExceptionUtil;
import io.crm.web.util.WebUtils;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

/**
 * Created by shahadat on 1/23/16.
 */
public class CampaignService {
    private final JDBCClient jdbcClient;

    public CampaignService(JDBCClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void findAllCampaign(Message<JsonObject> message) {
        WebUtils.query("select * from campaigns", jdbcClient)
            .then(rs -> message.reply(new JsonArray(rs.getRows())))
            .error(e -> ExceptionUtil.fail(message, e));
    }

    public void findCampaign(Message<JsonObject> message) {
        WebUtils.query("select * from campaigns where id = " + message.body().getValue("id"), jdbcClient)
            .then(rs -> message.reply(rs.getRows().get(0)))
            .error(e -> ExceptionUtil.fail(message, e));
    }
}
