package com.imslbd.um.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import io.crm.web.util.Convert;
import io.crm.web.util.Converters;
import io.crm.web.util.Pagination;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.imslbd.um.service.Services.AUTH_TOKEN;
import static com.imslbd.um.service.Services.converters;
import static io.crm.web.util.WebUtils.multiUpdate;
import static io.crm.web.util.WebUtils.query;
import static io.crm.web.util.WebUtils.update;

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
    private static final String UNIT_ID_MISMATCH = "UNIT_ID_MISMATCH";
    private static final String INVALID_SRC_INVENTORY_ID = "INVALID_SRC_INVENTORY_ID";
    private static final String INVALID_DEST_INVENTORY_ID = "INVALID_DEST_INVENTORY_ID";
    private static final String SRC_QUANTITY_LESS_THAN_DESTINATION_QUANTITY = "SRC_QUANTITY_LESS_THAN_DESTINATION_QUANTITY";

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
    private final String SRC_DEST_SAME = "SRC_DEST_SAME";

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
                new IncludeExcludeTransformation(null, ImmutableSet.of(User.CREATED_BY, User.CREATE_DATE, User.UPDATED_BY, User.UPDATE_DATE)),
                removeNullsTransformation,
                includeExcludeTransformation,
                converterTransformation,
                defaultValueTransformation
            )
        );

        validationPipeline = new ValidationPipeline<>(ImmutableList.copyOf(validators()));


        productConverterTransformation = new ConverterTransformation(converters(inventoryProductFields, Tables.inventoryProducts.name()));
        productIncludeExcludeTransformation = new IncludeExcludeTransformation(ImmutableSet.copyOf(inventoryProductFields), null);
        productDefaultValueTransformation = new DefaultValueTransformation(
            new JsonObject().put(InventoryProduct.AVAILABLE, 0)
        );

        productTransformationPipeline = new JsonTransformationPipeline(ImmutableList.of(
            new IncludeExcludeTransformation(null, ImmutableSet.of(User.CREATED_BY, User.CREATE_DATE, User.UPDATED_BY, User.UPDATE_DATE)),
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
                    query("select count(*) as totalCount " + from, jdbcClient)
                        .map(resultSet -> resultSet.getResults().get(0).getLong(0)),
                    query(
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
                id -> query("select * from " + TABLE_NAME + " where id = " + id, jdbcClient)
                    .decide(resultSet -> resultSet.getNumRows() < 1 ? INVENTORY_NOT_FOUND : Decision.CONTINUE)
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
                    .contnue(
                        rs -> Promises.from(rs)
                            .map(rset -> rset.getRows().get(0))
                            .then(message::reply))
                    .error(e -> ExceptionUtil.fail(message, e))
            )
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void create(Message<JsonObject> message) {

        final JsonObject user = new JsonObject(message.headers().get(AUTH_TOKEN));

        System.out.println();
        Promises.callable(() -> transformationPipeline.transform(message.body()))
            .decideAndMap(
                inventory -> {
                    List<ValidationResult> validationResults = validationPipeline.validate(inventory);
                    return validationResults != null
                        ? Decision.of(VALIDATION_ERROR, validationResults)
                        : Decision.of(Decision.CONTINUE, inventory);
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
            .contnue(
                rsp -> {
                    JsonObject inventory = (JsonObject) rsp;

                    inventory
                        .put(User.CREATE_DATE, Converters.toMySqlDateString(new Date()))
                        .put(User.CREATED_BY, user.getValue(User.ID));

                    WebUtils
                        .create(TABLE_NAME, inventory, jdbcClient)
                        .map(updateResult -> updateResult.getKeys().getLong(0))
                        .then(message::reply)
                        .then(id -> inventory.put(Inventory.ID, id))
                        .then(
                            js -> vertx.eventBus().publish(UmEvents.INVENTORY_CREATED,
                                inventory.put(User.CREATED_BY, user)))
                        .error(e -> ExceptionUtil.fail(message, e));
                })
            .error(e ->
                ExceptionUtil.fail(message, e));
    }

    public void update(Message<JsonObject> message) {

        final JsonObject user = new JsonObject(message.headers().get(AUTH_TOKEN));

        Promises.callable(() -> transformationPipeline.transform(message.body()))
            .decideAndMap(
                inventory -> {
                    List<ValidationResult> validationResults = validationPipeline.validate(inventory);
                    return validationResults != null ? Decision.of(VALIDATION_ERROR, validationResults) : Decision.of(Decision.CONTINUE, inventory);
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
            .contnue(
                rsp -> {
                    JsonObject inventory = (JsonObject) rsp;

                    inventory.put(User.UPDATED_BY, user.getValue(User.ID))
                        .put(User.UPDATE_DATE, Converters.toMySqlDateString(new Date()))
                    ;

                    WebUtils.update(
                        TABLE_NAME, inventory,
                        inventory.getLong(Inventory.ID, 0L), jdbcClient)
                        .map(updateResult -> inventory.getValue(Inventory.ID))
                        .then(message::reply)
                        .then(id -> inventory.put(Inventory.ID, id))
                        .then(
                            js -> vertx.eventBus().publish(UmEvents.INVENTORY_UPDATED,
                                inventory.put(User.UPDATED_BY, user)))
                        .error(e -> ExceptionUtil.fail(message, e));
                })
            .error(e -> ExceptionUtil.fail(message, e));
    }

    public void delete(Message<Object> message) {

        final JsonObject user = new JsonObject(message.headers().get(AUTH_TOKEN));

        Promises.callable(() -> Converters.toLong(message.body()))
            .mapToPromise(id -> WebUtils.delete(TABLE_NAME, id, jdbcClient)
                .map(updateResult -> updateResult.getUpdated() > 0 ? id : 0)
                .then(message::reply))
            .then(id -> vertx.eventBus().publish(UmEvents.INVENTORY_DELETED,
                new JsonObject()
                    .put(Inventory.ID, id)
                    .put(Inventory.DELETED_BY, user)
                    .put(Inventory.DELETE_DATE, Converters.toMySqlDateString(new Date()))))
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
        final String where = message.body() == null ? "" : "where inventoryId = " + message.body();
        query(
            "select * from " + Tables.inventoryProducts + " " + where, jdbcClient)
            .map(ResultSet::getRows)
            .then(list -> message.reply(
                new JsonObject().put(DATA, list)))
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void insertProduct(Message<JsonObject> message) {

        final JsonObject inventoryProduct = productTransformationPipeline.transform(message.body());

        final JsonObject user = new JsonObject(message.headers().get(AUTH_TOKEN));

        inventoryProduct.put(User.CREATED_BY, user.getValue(User.ID));
        inventoryProduct.put(User.CREATE_DATE, Converters.toMySqlDateString(new Date()));

        WebUtils
            .create(Tables.inventoryProducts.name(), inventoryProduct, jdbcClient)
            .map(updateResult -> updateResult.getKeys().getValue(0))
            .then(message::reply)
            .then(v -> vertx.eventBus().publish(UmEvents.NEW_PRODUCT_INSERTED_TO_INVENTORY,
                inventoryProduct
                    .put(User.CREATED_BY, user)))
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void deleteProduct(Message<Object> message) {

        final JsonObject user = new JsonObject(message.headers().get(AUTH_TOKEN));

        Promises.from(message.body())
            .mapToPromise(id -> WebUtils.delete(Tables.inventoryProducts.name(), id, jdbcClient)
                .map(updateResult -> updateResult.getUpdated() > 0 ? id : 0))
            .then(message::reply)
            .then(
                v -> vertx.eventBus().publish(UmEvents.PRODUCT_DELETED_FROM_INVENTORY,
                    new JsonObject()
                        .put(Inventory.DELETED_BY, user)
                        .put(Inventory.DELETE_DATE, Converters.toMySqlDateString(new Date()))))
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void addProduct(Message<JsonObject> message) {

        final JsonObject user = new JsonObject(message.headers().get(AUTH_TOKEN));

        Promises
            .callable(() -> {
                final JsonObject body = message.body();
                return
                    new JsonObject()
                        .put(InventoryProduct.ID, Converters.toLong(body.getValue(InventoryProduct.ID)))
                        .put(QUANTITY, Converters.toDouble(body.getValue(QUANTITY)))
                        .put(User.UPDATED_BY, user.getValue(User.ID))
                        .put(User.UPDATE_DATE, Converters.toMySqlDateString(new Date()))
                    ;
            })
            .mapToPromise(req -> WebUtils.update(
                "UPDATE `" + Tables.inventoryProducts + "` " +
                    "SET `quantity`= `quantity` + " + req.getDouble(QUANTITY) + " " +
                    "WHERE `id` = " + req.getValue(InventoryProduct.ID), jdbcClient)
                .map(updateResult -> (JsonObject) (updateResult.getUpdated() > 0 ? req : null)))
            .then(message::reply)
            .then(jsonObject -> {
                if (jsonObject != null) {
                    vertx.eventBus().publish(UmEvents.PRODUCT_ADDED_TO_INVENTORY,
                        jsonObject
                            .put(User.UPDATED_BY, user));
                }
            })
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void removeProduct(Message<JsonObject> message) {

        final JsonObject user = new JsonObject(message.headers().get(AUTH_TOKEN));

        Promises
            .callable(() -> {
                final JsonObject body = message.body();
                return
                    new JsonObject()
                        .put(InventoryProduct.ID, Converters.toLong(body.getValue(InventoryProduct.ID)))
                        .put(QUANTITY, Converters.toDouble(body.getValue(QUANTITY)))
                        .put(User.UPDATED_BY, user.getValue(User.ID))
                        .put(User.UPDATE_DATE, Converters.toMySqlDateString(new Date()))
                    ;
            })
            .mapToPromise(req -> WebUtils.update(
                "UPDATE `" + Tables.inventoryProducts + "` " +
                    "SET `quantity`= `quantity` - " + req.getDouble(QUANTITY) + " " +
                    "WHERE `id` = " + req.getValue(InventoryProduct.ID), jdbcClient)
                .map(updateResult -> (JsonObject) (updateResult.getUpdated() > 0 ? req : null)))
            .then(message::reply)
            .then(jsonObject -> {
                if (jsonObject != null) {
                    vertx.eventBus().publish(UmEvents.PRODUCT_REMOVED_FROM_INVENTORY,
                        jsonObject
                            .put(User.UPDATED_BY, user));
                }
            })
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void editProductQuantity(Message<JsonObject> message) {

        final JsonObject user = new JsonObject(message.headers().get(AUTH_TOKEN));

        Promises
            .callable(() -> {
                final JsonObject body = message.body();
                return
                    new JsonObject()
                        .put(InventoryProduct.ID, Converters.toLong(body.getValue(InventoryProduct.ID)))
                        .put(QUANTITY, Converters.toDouble(body.getValue(QUANTITY)))
                        .put(InventoryProduct.UNIT_ID, Converters.toLong(body.getValue(InventoryProduct.UNIT_ID)))
                        .put(User.UPDATED_BY, user.getValue(User.ID))
                        .put(User.UPDATE_DATE, Converters.toMySqlDateString(new Date()))
                    ;
            })
            .mapToPromise(req -> WebUtils.update(
                "UPDATE `" + Tables.inventoryProducts + "` " +
                    "SET `quantity`= " + req.getDouble(QUANTITY) + ", " +
                    "`unitId`= " + req.getValue(InventoryProduct.UNIT_ID) + " " +
                    "WHERE `id` = " + req.getValue(InventoryProduct.ID), jdbcClient)
                .map(updateResult -> (JsonObject) (updateResult.getUpdated() > 0 ? req : null)))
            .then(message::reply)
            .then(jsonObject -> {
                if (jsonObject != null) {
                    vertx.eventBus().publish(UmEvents.INVENTORY_PRODUCT_EDITED,
                        jsonObject
                            .put(User.UPDATED_BY, user));
                }
            })
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void transferTo(Message<JsonObject> message) {

        final JsonObject user = new JsonObject(message.headers().get(AUTH_TOKEN));

        Promises
            .callable(() -> {
                JsonObject body = message.body();
                return body
                    .put(InventoryProduct.SRC_INVENTORY_ID,
                        Converters.toLong(body.getValue(InventoryProduct.SRC_INVENTORY_ID)))
                    .put(InventoryProduct.DEST_INVENTORY_ID,
                        Converters.toLong(body.getValue(InventoryProduct.DEST_INVENTORY_ID)))
                    .put(InventoryProduct.PRODUCT_ID,
                        Converters.toLong(body.getValue(InventoryProduct.PRODUCT_ID)))
                    .put(InventoryProduct.QUANTITY,
                        Converters.toDouble(body.getValue(InventoryProduct.QUANTITY)))
                    .put(InventoryProduct.UNIT_ID,
                        Converters.toLong(body.getValue(InventoryProduct.UNIT_ID)))
                    ;
            })
            .map(
                qqr -> qqr
                    .put(InventoryProduct.INVENTORY_ID,
                        qqr.getLong(InventoryProduct.DEST_INVENTORY_ID)))
            .mapToPromise(v -> WebUtils.query("select quantity from inventoryProducts where " +
                "inventoryId = " + v.getLong(InventoryProduct.SRC_INVENTORY_ID) +
                " AND " +
                "productId = " + v.getLong(InventoryProduct.PRODUCT_ID), jdbcClient)
                .map(rs -> (JsonObject) v.put(InventoryProduct.SRC_QUANTITY, rs.getNumRows() <= 0 ? 0 : rs.getResults().get(0).getInteger(0))))
            .decide(
                req -> req.getLong(InventoryProduct.SRC_INVENTORY_ID)
                    .equals(req.getLong(InventoryProduct.DEST_INVENTORY_ID))
                    ? SRC_DEST_SAME
                    : req.getLong(InventoryProduct.SRC_INVENTORY_ID) <= 0
                    ? INVALID_SRC_INVENTORY_ID
                    : req.getLong(InventoryProduct.DEST_INVENTORY_ID) <= 0
                    ? INVALID_DEST_INVENTORY_ID
                    : req.getInteger(InventoryProduct.SRC_QUANTITY) < req.getInteger(InventoryProduct.QUANTITY)
                    ? SRC_QUANTITY_LESS_THAN_DESTINATION_QUANTITY : Decision.CONTINUE)
            .on(SRC_DEST_SAME, val -> message.fail(500, "Destination and source can't be same."))
            .on(INVALID_SRC_INVENTORY_ID, val4 -> message.fail(500, "Invalid source inventory id."))
            .on(INVALID_DEST_INVENTORY_ID, val5 -> message.fail(500, "Invalid destination inventory id."))
            .on(SRC_QUANTITY_LESS_THAN_DESTINATION_QUANTITY, val5 -> message.fail(500, "Insufficient product quantity in the source inventory."))
            .contnue(
                val1 -> Promises.from(val1)
                    .mapToPromise(
                        qqr -> query("SELECT quantity, unitId FROM `inventoryProducts` " +
                            " WHERE " +
                            "inventoryId = " + qqr.getLong(InventoryProduct.INVENTORY_ID) +
                            " AND " +
                            "productId = " + qqr.getLong(InventoryProduct.PRODUCT_ID), jdbcClient)
                            .map(
                                v -> {
                                    double newQuantity = v.getNumRows() > 0 ? v.getResults().get(0).getDouble(0) : 0;

                                    newQuantity += qqr.getDouble(InventoryProduct.QUANTITY);

                                    if (v.getNumRows() > 0) {
                                        long unitId = v.getResults().get(0).getLong(1);
                                        qqr.put(InventoryProduct.UNIT_ID_2, unitId);
                                    }

                                    return (JsonObject)
                                        qqr
                                            .put(InventoryProduct.NEQ_QUANTITY, newQuantity);
                                }))

                    .decide(qqr ->
                        (!qqr.containsKey(InventoryProduct.UNIT_ID_2)
                            || qqr.getLong(InventoryProduct.UNIT_ID)
                            .equals(qqr.getLong(InventoryProduct.UNIT_ID_2))) ? Decision.CONTINUE : UNIT_ID_MISMATCH)

                    .on(UNIT_ID_MISMATCH, val2 -> message.fail(500, "Source and destination unit id must be same."))

                    .contnue(
                        val3 -> Promises.from(val3)
                            .mapToPromise(qqr -> WebUtils.update(
                                "UPDATE `inventoryProducts` SET " +
                                    "`quantity`= quantity - " + qqr.getDouble(InventoryProduct.QUANTITY) +
                                    " WHERE " +
                                    "inventoryId = " + qqr.getLong(InventoryProduct.SRC_INVENTORY_ID) +
                                    " AND " +
                                    "productId = " + qqr.getLong(InventoryProduct.PRODUCT_ID), jdbcClient)
                                .map(v -> (JsonObject) qqr))
                            .mapToPromise(
                                req -> multiUpdate(ImmutableList.of(
                                    "DELETE FROM `inventoryProducts` WHERE inventoryId = " + req.getLong(InventoryProduct.INVENTORY_ID) + " " +
                                        "and productId = " + req.getLong(InventoryProduct.PRODUCT_ID),
                                    "INSERT INTO `inventoryProducts`" +
                                        "(`inventoryId`, `productId`, `quantity`, `available`, `unitId`) VALUES " +
                                        "(" + req.getLong(InventoryProduct.INVENTORY_ID) + " " +
                                        "," + req.getLong(InventoryProduct.PRODUCT_ID) + " " +
                                        "," + req.getDouble(InventoryProduct.NEQ_QUANTITY) + " " +
                                        "," + 0 + " " +
                                        "," + req.getLong(InventoryProduct.UNIT_ID) + ")"
                                ), jdbcClient).map(vk -> val3))
                            .then(message::reply)
                            .then(req -> vertx.eventBus().publish(UmEvents.INVENTORY_PRODUCT_TRANSFERRED,
                                req
                                    .put(User.UPDATED_BY, user)
                                    .put(User.UPDATE_DATE, Converters.toMySqlDateString(new Date()))))
                            .error(e -> ExceptionUtil.fail(message, e)))
                    .error(e -> ExceptionUtil.fail(message, e))
            )
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }
}
