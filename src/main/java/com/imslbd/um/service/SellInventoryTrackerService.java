package com.imslbd.um.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.imslbd.um.*;
import com.imslbd.um.model.Sell;
import com.imslbd.um.model.SellUnit;
import com.imslbd.um.model.User;
import io.crm.pipelines.transformation.JsonTransformationPipeline;
import io.crm.pipelines.transformation.impl.json.object.ConverterTransformation;
import io.crm.pipelines.transformation.impl.json.object.DefaultValueTransformation;
import io.crm.pipelines.transformation.impl.json.object.IncludeExcludeTransformation;
import io.crm.pipelines.transformation.impl.json.object.RemoveNullsTransformation;
import io.crm.pipelines.validator.ValidationResult;
import io.crm.pipelines.validator.ValidationResultBuilder;
import io.crm.promise.Promises;
import io.crm.promise.intfs.Defer;
import io.crm.promise.intfs.MapToPromiseHandler;
import io.crm.promise.intfs.Promise;
import io.crm.util.ExceptionUtil;
import io.crm.util.Util;
import io.crm.web.util.Converters;
import io.crm.web.util.WebUtils;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by shahadat on 4/13/16.
 */
public class SellInventoryTrackerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SellInventoryTrackerService.class);
    private static final String SELL_INVENTORY_TRACK_ENTRY_VALIDATION_ERRORS = "SELL_INVENTORY_TRACK_ENTRY_VALIDATION_ERRORS";
    private static final String SELL_UNIT_INDEX = "sell_unit_index";
    private final List<String> fields;

    private final JsonTransformationPipeline transformationPipeline;

    private final IncludeExcludeTransformation includeExcludeTransformation;
    private final ConverterTransformation converterTransformation;

    public SellInventoryTrackerService(List<String> fields) {
        this.fields = fields;

        converterTransformation = new ConverterTransformation(Services.converters(fields.toArray(new String[fields.size()]), Tables.sellInventoryTracking.name()));
        includeExcludeTransformation = new IncludeExcludeTransformation(ImmutableSet.copyOf(fields), null);


        {
            transformationPipeline = new JsonTransformationPipeline(ImmutableList.of(
                new RemoveNullsTransformation(),
                new DefaultValueTransformation(new JsonObject()),
                converterTransformation,
                Services.USER_TRACKING_EXCLUDE_TRANSFORMATION
            ));
        }
    }


    public void track(Message<JsonObject> message) {

        final JsonObject sell = message.body();

        List<JsonObject> sellUnits = sell.getJsonArray(Sell.SELL_UNITS, Util.EMPTY_JSON_ARRAY).getList();

        final List<String> list = sellUnits.stream()
            .map(js ->
                "select t.inventoryProductId, t.quantity, t.inventoryUnitId, ip.unitId, t.inventoryId" +
                    " from sellInventoryTracking t" +
                    " join inventoryProducts ip on ip.inventoryId = t.inventoryId" +
                    " where" +
                    " t.inventoryProductId = ip.productId " +
                    " and " +
                    " t.productId = " + js.getLong(InventoryProduct.PRODUCT_ID) + "" +
                    " and" +
                    " t.unitId = " + js.getLong(InventoryProduct.UNIT_ID)
            ).collect(Collectors.toList());

        WebUtils.multiQuery(list, UmApp.getJdbcClient())
            .then(resultSets -> {

                for (int I = 0; I < resultSets.size(); I++) {

                    final ResultSet resultSet = resultSets.get(I);

                    if (resultSet.getNumRows() <= 0) {

                        final JsonObject validationResult = new ValidationResultBuilder()
                            .setErrorCode(UmErrorCodes.SELL_INVENTORY_TRACK_ENTRY_NOT_FOUND.code())
                            .setValue(sellUnits.get(I))
                            .setAdditionals(
                                new JsonObject()
                                    .put(SELL_UNIT_INDEX, I))
                            .createValidationResult().toJson();

                        dumpValidationError(validationResult);

                    } else {

                        final int idx = I;

                        resultSet.getResults().forEach(jsonArray -> {

                            if (!jsonArray.getLong(2).equals(jsonArray.getLong(3))) {

                                final JsonObject validationResult = new ValidationResultBuilder()
                                    .setErrorCode(UmErrorCodes.SELL_INVENTORY_TRACK_ENTRY_MISMATCH.code())
                                    .setValue(sellUnits.get(idx))
                                    .setAdditionals(
                                        new JsonObject()
                                            .put(SELL_UNIT_INDEX, idx))
                                    .createValidationResult().toJson();

                                dumpValidationError(validationResult);

                            } else {

                                final double total = sellUnits.get(idx).getDouble("quantity") * jsonArray.getDouble(1);

                                WebUtils.update(
                                    "UPDATE `inventoryProducts` SET quantity = quantity - " + total +
                                        " where " +
                                        " inventoryId = " + jsonArray.getLong(4) +
                                        " and " +
                                        " productId = " + jsonArray.getLong(0) +
                                        " and " +
                                        " unitId = " + jsonArray.getLong(2)
                                    ,
                                    UmApp.getJdbcClient())
                                    .error(e -> LOGGER.error("ERROR_TRACKING_INVENTORY_SELL", e))
                                ;
                            }

                        });
                    }

                }

            })
            .error(e -> LOGGER.error("ERRPR_RETRIEVING_TRACKING_ENTRIES", e))
        ;
    }

    private void dumpValidationError(JsonObject validationResult) {
        UmApp.getMongoClient().insert(SELL_INVENTORY_TRACK_ENTRY_VALIDATION_ERRORS, validationResult, stringAsyncResult -> {
            if (stringAsyncResult.failed()) {
                LOGGER.error("ERROR INSERTING SELL_INVENTORY_TRACK_ENTRY_VALIDATION_ERRORS", stringAsyncResult.cause());
            }
        });
    }

    public void createTrack(Message<JsonArray> message) {


        final List<JsonObject> list = message.body().getList();

        final ImmutableList.Builder<Promise<Long>> builder = ImmutableList.builder();

        list.forEach(jsonObject -> {

            final Promise<Long> tracking = Promises.callable(() -> transformationPipeline.transform(jsonObject))
                .mapToPromise(js -> {

                    final JsonObject jsa = includeExcludeTransformation.transform(js);

                    jsa.remove(User.ID);

                    return WebUtils.create("sellInventoryTracking", jsa, UmApp.getJdbcClient());

                })
                .map(ur -> ur.getKeys().getLong(0));

            builder.add(tracking);
        });

        Promises.when(builder.build())
            .map(JsonArray::new)
            .then(message::reply)
            .error(e -> ExceptionUtil.fail(message, e))
        ;

    }

    public void updateTrack(Message<JsonObject> message) {

        WebUtils.getConnection(UmApp.getJdbcClient())
            .mapToPromise(con -> {
                try {
                    final Defer<Void> defer = Promises.defer();
                    con.setAutoCommit(false, Util.makeDeferred(defer));
                    return defer.promise().error(e -> con.close()).map(v -> (SQLConnection) con);
                } catch (Exception ex) {
                    con.close();
                    return Promises.fromError(ex);
                }
            })
            .mapToPromise(con -> {
                try {

                    final long productId = Converters.toLong(message.body().getValue("productId"));

                    return WebUtils.delete("sellInventoryTracking",
                        new JsonObject()
                            .put("productId", productId), con).map(v -> con).error(e -> con.close());

                } catch (Exception ex) {
                    con.close();
                    return Promises.<SQLConnection>fromError(ex);
                }
            })
            .mapToPromise(con -> {

                try {

                    final List<JsonObject> list = message.body().getJsonArray("tracks").getList();

                    final ImmutableList.Builder<Promise<Long>> builder = ImmutableList.builder();

                    list.forEach(jsonObject -> {

                        final Promise<Long> tracking = Promises.callable(() -> transformationPipeline.transform(jsonObject))
                            .mapToPromise(js -> {

                                final JsonObject jsa = includeExcludeTransformation.transform(js);

                                final Long id = jsa.getLong(User.ID, 0L);

                                return WebUtils
                                    .delete("sellInventoryTracking",
                                        new JsonObject()
                                            .put(User.ID, id), con)
                                    .mapToPromise(
                                        v -> WebUtils.create("sellInventoryTracking", jsa, con))
                                    .map(vx -> vx.getKeys().getLong(0))
                                    ;
                            });

                        builder.add(tracking);
                    });

                    return Promises.when(builder.build())
                        .map(JsonArray::new)
                        .mapToPromise(ja -> {
                            final Defer<Void> defer = Promises.defer();
                            con.commit(Util.makeDeferred(defer));
                            return defer.promise().map(jk -> ja);
                        })
                        .error(e -> con.close())
                        ;

                } catch (Exception ex) {
                    con.close();
                    return Promises.fromError(ex);
                }
            })
            .then(message::reply)
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void findTrack(Message<JsonObject> message) {

    }

    public void findAllTracks(Message<JsonObject> message) {
        Promises
            .callable(() -> {
                final JsonObject body = message.body();
                return body == null ? 0 : Converters.toLong(body.getValue("productId"));
            })
            .mapToPromise(
                productId -> WebUtils.query(
                    "select * from sellInventoryTracking" +
                        (productId == null || productId <= 0
                            ? ""
                            : (" where productId = " + productId)), UmApp.getJdbcClient()))
            .map(ResultSet::getRows)
            .map(JsonArray::new)
            .then(jsonArray -> message.reply(
                new JsonObject()
                    .put(Services.DATA, jsonArray)
            ))
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }
}
