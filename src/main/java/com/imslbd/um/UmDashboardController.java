package com.imslbd.um;

import com.imslbd.um.service.Services;
import io.crm.web.util.WebUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by shahadat on 4/23/16.
 */
public class UmDashboardController {
    public void dashboard(RoutingContext ctx) {
        try {

            WebUtils.query("select s.sellDate as date, sum(su.total) as totalSales" +
                " from sells s" +
                " join sellUnits su on s.id = su.sellId" +
                " group by DATE(sellDate) order by sellDate", UmApp.getJdbcClient())
                .map(ResultSet::getRows)
                .then(list -> ctx.response().end(
                    new JsonObject()
                        .put(Services.DATA, list).encodePrettily()
                ))
                .error(ctx::fail)
            ;

        } catch (Exception ex) {
            ctx.fail(ex);
        }
    }
}
