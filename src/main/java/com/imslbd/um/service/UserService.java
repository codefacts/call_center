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
import io.crm.pipelines.validator.ValidationResultBuilder;
import io.crm.pipelines.validator.Validator;
import io.crm.pipelines.validator.composer.JsonObjectValidatorComposer;
import io.crm.promise.Decision;
import io.crm.promise.Promises;
import io.crm.util.ExceptionUtil;
import io.crm.util.Util;
import io.crm.util.touple.immutable.Tpl2;
import io.crm.util.touple.immutable.Tpls;
import io.crm.web.util.Convert;
import io.crm.web.util.Converters;
import io.crm.web.util.Pagination;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static com.imslbd.um.service.Services.AUTH_TOKEN;
import static com.imslbd.um.service.Services.converters;

/**
 * Created by shahadat on 3/6/16.
 */
public class UserService {
    public static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private static final java.lang.String SIZE = "size";
    private static final String PAGE = "page";
    private static final String HEADERS = "headers";
    private static final String PAGINATION = "pagination";
    private static final String DATA = "data";
    private static final Integer DEFAULT_PAGE_SIZE = 100;
    private static final String VALIDATION_ERROR = "validationError";
    private static final String PASSWORD_MISMATCH = "PASSWORD_MISMATCH";

    private final Vertx vertx;
    private final JDBCClient jdbcClient;
    private final RemoveNullsTransformation removeNullsTransformation;
    private final DefaultValueTransformation defaultValueTransformationParams = new DefaultValueTransformation(Util.EMPTY_JSON_OBJECT);

    private static final String USER_NOT_FOUND = "USER_NOT_FOUND";

    private final IncludeExcludeTransformation includeExcludeTransformation;
    private final ConverterTransformation converterTransformation;
    private final DefaultValueTransformation defaultValueTransformation;

    private final JsonTransformationPipeline transformationPipeline;
    private final JsonTransformationPipeline createTransformationPipeline;
    private final ValidationPipeline<JsonObject> createValidationPipeline;
    private final ValidationPipeline<JsonObject> validationPipeline;

    private final ValidationPipeline<JsonObject> changePasswordValidationPipeline;
    private final IncludeExcludeTransformation changePasswordIncludeExcludeTransformation = new IncludeExcludeTransformation(ImmutableSet.of(User.ID, User.PASSWORD), null);

    public UserService(JDBCClient jdbcClient, String[] fields, Vertx vertx) {
        this.vertx = vertx;
        this.jdbcClient = jdbcClient;

        removeNullsTransformation = new RemoveNullsTransformation();
        includeExcludeTransformation = new IncludeExcludeTransformation(
            ImmutableSet.copyOf(Arrays.asList(fields)), null);
        converterTransformation = new ConverterTransformation(converters(fields, Tables.users.name()));

        defaultValueTransformation = new DefaultValueTransformation(
            new JsonObject()
                .put(Unit.UPDATED_BY, 0)
                .put(Unit.CREATED_BY, 0)
        );

        createTransformationPipeline = new JsonTransformationPipeline(
            ImmutableList.of(
                new IncludeExcludeTransformation(null, ImmutableSet.of(User.CREATED_BY, User.CREATE_DATE, User.UPDATED_BY, User.UPDATE_DATE)),
                removeNullsTransformation,
                includeExcludeTransformation,
                converterTransformation,
                defaultValueTransformation
            )
        );

        transformationPipeline = new JsonTransformationPipeline(
            ImmutableList.of(
                new IncludeExcludeTransformation(null, ImmutableSet.of(User.CREATED_BY, User.CREATE_DATE, User.UPDATED_BY, User.UPDATE_DATE)),
                removeNullsTransformation,
                includeExcludeTransformation,
                new IncludeExcludeTransformation(null, ImmutableSet.of(User.PASSWORD)),
                converterTransformation,
                defaultValueTransformation
            )
        );

        validationPipeline = new ValidationPipeline<>(ImmutableList.copyOf(validators()));
        createValidationPipeline = new ValidationPipeline<>(ImmutableList.copyOf(creationValidators()));

        changePasswordValidationPipeline = new ValidationPipeline<>(ImmutableList.copyOf(changePasswordValidators()));
    }

    private List<Validator<JsonObject>> changePasswordValidators() {
        List<Validator<JsonObject>> validators = new ArrayList<>();
        JsonObjectValidatorComposer validatorComposer = new JsonObjectValidatorComposer(validators, Um.messageBundle)
            .field(User.ID, fieldValidatorComposer -> fieldValidatorComposer.numberType().notNull().positive().nonZero())
            .field(User.CURRENT_PASSWORD,
                fieldValidatorComposer -> fieldValidatorComposer
                    .notNullEmptyOrWhiteSpace()
                    .stringType())
            .field(User.PASSWORD,
                fieldValidatorComposer -> fieldValidatorComposer
                    .notNullEmptyOrWhiteSpace()
                    .stringType())
            .field(User.RETYPE_PASSWORD,
                fieldValidatorComposer -> fieldValidatorComposer
                    .notNullEmptyOrWhiteSpace()
                    .stringType());

        validators.add(user -> {
            String retypePsd = Util.or(user.getString(User.RETYPE_PASSWORD), "");
            String password = Util.or(user.getString(User.PASSWORD), "");
            if (!password.equals(retypePsd))
                return new ValidationResultBuilder()
                    .setField(User.RETYPE_PASSWORD)
                    .setErrorCode(UmErrorCodes.TWO_PASSWORD_MISMATCH.code())
                    .createValidationResult();
            else {
                return null;
            }
        });
        return validatorComposer.getValidatorList();
    }

    private List<Validator<JsonObject>> creationValidators() {
        List<Validator<JsonObject>> validators = new ArrayList<>();
        JsonObjectValidatorComposer validatorComposer = new JsonObjectValidatorComposer(validators, Um.messageBundle)
            .field(User.USERNAME,
                fieldValidatorComposer -> fieldValidatorComposer.stringType().notNullEmptyOrWhiteSpace())
            .field(User.PASSWORD,
                (fieldValidatorComposer) -> fieldValidatorComposer.stringType().notNullEmptyOrWhiteSpace())
            .field(User.NAME,
                fieldValidatorComposer -> fieldValidatorComposer.stringType().notNullEmptyOrWhiteSpace())
            .field(User.PHONE,
                fieldValidatorComposer -> fieldValidatorComposer.stringType().notNullEmptyOrWhiteSpace());
        return validatorComposer.getValidatorList();
    }

    private List<Validator<JsonObject>> validators() {
        List<Validator<JsonObject>> validators = new ArrayList<>();
        JsonObjectValidatorComposer validatorComposer = new JsonObjectValidatorComposer(validators, Um.messageBundle)
            .field(User.USERNAME,
                fieldValidatorComposer -> fieldValidatorComposer.stringType().notNullEmptyOrWhiteSpace())
            .field(User.NAME,
                fieldValidatorComposer -> fieldValidatorComposer.stringType().notNullEmptyOrWhiteSpace())
            .field(User.PHONE,
                fieldValidatorComposer -> fieldValidatorComposer.stringType().notNullEmptyOrWhiteSpace());
        return validatorComposer.getValidatorList();
    }

    public void findAll(Message<JsonObject> message) {
        Promises.from(message.body())
            .map(defaultValueTransformationParams::transform)
            .map(removeNullsTransformation::transform)
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
            .callable(() -> message.body())
            .then(
                id -> WebUtils.query("select * from users where id = " + id, jdbcClient)
                    .decide(resultSet -> resultSet.getNumRows() < 1 ? USER_NOT_FOUND : Decision.CONTINUE)
                    .on(USER_NOT_FOUND,
                        rs -> {
                            message.reply(
                                new JsonObject()
                                    .put(Services.RESPONSE_CODE, UmErrorCodes.USER_NOT_FOUND.code())
                                    .put(Services.MESSAGE_CODE, UmErrorCodes.USER_NOT_FOUND.messageCode())
                                    .put(Services.MESSAGE,
                                        Um.messageBundle.translate(
                                            UmErrorCodes.USER_NOT_FOUND.messageCode(),
                                            new JsonObject()
                                                .put(User.ID, id))),
                                new DeliveryOptions()
                                    .addHeader(Services.RESPONSE_CODE, Util.toString(UmErrorCodes.USER_NOT_FOUND.code()))
                            );
                        })
                    .contnue(
                        rs -> Promises.from(rs)
                            .map(rset -> rset.getRows().get(0))
                            .map(user -> {
                                user.remove(User.PASSWORD);
                                return user;
                            })
                            .then(message::reply))
                    .error(e -> ExceptionUtil.fail(message, e))
            )
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void create(Message<JsonObject> message) {

        final JsonObject currentUser = new JsonObject(message.headers().get(AUTH_TOKEN));

        System.out.println();
        Promises.callable(() -> createTransformationPipeline.transform(message.body()))
            .decideAndMap(
                user -> {
                    List<ValidationResult> validationResults = createValidationPipeline.validate(user);
                    return validationResults != null ? Decision.of(VALIDATION_ERROR, validationResults) : Decision.of(Decision.CONTINUE, user);
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
                    JsonObject user = (JsonObject) rsp;

                    user.put(User.CREATED_BY, currentUser.getValue(User.ID))
                        .put(User.CREATE_DATE, Converters.toMySqlDateString(new Date()));

                    WebUtils
                        .create(Tables.users.name(), user, jdbcClient)
                        .map(updateResult -> updateResult.getKeys().getLong(0))
                        .then(message::reply)
                        .then(v -> vertx.eventBus().publish(UmEvents.USER_CREATED,
                            user.put(User.CREATED_BY, currentUser)))
                        .error(e -> ExceptionUtil.fail(message, e));
                })
            .error(e ->
                ExceptionUtil.fail(message, e));
    }

    public void update(Message<JsonObject> message) {

        final JsonObject currentUser = new JsonObject(message.headers().get(AUTH_TOKEN));

        Promises.callable(() -> transformationPipeline.transform(message.body()))
            .decideAndMap(
                user -> {
                    List<ValidationResult> validationResults = validationPipeline.validate(user);
                    return validationResults != null ? Decision.of(VALIDATION_ERROR, validationResults) : Decision.of(Decision.CONTINUE, user);
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
                    JsonObject user = (JsonObject) rsp;
                    final Long id = user.getLong(User.ID, 0L);

                    user.put(User.UPDATED_BY, currentUser.getValue(User.ID))
                        .put(User.UPDATE_DATE, Converters.toMySqlDateString(new Date()));

                    WebUtils.update(Tables.users.name(), user, id, jdbcClient)
                        .map(updateResult -> updateResult.getUpdated() > 0 ? id : 0)
                        .then(message::reply)
                        .then(eee -> vertx.eventBus().publish(UmEvents.USER_UPDATED, user.put(User.UPDATED_BY, currentUser)))
                        .error(e -> ExceptionUtil.fail(message, e));
                })
            .error(e -> ExceptionUtil.fail(message, e));
    }

    public void delete(Message<Object> message) {

        final JsonObject user = new JsonObject(message.headers().get(AUTH_TOKEN));

        Promises.callable(() -> Converters.toLong(message.body()))
            .mapToPromise(id -> WebUtils.delete(Tables.users.name(), id, jdbcClient)
                .map(updateResult -> updateResult.getUpdated() > 0 ? id : 0)
                .then(message::reply))
            .then(vv -> vertx.eventBus().publish(UmEvents.USER_DELETED,
                new JsonObject().put(Inventory.DELETED_BY, user)
                    .put(Inventory.DELETE_DATE, Converters.toMySqlDateString(new Date()))))
            .error(e ->
                ExceptionUtil.fail(message, e))
        ;
    }

    public void changePassword(Message<JsonObject> message) {
        Promises.callable(() -> converterTransformation.transform(message.body()))
            .decideAndMap(
                user -> {
                    List<ValidationResult> validationResults = changePasswordValidationPipeline.validate(user);
                    return validationResults != null ? Decision.of(VALIDATION_ERROR, validationResults) : Decision.of(Decision.CONTINUE, user);
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

                    Promises.callable(() -> (JsonObject) rsp)
                        .mapToPromise(
                            user -> WebUtils.query("select password from users where id = " + user.getValue(User.ID), new JsonArray(), jdbcClient)
                                .map(
                                    resultSet -> (Tpl2<List<JsonArray>, JsonObject>) Tpls.of(
                                        resultSet.getResults(), user)))
                        .decideAndMap(val -> val.apply(
                            (list, user) ->
                                Decision.of(
                                    list.size() <= 0
                                        ? PASSWORD_MISMATCH
                                        : !list.get(0).getString(0).equals(user.getString(User.CURRENT_PASSWORD))
                                        ? PASSWORD_MISMATCH
                                        : Decision.CONTINUE, user)))
                        .on(PASSWORD_MISMATCH,
                            rssp -> {
                                ImmutableList<ValidationResult> validationResults = ImmutableList.of(
                                    new ValidationResultBuilder()
                                        .setField(User.PASSWORD)
                                        .setErrorCode(ErrorCodes.PASSWORD_MISMATCH.code())
                                        .createValidationResult()
                                );
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
                        .contnue(user -> {

                            user = changePasswordIncludeExcludeTransformation.transform(user);
                            final Long id = user.getLong(User.ID, 0L);
                            WebUtils.update(Tables.users.name(), user, id, jdbcClient)
                                .map(updateResult -> updateResult.getUpdated() > 0 ? id : 0)
                                .then(message::reply)
                                .error(e -> ExceptionUtil.fail(message, e));
                        })
                        .error(e -> ExceptionUtil.fail(message, e))
                    ;
                })
            .error(e -> ExceptionUtil.fail(message, e));
    }

    public static void main(String... args) {
        SecureRandom secureRandom = new SecureRandom();
        String substring = (secureRandom.nextLong() + "").substring(9);
        System.out.println(substring);
        System.out.println(substring.length());
    }
}
