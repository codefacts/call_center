package com.imslbd.um.service;

import com.google.common.collect.ImmutableMap;
import com.imslbd.um.UmApp;
import com.imslbd.um.model.User;
import io.crm.promise.Promises;
import io.crm.promise.intfs.Defer;
import io.crm.promise.intfs.Promise;
import io.crm.util.Util;
import io.crm.util.touple.MutableTpl4;
import io.crm.web.util.WebUtils;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by shahadat on 4/12/16.
 */
public class EventDumpingService {
    public static final Logger LOGGER = LoggerFactory.getLogger(EventDumpingService.class);

    private final JDBCClient jdbcClient;
    private Map<String, JsonObject> inventories;
    private Map<String, JsonObject> products;
    private Map<String, JsonObject> units;
    private Map<String, JsonObject> users;

    public EventDumpingService(JDBCClient jdbcClient) {
        this.jdbcClient = jdbcClient;
        loadCache(jdbcClient);
    }

    public Handler<Message<JsonObject>> storeEvent(String event) {
        return msg -> store(msg.body(), event);
    }

    public void store(final JsonObject jsonObject, final String event) {

        final HashSet<String> collections = new HashSet<>();
        JsonObject compose = compose(jsonObject, collections);

        if (collections.size() > 0) {

            loadCache(jdbcClient)
                .then(v -> dump(jsonObject, event))
                .error(e -> LOGGER.error("Error dumping event. Event: " + event + " | Data: " + compose.encode(), e));

        } else {

            dump(jsonObject, event);
        }
    }

    public void dump(JsonObject compose, String event) {
        Defer<String> defer = Promises.defer();
        UmApp.getMongoClient().insert(event, compose, Util.makeDeferred(defer));
        defer.promise()
            .error(e -> LOGGER.error("Error dumping event. Event: " + event + " | Data: " + compose.encode(), e));
    }

    private JsonObject compose(JsonObject jsonObject, HashSet<String> collections) {

        jsonObject.forEach(e -> {

            final Object val = e.getValue();

            if (val instanceof JsonObject) {

                compose((JsonObject) val, collections);

            } else if (val instanceof JsonArray) {

                compose((JsonArray) val, collections);

            } else {

                final String key = e.getKey();

                if (key.endsWith("userId")) {

                    JsonObject user = users.get(val.toString());

                    if (user == null) {
                        collections.add("users");
                    }
                    e.setValue(val == null ? null : user);

                } else if (key.endsWith("unitId")) {

                    JsonObject unit = units.get(val.toString());

                    if (unit == null) {
                        collections.add("units");
                    }
                    e.setValue(val == null ? null : unit);

                } else if (key.endsWith("productId")) {

                    JsonObject product = products.get(val.toString());

                    if (product == null) {
                        collections.add("products");
                    }

                    e.setValue(val == null ? null : product);

                } else if (key.endsWith("inventoryId")) {

                    JsonObject inventory = inventories.get(val.toString());

                    if (inventory == null) {
                        collections.add("inventories");
                    }

                    e.setValue(val != null ? null : inventory);

                } else if (key.equals(User.CREATED_BY)) {

                    JsonObject user = users.get(val.toString());

                    if (user == null) {
                        collections.add("users");
                    }

                    e.setValue(val != null ? null : user);

                } else if (key.equals(User.UPDATED_BY)) {

                    JsonObject user = users.get(val.toString());

                    if (user == null) {
                        collections.add("users");
                    }

                    e.setValue(val != null ? null : user);

                }
            }
        });
        return jsonObject;
    }

    private void compose(JsonArray jsonArray, HashSet<String> collections) {
        jsonArray.forEach(o -> {
            if (o instanceof JsonObject) {
                compose((JsonObject) o, collections);
            } else if (o instanceof JsonArray) {
                compose((JsonArray) o, collections);
            }
        });
    }

    private String key(String kkk) {
        return kkk.substring(0, kkk.lastIndexOf("Id"));
    }

    private Promise<MutableTpl4<ResultSet, ResultSet, ResultSet, ResultSet>> loadCache(JDBCClient jdbcClient) {
        return Promises
            .when(
                WebUtils.query("select * from inventories", jdbcClient),
                WebUtils.query("select * from products", jdbcClient),
                WebUtils.query("select * from units", jdbcClient),
                WebUtils.query("select * from users", jdbcClient)
            )
            .then(val -> val.accept((inv, prod, un, us) -> {
                inventories = ImmutableMap.copyOf(inv.getRows().stream().collect(Collectors.toMap(o -> o.getValue(User.ID).toString(), o -> o)));
                products = ImmutableMap.copyOf(prod.getRows().stream().collect(Collectors.toMap(o -> o.getValue(User.ID).toString(), o -> o)));
                units = ImmutableMap.copyOf(un.getRows().stream().collect(Collectors.toMap(o -> o.getValue(User.ID).toString(), o -> o)));
                users = ImmutableMap.copyOf(us.getRows().stream().collect(Collectors.toMap(o -> o.getValue(User.ID).toString(), o -> o)));
            }))
            .error(e -> LOGGER.error("Error loading EventDumpingService cache", e))
            ;
    }
}
