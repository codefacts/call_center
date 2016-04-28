package com.imslbd.um.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.imslbd.um.*;
import com.imslbd.um.model.*;
import io.crm.ErrorCodes;
import io.crm.transformation.JsonTransformationPipeline;
import io.crm.transformation.impl.json.object.ConverterTransformation;
import io.crm.transformation.impl.json.object.DefaultValueTransformation;
import io.crm.transformation.impl.json.object.IncludeExcludeTransformation;
import io.crm.transformation.impl.json.object.RemoveNullsTransformation;
import io.crm.validator.ValidationPipeline;
import io.crm.validator.ValidationResult;
import io.crm.validator.ValidationResultBuilder;
import io.crm.validator.Validator;
import io.crm.validator.composer.FieldValidatorComposer;
import io.crm.validator.composer.JsonObjectValidatorComposer;
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
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.imslbd.um.UmUtils.limitOffset;
import static com.imslbd.um.service.Services.AUTH_TOKEN;
import static com.imslbd.um.service.Services.converters;
import static io.crm.util.Util.apply;

/**
 * Created by shahadat on 4/7/16.
 */
public class SellService {
    public static final Logger LOGGER = LoggerFactory.getLogger(SellService.class);
    private static final java.lang.String SIZE = "size";
    private static final String PAGE = "page";
    private static final String HEADERS = "headers";
    private static final String PAGINATION = "pagination";
    private static final String DATA = "data";
    private static final Integer DEFAULT_PAGE_SIZE = 100;
    private static final String VALIDATION_ERROR = "validationError";
    private static final String TABLE_NAME = Tables.sells.name();
    private static final String QUANTITY = "quantity";
    private static final Pattern ISO_DATETIME_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    private static final String SELL_UNITS = "sellUnits";

    private final Vertx vertx;
    private final JDBCClient jdbcClient;
    private final DefaultValueTransformation defaultValueTransformationParams = new DefaultValueTransformation(Util.EMPTY_JSON_OBJECT);

    private static final String SELL_NOT_FOUND = "SELL_NOT_FOUND";

    private final RemoveNullsTransformation removeNullsTransformation;

    private final IncludeExcludeTransformation includeExcludeTransformation;
    private final IncludeExcludeTransformation updateIncludeExcludeTransformation;
    private final ConverterTransformation converterTransformation;
    private final DefaultValueTransformation defaultValueTransformation;

    private final JsonTransformationPipeline transformationPipeline;
    private final ValidationPipeline<JsonObject> validationPipeline;

    private final IncludeExcludeTransformation sellUnitIncludeExcludeTransformation;
    private final ConverterTransformation sellUnitConverterTransformation;
    private final DefaultValueTransformation sellUnitDefaultValueTransformation;

    private final JsonTransformationPipeline sellUnitTransformationPipeline;

    private final IncludeExcludeTransformation findAllFiltersExclude;
    private final ConverterTransformation findAllConverterTransformation;

    private final AtomicLong id;
    private final AtomicLong transactionId;
    private final AtomicLong orderId;
    private final String[] fields;
    private final String[] unitFields;
    private final String[] sellUnitFields;
    private final String[] productFields;

    public SellService(JDBCClient jdbcClient, String[] fields, String[] sellUnitFields, String[] productFields, String[] unitFields, final long maxId, final long maxTransactionId, final long maxOrderId, Vertx vertx) {
        this.vertx = vertx;
        this.jdbcClient = jdbcClient;

        {
            this.fields = fields;
            this.sellUnitFields = sellUnitFields;
            this.unitFields = unitFields;
            this.productFields = productFields;
        }

        {
            removeNullsTransformation = new RemoveNullsTransformation();

            ImmutableMap<String, Function<Object, Object>> converters = converters(fields, TABLE_NAME);
            converterTransformation = new ConverterTransformation(converters);
            findAllConverterTransformation = new ConverterTransformation(
                converters.entrySet().stream().collect(Collectors.toMap(e -> "s." + e.getKey(), e -> e.getValue())));

            defaultValueTransformation = new DefaultValueTransformation(
                new JsonObject()
                    .put(Unit.UPDATED_BY, 0)
                    .put(Unit.CREATED_BY, 0)
            );
        }

        {
            sellUnitIncludeExcludeTransformation = new IncludeExcludeTransformation(
                ImmutableSet.copyOf(sellUnitFields), null);
            sellUnitConverterTransformation = new ConverterTransformation(converters(sellUnitFields, Tables.sellUnits.name()));

            sellUnitDefaultValueTransformation = new DefaultValueTransformation(new JsonObject());

            sellUnitTransformationPipeline = new JsonTransformationPipeline(
                ImmutableList.of(
                    new IncludeExcludeTransformation(null, ImmutableSet.of(User.CREATED_BY, User.CREATE_DATE, User.UPDATED_BY, User.UPDATE_DATE)),
                    removeNullsTransformation,
                    defaultValueTransformation,
                    sellUnitConverterTransformation,
                    sellUnitIncludeExcludeTransformation
                )
            );
        }

        {

            includeExcludeTransformation = new IncludeExcludeTransformation(ImmutableSet.copyOf(fields),
                ImmutableSet.of(Sell.SELL_DATE));

            updateIncludeExcludeTransformation = new IncludeExcludeTransformation(ImmutableSet.copyOf(fields), null);

            transformationPipeline = new JsonTransformationPipeline(
                ImmutableList.of(
                    new IncludeExcludeTransformation(null, ImmutableSet.of(User.CREATED_BY, User.CREATE_DATE, User.UPDATED_BY, User.UPDATE_DATE)),
                    converterTransformation,
                    defaultValueTransformation,
                    removeNullsTransformation,
                    js -> {

                        final List<JsonObject> priceList = js.getJsonArray(Sell.SELL_UNITS, Util.EMPTY_JSON_ARRAY).getList();

                        return
                            js.put(Sell.SELL_UNITS, priceList.stream()
                                .map(sellUnitTransformationPipeline::transform)
                                .filter(jss -> jss.getDouble(SellUnit.QUANTITY, 0.0) > 0)
                                .collect(Collectors.toList()));
                    }
                )
            );


            validationPipeline = new ValidationPipeline<>(ImmutableList.copyOf(validators()));
        }

        {
            findAllFiltersExclude = new IncludeExcludeTransformation(Arrays.asList(fields).stream().map(n -> "s." + n).collect(Collectors.toSet()), null);
        }

        id = new AtomicLong(maxId + 1);
        transactionId = new AtomicLong(maxTransactionId + 1);
        orderId = new AtomicLong(maxOrderId + 1);
        orderId.compareAndSet(999, 1);
    }

    private List<Validator<JsonObject>> validators() {
        List<Validator<JsonObject>> validators = new ArrayList<>();
        JsonObjectValidatorComposer validatorComposer = new JsonObjectValidatorComposer(validators, Um.messageBundle)
            .field(Sell.ID,
                fieldValidatorComposer -> fieldValidatorComposer
                    .numberType().nonZero().positive())
            .field(Sell.TRANSACTION_ID,
                fieldValidatorComposer -> fieldValidatorComposer
                    .numberType().nonZero().positive())
            .field(Sell.ORDER_ID,
                fieldValidatorComposer -> fieldValidatorComposer
                    .numberType().nonZero().positive())
            .field(Sell.SELL_DATE,
                fieldValidatorComposer -> fieldValidatorComposer
                    .stringType().pattern(ISO_DATETIME_PATTERN))
            .field(Sell.CONSUMER_NAME,
                FieldValidatorComposer::stringType)
            .field(Sell.CONSUMER_MOBILE,
                FieldValidatorComposer::stringType)
            .field(Sell.REMARKS,
                FieldValidatorComposer::stringType);

        validators.add(
            js -> js.getJsonArray(Sell.SELL_UNITS, Util.EMPTY_JSON_ARRAY).size() > 0 ? null :
                new ValidationResultBuilder()
                    .setErrorCode(UmErrorCodes.SALE_ITEM_MISSING_VALIDATON_ERROR.code())
                    .setField(Sell.SELL_UNITS)
                    .setValue(js.getValue(Sell.SELL_UNITS))
                    .createValidationResult());

        return validatorComposer.getValidatorList();
    }

    public void findAll(Message<JsonObject> message) {

        try {


            final JsonObject pms = message.body() == null
                ? new JsonObject() : removeNullsTransformation.transform(message.body());
            final int page = Converters.toInt(pms.getValue(Pagination.PAGE, 1));
            final int size = Converters.toInt(pms.getValue(Pagination.SIZE, DEFAULT_PAGE_SIZE));

            final JsonObject params = Stream.of(pms)
                .map(findAllConverterTransformation::transform)
                .map(findAllFiltersExclude::transform)
                .map(Services.USER_TRACKING_EXCLUDE_TRANSFORMATION::transform).findAny().get();

            final String from = "from sells s " +
                "join sellUnits su on su.sellId = s.id " +
                "join products p on su.productId = p.id " +
                "join units u on su.unitId = u.id";

            final JsonArray paramsArray = new JsonArray();

            final String where = apply(params.stream()
                    .peek(e -> paramsArray.add(e.getValue()))
                    .map(e -> e.getKey() + " = ?")
                    .collect(Collectors.joining(" and ")),
                whr -> whr.isEmpty() ? "" : "where " + whr);

            final String orderBy = "order by s.id desc";

            Promises
                .when(
                    WebUtils.query(
                        "select " + sellSelect() + " " + from + " " + where + " " +
                            "group by s.id, su.id " +
                            orderBy + " " + limitOffset(page, size), paramsArray, jdbcClient).map(ResultSet::getResults),
                    WebUtils.query(
                        "select count(*) " + from + " " + where, paramsArray, jdbcClient).map(resultSet1 -> resultSet1.getResults().get(0).getLong(0)))
                .map(val -> val.apply(
                    (jsonArrayList, total) -> {

                        final int idIndex = Arrays.asList(fields).indexOf(Sell.ID);

                        final ImmutableList.Builder<JsonObject> objectBuilder = ImmutableList.builder();

                        jsonArrayList.stream().collect(Collectors.groupingBy(jsa -> jsa.getValue(idIndex)))
                            .forEach((id, jsonArrays) -> {

                                objectBuilder.add(composeSell(jsonArrays));
                            });

                        return
                            new JsonObject()
                                .put(DATA, objectBuilder.build()
                                    .stream()
                                    .sorted((s1, s2) -> s2.getInteger(Sell.ID) - s1.getInteger(Sell.ID))
                                    .collect(Collectors.toList()))
                                .put(PAGINATION, new Pagination(page, size, total).toJson());
                    }))
                .then(message::reply)
                .error(e -> ExceptionUtil.fail(message, e));

        } catch (Exception ex) {
            ExceptionUtil.fail(message, ex);
        }
    }

    private JsonObject composeSell(List<JsonArray> jsonArrays) {
        final JsonObject sell = new JsonObject();

        {
            final JsonArray array = jsonArrays.get(0);

            for (int i = 0; i < fields.length; i++) {
                sell.put(fields[i], array.getValue(i));
            }
        }

        ImmutableList.Builder<JsonObject> builder = ImmutableList.builder();
        {

            final int suLimit = fields.length + sellUnitFields.length;
            final int prodLimit = suLimit + productFields.length;
            final int uLimit = prodLimit + unitFields.length;

            jsonArrays.forEach(jsonArray -> {

                final JsonObject sellUnit = new JsonObject();
                for (int i = fields.length; i < suLimit; i++) {
                    sellUnit.put(sellUnitFields[i - fields.length], jsonArray.getValue(i));
                }

                final JsonObject prod = new JsonObject();
                for (int i = suLimit; i < prodLimit; i++) {
                    prod.put(productFields[i - suLimit], jsonArray.getValue(i));
                }

                final JsonObject unit = new JsonObject();
                for (int i = prodLimit; i < uLimit; i++) {
                    unit.put(unitFields[i - prodLimit], jsonArray.getValue(i));
                }

                sellUnit
                    .put(SellUnit.PRODUCT, prod)
                    .put(SellUnit.UNIT, unit);
                builder.add(sellUnit);
            });
        }

        return sell.put(Sell.SELL_UNITS, builder.build());
    }

    public void find(Message<Object> message) {

        Promises
            .callable(message::body)
            .then(
                id -> WebUtils.query(
                    "select " + sellSelect() + " from " + TABLE_NAME + " s " +
                        "join sellUnits su on su.sellId = s.id " +
                        "join products p on su.productId = p.id " +
                        "join units u on su.unitId = u.id " +
                        "where s.id = " + id + " " +
                        "group by s.id, su.id", jdbcClient)
                    .decide(resultSet -> resultSet.getNumRows() < 1 ? SELL_NOT_FOUND : Decision.CONTINUE)
                    .on(SELL_NOT_FOUND,
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
                            .map(rset -> rset.getResults())
                            .map(this::composeSell)
                            .then(message::reply))
                    .error(e -> ExceptionUtil.fail(message, e))
            )
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    private String sellSelect() {

        final String sl = Arrays.asList(fields).stream().map(field -> "s." + field).collect(Collectors.joining(", "));
        final String su = Arrays.asList(sellUnitFields).stream().map(field -> "su." + field).collect(Collectors.joining(", "));
        final String pr = Arrays.asList(productFields).stream().map(field -> "p." + field).collect(Collectors.joining(", "));
        final String u = Arrays.asList(unitFields).stream().map(field -> "u." + field).collect(Collectors.joining(", "));

        return sl + ", " + su + ", " + pr + ", " + u;
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
                    final JsonObject sell = (JsonObject) rsp;

                    WebUtils.getConnection(jdbcClient)
                        .mapToPromise(con -> {
                            final Defer<Void> defer = Promises.defer();
                            con.setAutoCommit(false, Util.makeDeferred(defer));
                            return defer.promise().map(v -> con)
                                .error(e -> con.close());
                        })
                        .then(con -> {

                            try {
                                final List<JsonObject> priceList = sell.getJsonArray(Sell.SELL_UNITS).getList();

                                final long newId = id.getAndIncrement();

                                final JsonObject sll = includeExcludeTransformation
                                    .transform(sell)
                                    .put(Sell.ID, newId)
                                    .put(Sell.TRANSACTION_ID, transactionId.getAndIncrement())
                                    .put(Sell.ORDER_ID, orderId.getAndIncrement())
                                    .put(Sell.SELL_DATE, Converters.toMySqlDateString(new Date()))
                                    .put(User.CREATE_DATE, Converters.toMySqlDateString(new Date()))
                                    .put(User.CREATED_BY, user.getValue(User.ID));

                                final List<JsonObject> sellItems = priceList.stream()
                                    .peek(js -> js.put(SellUnit.SELL_ID, newId))
                                    .collect(Collectors.toList());

                                Promises
                                    .when(
                                        WebUtils.create(TABLE_NAME,
                                            sll, con)
                                            .map(updateResult -> updateResult.getKeys().getLong(0)),
                                        WebUtils.createMulti(Tables.sellUnits.name(), sellItems, con)
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
                                    .then(
                                        v -> vertx.eventBus().publish(UmEvents.SELL_CREATED,
                                            sll.put(Sell.SELL_UNITS, sellItems).put(User.CREATED_BY, user)))
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
                    final JsonObject sell = (JsonObject) rsp;
                    final long id = sell.getLong(Sell.ID);

                    WebUtils.getConnection(jdbcClient)
                        .mapToPromise(con -> {
                            final Defer<Void> defer = Promises.defer();
                            con.setAutoCommit(false, Util.makeDeferred(defer));
                            return defer.promise().map(v -> con)
                                .error(e -> con.close());
                        })
                        .mapToPromise(con -> WebUtils.delete(Tables.sellUnits.name(),
                            new JsonObject()
                                .put(SellUnit.SELL_ID, id), con).map(up -> (SQLConnection) con))
                        .then(con -> {

                            try {
                                List<JsonObject> priceList = sell.getJsonArray(Sell.SELL_UNITS).getList();

                                final JsonObject sll = updateIncludeExcludeTransformation
                                    .transform(sell)
                                    .put(User.UPDATED_BY, user.getValue(User.ID))
                                    .put(User.UPDATE_DATE, Converters.toMySqlDateString(new Date()));

                                final List<JsonObject> items = priceList.stream()
                                    .peek(js -> js.put(SellUnit.SELL_ID, id))
                                    .collect(Collectors.toList());

                                Promises
                                    .when(
                                        WebUtils.update(TABLE_NAME,
                                            sll,
                                            new JsonObject()
                                                .put(Sell.ID, id), con)
                                            .map(updateResult -> updateResult.getUpdated() > 0 ? id : 0),
                                        WebUtils.createMulti(Tables.sellUnits.name(),
                                            items, con)
                                    )
                                    .mapToPromise(t -> {
                                        Defer<Void> defer = Promises.defer();
                                        con.commit(Util.makeDeferred(defer));
                                        return defer.promise()
                                            .map((MapToHandler<Void, MutableTpl2<Long, UpdateResult>>) (v -> t));
                                    })
                                    .then(tpl2 -> tpl2.accept((sellId, list) -> {
                                        message.reply(sellId);
                                    }))
                                    .then(k -> vertx.eventBus().publish(
                                        UmEvents.SELL_UPDATED,
                                        sll.put(Sell.SELL_UNITS, items).put(User.UPDATED_BY, user)))
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
            .error(e -> ExceptionUtil.fail(message, e));
    }

    public void delete(Message<String> message) {

        final JsonObject user = new JsonObject(message.headers().get(AUTH_TOKEN));

        Promises.callable(() -> Converters.toLong(message.body()))
            .mapToPromise(id -> WebUtils.delete(TABLE_NAME, id, jdbcClient)
                .map(updateResult -> updateResult.getUpdated() > 0 ? id : 0)
                .then(message::reply))
            .then(
                v -> vertx.eventBus().publish(UmEvents.SELL_DELETED,
                    new JsonObject().put(Inventory.DELETED_BY, user).put(Inventory.DELETE_DATE, Converters.toMySqlDateString(new Date()))))
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

    public void findDecomposed(Message<Object> message) {

    }
}
