package com.imslbd.um.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.imslbd.call_center.MainVerticle;
import com.imslbd.um.*;
import com.imslbd.um.model.Inventory;
import com.imslbd.um.model.Unit;
import com.imslbd.um.model.User;
import io.crm.ErrorCodes;
import io.crm.transformation.JsonTransformationPipeline;
import io.crm.transformation.impl.json.object.ConverterTransformation;
import io.crm.transformation.impl.json.object.DefaultValueTransformation;
import io.crm.transformation.impl.json.object.IncludeExcludeTransformation;
import io.crm.transformation.impl.json.object.RemoveNullsTransformation;
import io.crm.validator.ValidationPipeline;
import io.crm.validator.ValidationResult;
import io.crm.validator.Validator;
import io.crm.validator.composer.JsonObjectValidatorComposer;
import io.crm.promise.Decision;
import io.crm.util.ExceptionUtil;
import io.crm.util.Util;
import io.crm.web.util.Converters;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.imslbd.um.service.Services.AUTH_TOKEN;

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
    private final JsonTransformationPipeline transformationPipeline;
    private final String[] fields;
    private final int DEFAULT_PAGE_SIZE = 20;
    private final ValidationPipeline<JsonObject> validationPipeline;
    private final Vertx vertx;

    public UnitService(JDBCClient jdbcClient, String[] fields, Vertx vertx) {
        this.jdbcClient = jdbcClient;
        this.fields = fields;
        this.vertx = vertx;

        includeExcludeTransformation = new IncludeExcludeTransformation(ImmutableSet.copyOf(Arrays.asList(fields)), null);
        converterTransformation = new ConverterTransformation(Services.converters(fields, Tables.units.name()));

        validationPipeline = new ValidationPipeline<>(ImmutableList.copyOf(validators()));
        defaultValueTransformation = new DefaultValueTransformation(
            new JsonObject()
                .put(Unit.UPDATED_BY, 0)
                .put(Unit.CREATED_BY, 0)
        );

        transformationPipeline = new JsonTransformationPipeline(ImmutableList.of(
            new IncludeExcludeTransformation(null, ImmutableSet.of(User.CREATED_BY, User.CREATE_DATE, User.UPDATED_BY, User.UPDATE_DATE)),
            removeNullsTransformation,
            converterTransformation,
            defaultValueTransformation,
            includeExcludeTransformation
        ));
    }

    private List<Validator<JsonObject>> validators() {
        List<Validator<JsonObject>> validators = new ArrayList<>();
        JsonObjectValidatorComposer validatorComposer = new JsonObjectValidatorComposer(validators, Um.messageBundle);
        return validatorComposer.getValidatorList();
    }

    public void findAllUnits(Message<JsonObject> message) {
        JsonObject params = Util.or(message.body(), new JsonObject());

        //Remove Nulls
        params = removeNullsTransformation.transform(params);

        int page = params.getInteger(Services.PAGE, 1);
        int size = params.getInteger(Services.SIZE, DEFAULT_PAGE_SIZE);

        WebUtils.query("select * from units" +
            " order by name asc" +
            " " + UmUtils.limitOffset(page, size), jdbcClient)
            .map(rs ->
                new JsonObject()
                    .put(Services.DATA, rs.getRows()))
            .then(message::reply)
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void findUnit(Message<Long> message) {

        final long id = message.body();

        WebUtils.query("select * from units where id = " + id, jdbcClient)
            .decideAndMap(rs -> Decision.of(rs.getNumRows() <= 0 ? NOT_FOUND
                : Decision.CONTINUE, rs))
            .on(NOT_FOUND,
                resultSet ->
                    message.fail(UmErrorCodes.UNIT_NOT_FOUND.code(),
                        Um.messageBundle.translate(
                            UmErrorCodes.UNIT_NOT_FOUND.messageCode(),
                            new JsonObject()
                                .put(Unit.ID, id))))
            .contnue(
                resultSet ->
                    message.reply(resultSet.getRows().get(0)))
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void createUnit(Message<JsonObject> message) {
        try {

            final JsonObject user = new JsonObject(message.headers().get(AUTH_TOKEN));

            JsonObject js = transformationPipeline.transform(Util.or(message.body(), new JsonObject()));

            final JsonObject unitJson = js;

            List<ValidationResult> results = validationPipeline.validate(unitJson);

            if (results != null) {
                message.fail(ErrorCodes.VALIDATION_ERROR.code(),
                    Um.messageBundle.translate(ErrorCodes.VALIDATION_ERROR.messageCode(),
                        new JsonObject()
                            .put(Services.VALIDATION_RESULTS, results)));
                return;
            }

            unitJson.put(User.CREATED_BY, user.getValue(User.ID))
                .put(User.CREATE_DATE, Converters.toMySqlDateString(new Date()));

            WebUtils.create(Tables.units.name(), unitJson, jdbcClient)
                .map(updateResult -> updateResult.getKeys().getLong(0))
                .then(id -> message.reply(id))
                .error(e -> ExceptionUtil.fail(message, e))
                .then(id -> vertx.eventBus().publish(UmEvents.UNIT_CREATED, unitJson.put(User.CREATED_BY, user)))
            ;

        } catch (Exception ex) {
            LOGGER.error("Error Creating Unit.");
            message.fail(ErrorCodes.SERVER_ERROR.code(), Um.messageBundle.translate(ErrorCodes.SERVER_ERROR.messageCode(), Util.EMPTY_JSON_OBJECT));
        }

    }

    public void updateUnit(Message<JsonObject> message) {
        try {

            final JsonObject user = new JsonObject(message.headers().get(AUTH_TOKEN));

            JsonObject js = transformationPipeline.transform(Util.or(message.body(), new JsonObject()));

            final JsonObject unitJson = js;

            List<ValidationResult> results = validationPipeline.validate(unitJson);

            if (results != null) {
                message.fail(ErrorCodes.VALIDATION_ERROR.code(),
                    Um.messageBundle.translate(ErrorCodes.VALIDATION_ERROR.messageCode(),
                        new JsonObject()
                            .put(Services.VALIDATION_RESULTS, results)));
                return;
            }

            unitJson.put(User.UPDATED_BY, user.getValue(User.ID))
                .put(User.UPDATE_DATE, Converters.toMySqlDateString(new Date()));

            WebUtils.update(Tables.units.name(), unitJson, unitJson.getLong("id"), jdbcClient)
                .map(updateResult -> updateResult.getUpdated() > 0 ? unitJson.getLong("id") : 0)
                .then(message::reply)
                .then(v -> vertx.eventBus().publish(UmEvents.UNIT_UPDATED, unitJson.put(User.UPDATED_BY, user)))
                .error(e -> ExceptionUtil.fail(message, e))
                .then(v -> vertx.eventBus().publish(UmEvents.UNIT_UPDATED, unitJson))
            ;

        } catch (Exception ex) {
            LOGGER.error("Error Creating Unit.");
            message.fail(ErrorCodes.SERVER_ERROR.code(), Um.messageBundle.translate(ErrorCodes.SERVER_ERROR.messageCode(), Util.EMPTY_JSON_OBJECT));
        }
    }

    public void deleteUnit(Message<Number> message) {

        final JsonObject user = new JsonObject(message.headers().get(AUTH_TOKEN));

        final long id = message.body().longValue();
        WebUtils.delete(Tables.units.name(), id, jdbcClient)
            .map(updateResult -> updateResult.getUpdated() > 0 ? id : 0)
            .then(message::reply)
            .then(v -> vertx.eventBus().publish(UmEvents.UNIT_DELETED,
                new JsonObject()
                    .put(Unit.ID, id)
                    .put(Inventory.DELETED_BY, user)
                    .put(Inventory.DELETE_DATE, Converters.toMySqlDateString(new Date()))))
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
