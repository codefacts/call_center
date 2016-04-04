package com.imslbd.um.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.imslbd.call_center.MainVerticle;
import com.imslbd.call_center.MyApp;
import com.imslbd.um.*;
import com.imslbd.um.model.Unit;
import io.crm.ErrorCodes;
import io.crm.pipelines.transformation.impl.json.object.ConverterTransformation;
import io.crm.pipelines.transformation.impl.json.object.DefaultValueTransformation;
import io.crm.pipelines.transformation.impl.json.object.IncludeExcludeTransformation;
import io.crm.pipelines.transformation.impl.json.object.RemoveNullsTransformation;
import io.crm.pipelines.validator.ValidationPipeline;
import io.crm.pipelines.validator.ValidationResult;
import io.crm.pipelines.validator.Validator;
import io.crm.pipelines.validator.composer.JsonObjectValidatorComposer;
import io.crm.promise.Decision;
import io.crm.util.ExceptionUtil;
import io.crm.util.Util;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Created by shahadat on 3/27/16.
 */
public class UnitService {
    public static final Logger LOGGER = LoggerFactory.getLogger(UnitService.class);
    private static final String NOT_FOUND = "NOT_FOUND";
    private final JDBCClient jdbcClient;
    private final RemoveNullsTransformation removeNullsTransformation = new RemoveNullsTransformation();
    private final ConverterTransformation converterTransformation;
    private final IncludeExcludeTransformation includeExcludeTransformation;
    private final DefaultValueTransformation defaultValueTransformation;
    private final String[] fields;
    private final int DEFAULT_PAGE_SIZE = 20;
    private final ValidationPipeline<JsonObject> validationPipeline;
    private final Vertx vertx;

    public UnitService(JDBCClient jdbcClient, String[] fields, Vertx vertx) {
        this.jdbcClient = jdbcClient;
        this.fields = fields;
        this.vertx = vertx;

        includeExcludeTransformation = new IncludeExcludeTransformation(ImmutableSet.copyOf(Arrays.asList(fields)), null);
        converterTransformation = new ConverterTransformation(converters(fields));

        validationPipeline = new ValidationPipeline<>(ImmutableList.copyOf(validators()));
        defaultValueTransformation = new DefaultValueTransformation(
            new JsonObject()
                .put(Unit.UPDATED_BY, 0)
                .put(Unit.CREATED_BY, 0)
        );
    }

    private List<Validator<JsonObject>> validators() {
        List<Validator<JsonObject>> validators = new ArrayList<>();
        JsonObjectValidatorComposer validatorComposer = new JsonObjectValidatorComposer(validators, Um.messageBundle);
        return validatorComposer.getValidatorList();
    }

    private ImmutableMap<String, Function<Object, Object>> converters(String[] fields) {
        JsonObject db = MyApp.loadConfig().getJsonObject(Services.DATABASE);
        String url = db.getString("url");
        String user = db.getString("user");
        String password = db.getString("password");

        ImmutableMap.Builder<String, Function<Object, Object>> builder = ImmutableMap.builder();
        try {
            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                Statement statement = connection.createStatement();
                statement.execute("select * from units");
                ResultSet rs = statement.getResultSet();
                ResultSetMetaData metaData = rs.getMetaData();

                int columnCount = metaData.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    int columnType = metaData.getColumnType(i + 1);
                    builder.put(fields[i], Services.TYPE_CONVERTERS.get(columnType));
                    System.out.println(columnType + ": " + Services.JDBC_TYPES.get(columnType));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error connecting to database through jdbc", e);
        }

        return builder.build();
    }

    public void findAllUnits(Message<JsonObject> message) {
        JsonObject params = Util.or(message.body(), new JsonObject());

        //Remove Nulls
        params = removeNullsTransformation.transform(params);

        int page = params.getInteger(Services.PAGE, 1);
        int size = params.getInteger(Services.SIZE, DEFAULT_PAGE_SIZE);

        WebUtils.query("select * from units " + UmUtils.limitOffset(page, size), jdbcClient)
            .map(rs ->
                new JsonObject()
                    .put(Services.DATA, rs.getRows()))
            .then(rs -> message.reply(rs))
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void findUnit(Message<Long> message) {

        final long id = message.body();

        WebUtils.query("select * from units where id = " + id, jdbcClient)
            .decideAndMap(rs -> Decision.of(rs.getNumRows() <= 0 ? NOT_FOUND
                : Decision.OTHERWISE, rs))
            .on(NOT_FOUND,
                resultSet ->
                    message.fail(UmErrorCodes.UNIT_NOT_FOUND.code(),
                        Um.messageBundle.translate(
                            UmErrorCodes.UNIT_NOT_FOUND.messageCode(),
                            new JsonObject()
                                .put(Unit.ID, id))))
            .otherwise(
                resultSet ->
                    message.reply(resultSet.getRows().get(0)))
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void createUnit(Message<JsonObject> message) {
        try {
            JsonObject js = Util.or(message.body(), new JsonObject());
            js = removeNullsTransformation.transform(js);
            js = includeExcludeTransformation.transform(js);
            js = converterTransformation.transform(js);
            js = defaultValueTransformation.transform(js);

            final JsonObject unitJson = js;

            List<ValidationResult> results = validationPipeline.validate(unitJson);

            if (results != null) {
                message.fail(ErrorCodes.VALIDATION_ERROR.code(),
                    Um.messageBundle.translate(ErrorCodes.VALIDATION_ERROR.messageCode(),
                        new JsonObject()
                            .put(Services.VALIDATION_RESULTS, results)));
                return;
            }

            WebUtils.create(Tables.units.name(), unitJson, jdbcClient)
                .then(id -> message.reply(id))
                .error(e -> ExceptionUtil.fail(message, e))
                .then(id -> vertx.eventBus().publish(UmEvents.UNIT_CREATED, unitJson))
            ;

        } catch (Exception ex) {
            LOGGER.error("Error Creating Unit.");
            message.fail(ErrorCodes.SERVER_ERROR.code(), Um.messageBundle.translate(ErrorCodes.SERVER_ERROR.messageCode(), Util.EMPTY_JSON_OBJECT));
        }

    }

    public void updateUnit(Message<JsonObject> message) {
        try {
            JsonObject js = Util.or(message.body(), new JsonObject());
            js = removeNullsTransformation.transform(js);
            js = includeExcludeTransformation.transform(js);
            js = converterTransformation.transform(js);
            js = defaultValueTransformation.transform(js);

            final JsonObject unitJson = js;

            List<ValidationResult> results = validationPipeline.validate(unitJson);

            if (results != null) {
                message.fail(ErrorCodes.VALIDATION_ERROR.code(),
                    Um.messageBundle.translate(ErrorCodes.VALIDATION_ERROR.messageCode(),
                        new JsonObject()
                            .put(Services.VALIDATION_RESULTS, results)));
                return;
            }

            WebUtils.update(Tables.units.name(), unitJson, unitJson.getLong("id"), jdbcClient)
                .then(message::reply)
                .error(e -> ExceptionUtil.fail(message, e))
                .then(v -> vertx.eventBus().publish(UmEvents.UNIT_UPDATED, unitJson))
            ;

        } catch (Exception ex) {
            LOGGER.error("Error Creating Unit.");
            message.fail(ErrorCodes.SERVER_ERROR.code(), Um.messageBundle.translate(ErrorCodes.SERVER_ERROR.messageCode(), Util.EMPTY_JSON_OBJECT));
        }
    }

    public void deleteUnit(Message<Number> message) {
        final long id = message.body().longValue();
        WebUtils.delete(Tables.units.name(), id, jdbcClient)
            .then(message::reply)
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public static void main(String[] ars) throws Exception {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle(), event -> {
        });

        Thread.sleep(2000);

        vertx.eventBus().send(UmEvents.UPDATE_UNIT,
            new JsonObject()
                .put(Unit.ID, 3)
                .put(Unit.NAME, "545454")
                .put(Unit.CREATED_BY, 0), r -> {
                System.out.println(r.result());
            });
    }
}