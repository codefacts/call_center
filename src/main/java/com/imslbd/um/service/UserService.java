package com.imslbd.um.service;

import com.imslbd.um.DataTypes;
import com.imslbd.um.UmUtils;
import com.imslbd.um.ex.UserNotFoundException;
import com.imslbd.um.model.User;
import io.crm.pipelines.transformation.impl.json.object.NullToEmptyObject;
import io.crm.pipelines.transformation.impl.json.object.RemoveNullsTransformation;
import io.crm.promise.Promises;
import io.crm.util.ExceptionUtil;
import io.crm.util.Util;
import io.crm.web.util.Pagination;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by shahadat on 3/6/16.
 */
public class UserService {
    private static final java.lang.String SIZE = "size";
    private static final String PAGE = "page";
    private static final String HEADERS = "headers";
    private static final String PAGINATION = "pagination";
    private static final String DATA = "data";
    private static final String FIELD = "field";
    private static final Integer DEFAULT_PAGE_SIZE = 100;
    private static final String LABEL = "label";
    private static final String IS_KEY = "isKey";
    private static final String DATA_TYPE = "dataType";

    private final Vertx vertx;
    private final JDBCClient jdbcClient;
    private final RemoveNullsTransformation removeNulls = new RemoveNullsTransformation();
    private final NullToEmptyObject nullToEmptyObject = new NullToEmptyObject();

    public UserService(Vertx vertx, JDBCClient jdbcClient) {
        this.vertx = vertx;
        this.jdbcClient = jdbcClient;
    }

    public void findAllUsers(Message<JsonObject> message) {
        Promises.from(message.body())
            .map(nullToEmptyObject::transform)
            .map(removeNulls::transform)
            .then(json -> {
                int page = json.getInteger(PAGE, 1);
                int size = json.getInteger(SIZE, DEFAULT_PAGE_SIZE);
                String from = "from users";

                Promises.when(
                    WebUtils.query("select count(*) as totalCount " + from, jdbcClient)
                        .map(resultSet -> resultSet.getResults().get(0).getLong(0)),
                    WebUtils.query(
                        "select * " + from + " "
                            + UmUtils.limitOffset(page, size), jdbcClient)
                        .map(resultSet3 -> new JsonObject()
                            .put(HEADERS, resultSet3.getColumnNames()
                                .stream()
                                .map(
                                    field -> {

                                        final String title = Util.parseCamelOrSnake(field);

                                        JsonObject jsonObject = new JsonObject()
                                            .put(FIELD, field)
                                            .put(LABEL, title);

                                        {
                                            if (field.equals("id")) {
                                                jsonObject.put(IS_KEY, true);
                                            }
                                        }

                                        {
                                            if (WebUtils.inferDateTypeFromTitle(title)) {
                                                jsonObject.put(DATA_TYPE, DataTypes.DATE);
                                            }
                                        }

                                        return jsonObject;
                                    })
                                .collect(Collectors.toList()))
                            .put(DATA, resultSet3.getRows())))
                    .map(tpl2 -> tpl2.apply(
                        (totalCount, js) ->
                            js.put(PAGINATION,
                                new Pagination(page, size, totalCount).toJson())))
                    .then(message::reply)
                    .error(e -> ExceptionUtil.fail(message, e))
                ;
            })
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void findUser(Message<Number> message) {
        Promises
            .fromCallable(() -> {
                Long id = message.body().longValue();
                return id;
            })
            .mapToPromise(
                id -> WebUtils.query("select * from users where id = " + id, jdbcClient)
                    .map(resultSet -> {

                        if (resultSet.getNumRows() <= 0) {
                            throw new UserNotFoundException();
                        }

                        return resultSet.getRows().get(0);
                    })
                    .map(user -> {
                        user.remove(User.PASSWORD);
                        return user;
                    }))
            .then(message::reply)
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void createUser(Message<JsonObject> message) {

    }

    public void updateUser(Message<JsonObject> message) {

    }

    public void deleteUser(Message<JsonObject> message) {

    }

    public static void main(String... args) {
        SecureRandom secureRandom = new SecureRandom();
        String substring = (secureRandom.nextLong() + "").substring(9);
        System.out.println(substring);
        System.out.println(substring.length());
    }
}
