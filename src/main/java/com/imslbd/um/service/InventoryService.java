package com.imslbd.um.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.imslbd.call_center.MyApp;
import com.imslbd.um.*;
import com.imslbd.um.model.Inventory;
import com.imslbd.um.model.Unit;
import com.imslbd.um.model.User;
import io.crm.ErrorCodes;
import io.crm.pipelines.transformation.JsonTransformationPipeline;
import io.crm.pipelines.transformation.impl.json.object.ConverterTransformation;
import io.crm.pipelines.transformation.impl.json.object.DefaultValueTransformation;
import io.crm.pipelines.transformation.impl.json.object.IncludeExcludeTransformation;
import io.crm.pipelines.transformation.impl.json.object.RemoveNullsTransformation;
import io.crm.pipelines.validator.ValidationPipeline;
import io.crm.pipelines.validator.ValidationResult;
import io.crm.pipelines.validator.Validator;
import io.crm.pipelines.validator.composer.JsonObjectValidatorComposer;
import io.crm.promise.Decision;
import io.crm.promise.Promises;
import io.crm.util.ExceptionUtil;
import io.crm.util.Util;
import io.crm.web.util.Converters;
import io.crm.web.util.Pagination;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.*;
import io.vertx.ext.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.imslbd.um.service.Services.DATA;
import static com.imslbd.um.service.Services.converters;

/**
 * Created by shahadat on 3/6/16.
 */
public class InventoryService {
    public static final Logger LOGGER = LoggerFactory.getLogger(InventoryService.class);
    private static final java.lang.String SIZE = "size";
    private static final String PAGE = "page";
    private static final String HEADERS = "headers";
    private static final String PAGINATION = "pagination";
    private static final String DATA = "data";
    private static final Integer DEFAULT_PAGE_SIZE = 1000;
    private static final String VALIDATION_ERROR = "validationError";
    private static final String TABLE_NAME = Tables.inventories.name();
    private static final String QUANTITY = "quantity";

    private final Vertx vertx;
    private final JDBCClient jdbcClient;
    private final RemoveNullsTransformation removeNullsTransformation;
    private final DefaultValueTransformation defaultValueTransformationParams = new DefaultValueTransformation(Util.EMPTY_JSON_OBJECT);

    private static final String INVENTORY_NOT_FOUND = "INVENTORY_NOT_FOUND";

    private final IncludeExcludeTransformation includeExcludeTransformation;
    private final IncludeExcludeTransformation productIncludeExcludeTransformation;
    private final ConverterTransformation converterTransformation;
    private final ConverterTransformation productConverterTransformation;
    private final DefaultValueTransformation defaultValueTransformation;
    private final DefaultValueTransformation productDefaultValueTransformation;

    private final JsonTransformationPipeline transformationPipeline;
    private final JsonTransformationPipeline productTransformationPipeline;

    private final ValidationPipeline<JsonObject> validationPipeline;

    public InventoryService(JDBCClient jdbcClient, String[] fields, String[] inventoryProductFields, Vertx vertx) {
        this.vertx = vertx;
        this.jdbcClient = jdbcClient;

        removeNullsTransformation = new RemoveNullsTransformation();
        includeExcludeTransformation = new IncludeExcludeTransformation(ImmutableSet.copyOf(fields), null);
        converterTransformation = new ConverterTransformation(converters(fields, TABLE_NAME));

        defaultValueTransformation = new DefaultValueTransformation(
            new JsonObject()
                .put(Unit.UPDATED_BY, 0)
                .put(Unit.CREATED_BY, 0)
        );

        transformationPipeline = new JsonTransformationPipeline(
            ImmutableList.of(
                includeExcludeTransformation,
                converterTransformation,
                defaultValueTransformation,
                removeNullsTransformation
            )
        );

        validationPipeline = new ValidationPipeline<>(ImmutableList.copyOf(validators()));


        productConverterTransformation = new ConverterTransformation(converters(inventoryProductFields, Tables.inventoryProducts.name()));
        productIncludeExcludeTransformation = new IncludeExcludeTransformation(ImmutableSet.copyOf(inventoryProductFields), null);
        productDefaultValueTransformation = new DefaultValueTransformation(
            new JsonObject().put(InventoryProduct.AVAILABLE, 0)
        );

        productTransformationPipeline = new JsonTransformationPipeline(ImmutableList.of(
            productIncludeExcludeTransformation,
            productConverterTransformation,
            productDefaultValueTransformation,
            removeNullsTransformation
        ));
    }

    private List<Validator<JsonObject>> validators() {
        List<Validator<JsonObject>> validators = new ArrayList<>();
        JsonObjectValidatorComposer validatorComposer = new JsonObjectValidatorComposer(validators, Um.messageBundle)
            .field(Inventory.ID,
                fieldValidatorComposer -> fieldValidatorComposer
                    .numberType().nonZero().positive())
            .field(Inventory.NAME,
                fieldValidatorComposer -> fieldValidatorComposer
                    .notNullEmptyOrWhiteSpace()
                    .stringType())
            .field(Inventory.REMARKS,
                fieldValidatorComposer -> fieldValidatorComposer
                    .stringType());
        return validatorComposer.getValidatorList();
    }

    public void findAll(Message<JsonObject> message) {
        Promises.from(message.body())
            .map(defaultValueTransformationParams::transform)
            .map(removeNullsTransformation::transform)
            .then(json -> {
                int page = json.getInteger(PAGE, 1);
                int size = json.getInteger(SIZE, DEFAULT_PAGE_SIZE);
                String from = "from " + TABLE_NAME;

                Promises.when(
                    WebUtils.query("select count(*) as totalCount " + from, jdbcClient)
                        .map(resultSet -> resultSet.getResults().get(0).getLong(0)),
                    WebUtils.query(
                        "select * " + from + " "
                            + UmUtils.limitOffset(page, size), jdbcClient)
                        .map(resultSet3 -> new JsonObject()
                            .put(HEADERS, resultSet3.getColumnNames()
                                .stream()
                                .map(WebUtils::describeField)
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

    public void find(Message<Object> message) {
        Promises
            .callable(message::body)
            .then(
                id -> WebUtils.query("select * from " + TABLE_NAME + " where id = " + id, jdbcClient)
                    .decide(resultSet -> resultSet.getNumRows() < 1 ? INVENTORY_NOT_FOUND : Decision.OTHERWISE)
                    .on(INVENTORY_NOT_FOUND,
                        rs -> {
                            message.reply(
                                new JsonObject()
                                    .put(Services.RESPONSE_CODE, UmErrorCodes.INVENTORY_NOT_FOUND.code())
                                    .put(Services.MESSAGE_CODE, UmErrorCodes.INVENTORY_NOT_FOUND.messageCode())
                                    .put(Services.MESSAGE,
                                        Um.messageBundle.translate(
                                            UmErrorCodes.INVENTORY_NOT_FOUND.messageCode(),
                                            new JsonObject()
                                                .put(Inventory.ID, id))),
                                new DeliveryOptions()
                                    .addHeader(Services.RESPONSE_CODE,
                                        Util.toString(UmErrorCodes.INVENTORY_NOT_FOUND.code()))
                            );
                        })
                    .otherwise(
                        rs -> Promises.from(rs)
                            .map(rset -> rset.getRows().get(0))
                            .then(message::reply))
                    .error(e -> ExceptionUtil.fail(message, e))
            )
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void create(Message<JsonObject> message) {
        System.out.println();
        Promises.callable(() -> transformationPipeline.transform(message.body()))
            .decideAndMap(
                inventory -> {
                    List<ValidationResult> validationResults = validationPipeline.validate(inventory);
                    return validationResults != null
                        ? Decision.of(VALIDATION_ERROR, validationResults)
                        : Decision.of(Decision.OTHERWISE, inventory);
                })
            .on(VALIDATION_ERROR,
                rsp -> {
                    List<ValidationResult> validationResults = (List<ValidationResult>) rsp;
                    System.out.println("REPLYING: " + validationResults);
                    message.reply(
                        new JsonObject()
                            .put(Services.RESPONSE_CODE, ErrorCodes.VALIDATION_ERROR.code())
                            .put(Services.MESSAGE_CODE, ErrorCodes.VALIDATION_ERROR.messageCode())
                            .put(Services.MESSAGE,
                                Um.messageBundle.translate(ErrorCodes.VALIDATION_ERROR.messageCode(),
                                    new JsonObject().put(Services.VALIDATION_RESULTS, validationResults)))
                            .put(Services.VALIDATION_RESULTS,
                                validationResults.stream()
                                    .map(v -> v.addAdditionals(Services.ERROR_CODES_MAP.get(v.getErrorCode())))
                                    .peek(v -> v.message(
                                        Um.messageBundle.translate(
                                            v.getAdditionals().getString(Services.MESSAGE_CODE), v.toJson())))
                                    .map(ValidationResult::toJson)
                                    .collect(Collectors.toList())),
                        new DeliveryOptions()
                            .addHeader(Services.RESPONSE_CODE, ErrorCodes.VALIDATION_ERROR.code() + "")
                    );
                })
            .otherwise(
                rsp -> {
                    JsonObject inventory = (JsonObject) rsp;
                    WebUtils
                        .create(TABLE_NAME, inventory, jdbcClient)
                        .map(updateResult -> updateResult.getKeys().getLong(0))
                        .then(message::reply)
                        .error(e -> ExceptionUtil.fail(message, e));
                })
            .error(e ->
                ExceptionUtil.fail(message, e));
    }

    public void update(Message<JsonObject> message) {
        Promises.callable(() -> transformationPipeline.transform(message.body()))
            .decideAndMap(
                inventory -> {
                    List<ValidationResult> validationResults = validationPipeline.validate(inventory);
                    return validationResults != null ? Decision.of(VALIDATION_ERROR, validationResults) : Decision.of(Decision.OTHERWISE, inventory);
                })
            .on(VALIDATION_ERROR,
                rsp -> {
                    List<ValidationResult> validationResults = (List<ValidationResult>) rsp;
                    message.reply(
                        new JsonObject()
                            .put(Services.RESPONSE_CODE, ErrorCodes.VALIDATION_ERROR.code())
                            .put(Services.MESSAGE_CODE, ErrorCodes.VALIDATION_ERROR.messageCode())
                            .put(Services.MESSAGE,
                                Um.messageBundle.translate(ErrorCodes.VALIDATION_ERROR.messageCode(),
                                    new JsonObject().put(Services.VALIDATION_RESULTS, validationResults)))
                            .put(Services.VALIDATION_RESULTS,
                                validationResults.stream()
                                    .map(v -> v.addAdditionals(Services.ERROR_CODES_MAP.get(v.getErrorCode())))
                                    .peek(v -> v.message(
                                        Um.messageBundle.translate(
                                            v.getAdditionals().getString(Services.MESSAGE_CODE), v.toJson())))
                                    .map(ValidationResult::toJson)
                                    .collect(Collectors.toList())),
                        new DeliveryOptions()
                            .addHeader(Services.RESPONSE_CODE, ErrorCodes.VALIDATION_ERROR.code() + "")
                    );
                })
            .otherwise(
                rsp -> {
                    JsonObject inventory = (JsonObject) rsp;
                    WebUtils.update(
                        TABLE_NAME, inventory,
                        inventory.getLong(Inventory.ID, 0L), jdbcClient)
                        .map(updateResult -> inventory.getValue(Inventory.ID))
                        .then(message::reply)
                        .error(e -> ExceptionUtil.fail(message, e));
                })
            .error(e -> ExceptionUtil.fail(message, e));
    }

    public void delete(Message<Object> message) {
        Promises.callable(() -> Converters.toLong(message.body()))
            .mapToPromise(id -> WebUtils.delete(TABLE_NAME, id, jdbcClient)
                .map(updateResult -> updateResult.getUpdated() > 0 ? id : 0)
                .then(message::reply))
            .error(e ->
                ExceptionUtil.fail(message, e))
        ;
    }

    public static void main(String... args) {
        SecureRandom secureRandom = new SecureRandom();
        String substring = (secureRandom.nextLong() + "").substring(9);
        System.out.println(substring);
        System.out.println(substring.length());
    }

    public void findAllProducts(Message<Object> message) {
        WebUtils.query("select * from " + Tables.inventoryProducts + " " +
            "where inventoryId = " + message.body(), jdbcClient)
            .map(ResultSet::getRows)
            .then(list -> message.reply(
                new JsonObject().put(DATA, list)))
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void insertProduct(Message<JsonObject> message) {

        final JsonObject inventoryProduct = productTransformationPipeline.transform(message.body());

        WebUtils
            .create(Tables.inventoryProducts.name(), inventoryProduct, jdbcClient)
            .map(updateResult -> updateResult.getKeys().getValue(0))
            .then(message::reply)
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void deleteProduct(Message<Object> message) {
        Promises.from(message.body())
            .mapToPromise(id -> WebUtils.delete(Tables.inventoryProducts.name(), id, jdbcClient)
                .map(updateResult -> updateResult.getUpdated() > 0 ? id : 0))
            .then(message::reply)
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void addProduct(Message<JsonObject> message) {
        Promises
            .callable(() -> {
                final JsonObject body = message.body();
                return
                    new JsonObject()
                        .put(InventoryProduct.ID, Converters.toLong(body.getValue(InventoryProduct.ID)))
                        .put(QUANTITY, Converters.toDouble(body.getValue(QUANTITY)))
                    ;
            })
            .mapToPromise(req -> WebUtils.update(
                "UPDATE `" + Tables.inventoryProducts + "` " +
                    "SET `quantity`= `quantity` + " + req.getDouble(QUANTITY) + " " +
                    "WHERE `id` = " + req.getValue(InventoryProduct.ID), jdbcClient)
                .map(updateResult -> (JsonObject) (updateResult.getUpdated() > 0 ? req : null)))
            .then(message::reply)
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void removeProduct(Message<JsonObject> message) {
        Promises
            .callable(() -> {
                final JsonObject body = message.body();
                return
                    new JsonObject()
                        .put(InventoryProduct.ID, Converters.toLong(body.getValue(InventoryProduct.ID)))
                        .put(QUANTITY, Converters.toDouble(body.getValue(QUANTITY)))
                    ;
            })
            .mapToPromise(req -> WebUtils.update(
                "UPDATE `" + Tables.inventoryProducts + "` " +
                    "SET `quantity`= `quantity` - " + req.getDouble(QUANTITY) + " " +
                    "WHERE `id` = " + req.getValue(InventoryProduct.ID), jdbcClient)
                .map(updateResult -> (JsonObject) (updateResult.getUpdated() > 0 ? req : null)))
            .then(message::reply)
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void editProductQuantity(Message<JsonObject> message) {
        Promises
            .callable(() -> {
                final JsonObject body = message.body();
                return
                    new JsonObject()
                        .put(InventoryProduct.ID, Converters.toLong(body.getValue(InventoryProduct.ID)))
                        .put(QUANTITY, Converters.toDouble(body.getValue(QUANTITY)))
                        .put(InventoryProduct.UNIT_ID, Converters.toLong(body.getValue(InventoryProduct.UNIT_ID)))
                    ;
            })
            .mapToPromise(req -> WebUtils.update(
                "UPDATE `" + Tables.inventoryProducts + "` " +
                    "SET `quantity`= " + req.getDouble(QUANTITY) + ", " +
                    "`unitId`= " + req.getValue(InventoryProduct.UNIT_ID) + " " +
                    "WHERE `id` = " + req.getValue(InventoryProduct.ID), jdbcClient)
                .map(updateResult -> (JsonObject) (updateResult.getUpdated() > 0 ? req : null)))
            .then(message::reply)
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }
}
