package com.imslbd.um.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.imslbd.um.Tables;
import com.imslbd.um.Um;
import com.imslbd.um.UmErrorCodes;
import com.imslbd.um.UmUtils;
import com.imslbd.um.model.Product;
import com.imslbd.um.model.ProductUnitPrice;
import com.imslbd.um.model.Unit;
import io.crm.ErrorCodes;
import io.crm.pipelines.transformation.JsonTransformationPipeline;
import io.crm.pipelines.transformation.impl.json.object.ConverterTransformation;
import io.crm.pipelines.transformation.impl.json.object.DefaultValueTransformation;
import io.crm.pipelines.transformation.impl.json.object.IncludeExcludeTransformation;
import io.crm.pipelines.transformation.impl.json.object.RemoveNullsTransformation;
import io.crm.pipelines.validator.ValidationPipeline;
import io.crm.pipelines.validator.ValidationResult;
import io.crm.pipelines.validator.Validator;
import io.crm.pipelines.validator.composer.FieldValidatorComposer;
import io.crm.pipelines.validator.composer.JsonObjectValidatorComposer;
import io.crm.promise.Decision;
import io.crm.promise.Promises;
import io.crm.promise.intfs.Defer;
import io.crm.promise.intfs.MapToHandler;
import io.crm.util.ExceptionUtil;
import io.crm.util.Util;
import io.crm.util.touple.MutableTpl2;
import io.crm.web.util.Converters;
import io.crm.web.util.Pagination;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.imslbd.um.service.Services.converters;

/**
 * Created by shahadat on 4/3/16.
 */
public class ProductService {
    private final AtomicLong id;

    public static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    private static final java.lang.String SIZE = "size";
    private static final String PAGE = "page";
    private static final String HEADERS = "headers";
    private static final String PAGINATION = "pagination";
    private static final String DATA = "data";
    private static final Integer DEFAULT_PAGE_SIZE = 100;
    private static final String VALIDATION_ERROR = "validationError";
    private static final String TABLE_NAME = Tables.products.name();
    private static final String PRODUCT_UNIT_PRICES_TABLE = "productUnitPrices";

    private final Vertx vertx;
    private final JDBCClient jdbcClient;
    private final RemoveNullsTransformation removeNullsTransformation;
    private final DefaultValueTransformation defaultValueTransformationParams =
        new DefaultValueTransformation(Util.EMPTY_JSON_OBJECT);

    private static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";

    private final IncludeExcludeTransformation includeExcludeTransformation;
    private final IncludeExcludeTransformation unitIncludeExcludeTransformation;
    private final ConverterTransformation converterTransformation;
    private final ConverterTransformation unitConverterTransformation;
    private final DefaultValueTransformation defaultValueTransformation;

    private final JsonTransformationPipeline transformationPipeline;
    private final ValidationPipeline<JsonObject> validationPipeline;
    private final ValidationPipeline<JsonObject> unitValidationPipeline;
    private final List<String> productFields;
    private final List<String> productUnitPriceFields;
    private final List<String> unitFields;

    public ProductService(JDBCClient jdbcClient, String[] fields, String[] priceFields, String[] unitFields, long maxId, Vertx vertx) {
        this.vertx = vertx;
        this.jdbcClient = jdbcClient;

        id = new AtomicLong(maxId + 1);

        productFields = ImmutableList.copyOf(fields);
        productUnitPriceFields = ImmutableList.copyOf(priceFields);
        this.unitFields = ImmutableList.copyOf(unitFields);

        removeNullsTransformation = new RemoveNullsTransformation();

        includeExcludeTransformation = new IncludeExcludeTransformation(
            ImmutableSet.<String>builder().addAll(
                Arrays.asList(fields)).build(), ImmutableSet.of(Product.ID));
        unitIncludeExcludeTransformation = new IncludeExcludeTransformation(ImmutableSet.copyOf(priceFields),
            ImmutableSet.of(Unit.ID));

        converterTransformation = new ConverterTransformation(converters(fields, TABLE_NAME));
        unitConverterTransformation = new ConverterTransformation(converters(priceFields, PRODUCT_UNIT_PRICES_TABLE));

        defaultValueTransformation = new DefaultValueTransformation(
            new JsonObject()
                .put(Unit.UPDATED_BY, 0)
                .put(Unit.CREATED_BY, 0)
        );

        transformationPipeline = new JsonTransformationPipeline(
            ImmutableList.of(
                removeNullsTransformation,
                converterTransformation,
                defaultValueTransformation
            )
        );

        validationPipeline = new ValidationPipeline<>(ImmutableList.copyOf(validators()));
        unitValidationPipeline = new ValidationPipeline<>(ImmutableList.copyOf(unitValidators()));
    }

    private List<Validator<JsonObject>> unitValidators() {
        List<Validator<JsonObject>> list = new ArrayList<>();

        return new JsonObjectValidatorComposer(list, Um.messageBundle)
            .field(Unit.ID, fieldValidatorComposer -> fieldValidatorComposer.numberType().positive().notNull().nonZero())
            .field(Unit.NAME, fieldValidatorComposer1 -> fieldValidatorComposer1.stringType().notNullEmptyOrWhiteSpace())
            .field(Unit.FULL_NAME, fieldValidatorComposer2 -> fieldValidatorComposer2.stringType().notNullEmptyOrWhiteSpace())
            .field(Unit.REMARKS, fieldValidatorComposer3 -> fieldValidatorComposer3.stringType().notNullEmptyOrWhiteSpace())
            .getValidatorList()
            ;
    }

    private List<Validator<JsonObject>> validators() {
        List<Validator<JsonObject>> validators = new ArrayList<>();
        return new JsonObjectValidatorComposer(validators, Um.messageBundle)
            .field(Product.NAME,
                fieldValidatorComposer -> fieldValidatorComposer
                    .stringType()
                    .notNullEmptyOrWhiteSpace())
            .field(Product.MANUFACTURER_PRICE,
                fieldValidatorComposer -> fieldValidatorComposer
                    .numberType()
                    .notNull().nonZero().positive())
            .field(Product.MANUFACTURER_PRICE_UNIT_ID,
                fieldValidatorComposer -> fieldValidatorComposer
                    .numberType()
                    .notNull().nonZero().positive())
            .field(Product.REMARKS,
                FieldValidatorComposer::stringType)
            .field(Product.SKU,
                FieldValidatorComposer::stringType)
            .getValidatorList()
            ;
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

        final String pSel = productFields.stream().map(field -> "p." + field).collect(Collectors.joining(", "));
        final String prSel = productUnitPriceFields.stream().map(field -> "up." + field).collect(Collectors.joining(", "));
        final String uSel = unitFields.stream().map(field -> "u." + field).collect(Collectors.joining(", "));

        Promises
            .callable(message::body)
            .then(
                id -> WebUtils.query("select " + pSel + ", " + prSel + ", " + uSel + " " +
                    "from " + TABLE_NAME + " p " +
                    "join " + Tables.productUnitPrices + " up on up.productId = p.id " +
                    "join " + Tables.units + " u on u.id = up.unitId " +
                    "where p.id = " + id, jdbcClient)
                    .decide(resultSet -> resultSet.getNumRows() < 1 ? PRODUCT_NOT_FOUND : Decision.OTHERWISE)
                    .on(PRODUCT_NOT_FOUND,
                        rs ->
                            message.reply(
                                new JsonObject()
                                    .put(Services.RESPONSE_CODE, UmErrorCodes.PRODUCT_NOT_FOUND.code())
                                    .put(Services.MESSAGE_CODE, UmErrorCodes.PRODUCT_NOT_FOUND.messageCode())
                                    .put(Services.MESSAGE,
                                        Um.messageBundle.translate(
                                            UmErrorCodes.PRODUCT_NOT_FOUND.messageCode(),
                                            new JsonObject()
                                                .put(Product.ID, id))),
                                new DeliveryOptions()
                                    .addHeader(Services.RESPONSE_CODE,
                                        Util.toString(UmErrorCodes.PRODUCT_NOT_FOUND.code()))
                            ))
                    .otherwise(
                        rs -> Promises.from(rs)
                            .map(rset -> {
                                JsonObject product = new JsonObject();
                                ImmutableList.Builder<JsonObject> builder = ImmutableList.builder();

                                List<JsonArray> results = rs.getResults();
                                JsonArray array = results.get(0);

                                for (int i = 0; i < productFields.size(); i++) {
                                    product.put(productFields.get(i), array.getValue(i));
                                }

                                final int length = productFields.size() + productUnitPriceFields.size();
                                final int len = length + unitFields.size();

                                results.forEach(jsonArray -> {

                                    JsonObject productUnitPrice = new JsonObject();

                                    for (int i = productFields.size(); i < length; i++) {
                                        productUnitPrice.put(productUnitPriceFields.get(i - productFields.size()),
                                            jsonArray.getValue(i));
                                    }

                                    final JsonObject unit = new JsonObject();
                                    for (int j = length; j < len; j++) {
                                        unit.put(unitFields.get(j - length), jsonArray.getValue(j));
                                    }

                                    productUnitPrice.put(ProductUnitPrice.UNIT, unit);
                                    productUnitPrice.put(ProductUnitPrice.AMOUNT, productUnitPrice.getValue(ProductUnitPrice.PRICE));

                                    builder.add(productUnitPrice);
                                });

                                return product.put(Product.PRICES, builder.build());
                            })
                            .mapToPromise(product -> WebUtils.query(
                                "select * from " + Tables.units + " " +
                                    "where " + Unit.ID + " = " + product.getValue(Product.MANUFACTURER_PRICE_UNIT_ID), jdbcClient)
                                .map(rss -> rss.getRows().stream().findFirst().orElse(new JsonObject()))
                                .map(unit -> product.put(Product.MANUFACTURER_PRICE,
                                    new JsonObject()
                                        .put(ProductUnitPrice.AMOUNT, product.getDouble(Product.MANUFACTURER_PRICE))
                                        .put(ProductUnitPrice.UNIT, unit)))
                            )
                            .then(p -> {
                                System.out.println(p.encodePrettily());
                            })
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
                product -> {

                    List<ValidationResult> validationResults = validationPipeline.validate(product);

                    return validationResults != null
                        ? Decision.of(VALIDATION_ERROR, validationResults)
                        : Decision.of(Decision.OTHERWISE, product);
                })
            .on(VALIDATION_ERROR,
                rsp -> {
                    List<ValidationResult> validationResults = (List<ValidationResult>) rsp;
                    System.out.println("VALIDATION_RESULTS: " + validationResults);
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
                    JsonObject product = (JsonObject) rsp;

                    WebUtils.getConnection(jdbcClient)
                        .mapToPromise(con -> {
                            final Defer<Void> defer = Promises.defer();
                            con.setAutoCommit(false, Util.makeDeferred(defer));
                            return defer.promise().map(v -> con)
                                .error(e -> con.close());
                        })
                        .then(con -> {

                            try {
                                List<JsonObject> priceList = product.getJsonArray(Product.PRICES).getList();

                                final long newId = id.getAndIncrement();

                                Promises
                                    .when(
                                        WebUtils.create(TABLE_NAME,
                                            includeExcludeTransformation
                                                .transform(product)
                                                .put(Product.ID, newId), con)
                                            .map(updateResult -> updateResult.getKeys().getLong(0)),
                                        WebUtils.createMulti(PRODUCT_UNIT_PRICES_TABLE,
                                            priceList.stream()
                                                .map(removeNullsTransformation::transform)
                                                .map(unitConverterTransformation::transform)
                                                .map(unitIncludeExcludeTransformation::transform)
                                                .map(js ->
                                                    js
                                                        .put(Unit.PRODUCT_ID, newId)
                                                        .put(Unit.CREATED_BY, 0)
                                                        .put(Unit.UPDATED_BY, 0))
                                                .collect(Collectors.toList()), con)
                                    )
                                    .mapToPromise(t -> {
                                        Defer<Void> defer = Promises.defer();
                                        con.commit(Util.makeDeferred(defer));
                                        return defer.promise()
                                            .map((MapToHandler<Void, MutableTpl2<Long, UpdateResult>>) (v -> t));
                                    })
                                    .then(tpl2 -> tpl2.accept((id, list) -> {
                                        message.reply(id);
                                    }))
                                    .error(e -> ExceptionUtil.fail(message, e))
                                    .complete(p -> con.close())
                                ;

                            } catch (Exception e) {
                                con.close();
                                ExceptionUtil.fail(message, e);
                            }
                        })
                        .error(e -> ExceptionUtil.fail(message, e))
                    ;
                })
            .error(e ->
                ExceptionUtil.fail(message, e));
    }

    public void update(Message<JsonObject> message) {
        System.out.println();
        Promises.callable(() -> transformationPipeline.transform(message.body()))
            .decideAndMap(
                product -> {

                    List<ValidationResult> validationResults = validationPipeline.validate(product);

                    return validationResults != null
                        ? Decision.of(VALIDATION_ERROR, validationResults)
                        : Decision.of(Decision.OTHERWISE, product);
                })
            .on(VALIDATION_ERROR,
                rsp -> {
                    List<ValidationResult> validationResults = (List<ValidationResult>) rsp;
                    System.out.println("VALIDATION_RESULTS: " + validationResults);
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
                    JsonObject product = (JsonObject) rsp;

                    WebUtils.getConnection(jdbcClient)
                        .mapToPromise(con -> {
                            final Defer<Void> defer = Promises.defer();
                            con.setAutoCommit(false, Util.makeDeferred(defer));
                            return defer.promise().map(v -> con)
                                .error(e -> con.close());
                        })
                        .mapToPromise(
                            con -> WebUtils.delete(Tables.productUnitPrices.name(), new JsonObject()
                                .put(ProductUnitPrice.PRODUCT_ID, product.getValue(Product.ID)), con)
                                .map(v -> (SQLConnection) con))

                        .then(con -> {

                            try {
                                List<JsonObject> priceList = product.getJsonArray(Product.PRICES).getList();
                                final Object productId = product.getValue(Product.ID);
                                Promises
                                    .when(
                                        WebUtils.update(TABLE_NAME,
                                            includeExcludeTransformation
                                                .transform(product),
                                            new JsonObject()
                                                .put(Product.ID, productId), con)
                                            .map(updateResult -> productId),
                                        WebUtils.createMulti(PRODUCT_UNIT_PRICES_TABLE,
                                            priceList.stream()
                                                .map(removeNullsTransformation::transform)
                                                .map(unitConverterTransformation::transform)
                                                .map(unitIncludeExcludeTransformation::transform)
                                                .map(js ->
                                                    js
                                                        .put(Unit.PRODUCT_ID, productId)
                                                        .put(Unit.CREATED_BY, 0)
                                                        .put(Unit.UPDATED_BY, 0))
                                                .collect(Collectors.toList()), con)
                                    )
                                    .mapToPromise(t -> {
                                        Defer<Void> defer = Promises.defer();
                                        con.commit(Util.makeDeferred(defer));
                                        return defer.promise()
                                            .map(v -> (MutableTpl2<Object, UpdateResult>) t);
                                    })
                                    .then(tpl2 -> tpl2.accept(
                                        (id, list) -> message.reply(id)))
                                    .error(e -> ExceptionUtil.fail(message, e))
                                    .complete(p -> con.close())
                                ;

                            } catch (Exception e) {
                                con.close();
                                ExceptionUtil.fail(message, e);
                            }
                        })
                        .error(e -> ExceptionUtil.fail(message, e))
                    ;
                })
            .error(e ->
                ExceptionUtil.fail(message, e));
    }

    public void delete(Message<Object> message) {
        Promises.callable(() -> Converters.toLong(message.body()))
            .mapToPromise(id -> WebUtils.delete(TABLE_NAME, id, jdbcClient)
                .then(message::reply))
            .error(e ->
                ExceptionUtil.fail(message, e))
        ;
    }

    public void findDecomposed(Message<Object> message) {

        final String pSel = productFields.stream().map(field -> "p." + field).collect(Collectors.joining(", "));
        final String prSel = productUnitPriceFields.stream().map(field -> "up." + field).collect(Collectors.joining(", "));

        Promises
            .callable(message::body)
            .then(
                id -> WebUtils.query("select " + pSel + ", " + prSel + " " +
                    "from " + TABLE_NAME + " p " +
                    "join " + Tables.productUnitPrices + " up on up.productId = p.id " +
                    "where p.id = " + id, jdbcClient)
                    .decide(resultSet -> resultSet.getNumRows() < 1 ? PRODUCT_NOT_FOUND : Decision.OTHERWISE)
                    .on(PRODUCT_NOT_FOUND,
                        rs ->
                            message.reply(
                                new JsonObject()
                                    .put(Services.RESPONSE_CODE, UmErrorCodes.PRODUCT_NOT_FOUND.code())
                                    .put(Services.MESSAGE_CODE, UmErrorCodes.PRODUCT_NOT_FOUND.messageCode())
                                    .put(Services.MESSAGE,
                                        Um.messageBundle.translate(
                                            UmErrorCodes.PRODUCT_NOT_FOUND.messageCode(),
                                            new JsonObject()
                                                .put(Product.ID, id))),
                                new DeliveryOptions()
                                    .addHeader(Services.RESPONSE_CODE,
                                        Util.toString(UmErrorCodes.PRODUCT_NOT_FOUND.code()))
                            ))
                    .otherwise(
                        rs -> Promises.from(rs)
                            .map(rset -> {
                                JsonObject product = new JsonObject();
                                ImmutableList.Builder<JsonObject> builder = ImmutableList.builder();

                                List<JsonArray> results = rs.getResults();
                                JsonArray array = results.get(0);

                                for (int i = 0; i < productFields.size(); i++) {
                                    product.put(productFields.get(i), array.getValue(i));
                                }

                                final int length = productFields.size() + productUnitPriceFields.size();

                                results.forEach(jsonArray -> {

                                    JsonObject productUnitPrice = new JsonObject();

                                    for (int i = productFields.size(); i < length; i++) {
                                        productUnitPrice.put(productUnitPriceFields.get(i - productFields.size()),
                                            jsonArray.getValue(i));
                                    }

                                    builder.add(productUnitPrice);
                                });

                                return product.put(Product.PRICES, builder.build());
                            })
                            .then(p -> {
                                System.out.println(p.encodePrettily());
                            })
                            .then(message::reply))
                    .error(e -> ExceptionUtil.fail(message, e))
            )
            .error(e -> ExceptionUtil.fail(message, e))
        ;

    }
}
