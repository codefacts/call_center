package com.imslbd.um.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.imslbd.call_center.MyApp;
import com.imslbd.um.UmErrorCodes;
import com.imslbd.um.model.User;
import io.crm.ErrorCodes;
import io.crm.pipelines.transformation.impl.json.object.IncludeExcludeTransformation;
import io.crm.util.DataTypes;
import io.crm.util.ExceptionUtil;
import io.crm.web.util.Converters;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by shahadat on 3/27/16.
 */
public class Services {
    public static final Logger LOGGER = LoggerFactory.getLogger(Services.class);
    public static final String AUTH_TOKEN = "authToken";
    public static final java.lang.String SIZE = "size";
    public static final String PAGE = "page";
    public static final String DATA = "data";
    public static final String PAGINATION = "pagination";
    public static final java.lang.String ID = "id";
    public static final java.lang.String DATABASE = "um_database";

    public static final Map<Integer, String> JDBC_TYPES;
    public static final Map<Integer, DataTypes> DATA_TYPES_MAP;
    public static final Map<Integer, Function<Object, Object>> TYPE_CONVERTERS;
    public static final String VALIDATION_RESULTS = "validation_results";
    public static final String MESSAGE_CODE = "messageCode";
    public static final String MESSAGE = "message";
    public static final String RESPONSE_CODE = "responseCode";
    public static final Map<Integer, JsonObject> ERROR_CODES_MAP;
    public static final IncludeExcludeTransformation USER_TRACKING_EXCLUDE_TRANSFORMATION = new IncludeExcludeTransformation(null, ImmutableSet.of(
        User.CREATE_DATE, User.CREATED_BY, User.UPDATE_DATE, User.UPDATED_BY
    ));

    static {
        JDBC_TYPES = ImmutableMap.copyOf(Arrays.asList(Types.class.getDeclaredFields())
            .stream()
            .filter(fs -> Modifier.isFinal(fs.getModifiers()) && Modifier.isPublic(fs.getModifiers())
                && Modifier.isStatic(fs.getModifiers()))
            .collect(Collectors.toMap(
                fs ->
                    ExceptionUtil.toRuntimeCall(() -> fs.getInt(null)),
                ExceptionUtil.toRuntimeCall(() -> Field::getName))));
    }

    static {
        ImmutableMap.Builder<Integer, DataTypes> builder = ImmutableMap.builder();
        builder
            .put(4, DataTypes.LONG)
            .put(12, DataTypes.STRING)
            .put(-1, DataTypes.STRING)
            .put(-7, DataTypes.BOOLEAN)
            .put(91, DataTypes.DATE)
            .put(93, DataTypes.DATE)
            .put(8, DataTypes.DATE.DOUBLE)
        ;
        DATA_TYPES_MAP = builder.build();
    }

    static {
        ImmutableMap.Builder<Integer, Function<Object, Object>> builder = ImmutableMap.builder();
        builder
            .put(4, Converters::toLong)
            .put(12, s -> s)
            .put(-1, s -> s)
            .put(-7, Converters::toBoolean)
            .put(91, Converters::toMySqlDateString)
            .put(93, Converters::toMySqlDateString)
            .put(8, Converters::toDouble)
        ;

        TYPE_CONVERTERS = builder.build();
    }

    private static final String ERROR_CODE = "errorCode";

    private static final String HTTP_RESPONSE_CODE = "httpResponseCode";

    static {
        ImmutableMap.Builder<Integer, JsonObject> builder = ImmutableMap.builder();

        Arrays.asList(ErrorCodes.values())
            .forEach(ec -> builder.put(ec.code(),
                new JsonObject()
                    .put(ERROR_CODE, ec.code())
                    .put(MESSAGE_CODE, ec.messageCode())
                    .put(HTTP_RESPONSE_CODE, ec.httpResponseCode())));

        Arrays.asList(UmErrorCodes.values())
            .forEach(ec -> builder.put(ec.code(),
                new JsonObject()
                    .put(ERROR_CODE, ec.code())
                    .put(MESSAGE_CODE, ec.messageCode())
                    .put(HTTP_RESPONSE_CODE, ec.httpResponseCode())));

        ERROR_CODES_MAP = builder.build();
    }

    public static ImmutableMap<String, Function<Object, Object>> converters(String[] fields, String tableName) {
        JsonObject db = MyApp.loadConfig().getJsonObject(Services.DATABASE);
        String url = db.getString("url");
        String user = db.getString("user");
        String password = db.getString("password");

        ImmutableMap.Builder<String, Function<Object, Object>> builder = ImmutableMap.builder();
        try {
            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                Statement statement = connection.createStatement();
                statement.execute("select * from " + tableName);
                ResultSet rs = statement.getResultSet();
                ResultSetMetaData metaData = rs.getMetaData();

                int columnCount = metaData.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    int columnType = metaData.getColumnType(i + 1);
                    Function<Object, Object> converter = Services.TYPE_CONVERTERS.get(columnType);
                    Objects.requireNonNull(converter, "Type Converter can't be null for Type: " +
                        "[" + fields[i] + " " + columnType + ": " + Services.JDBC_TYPES.get(columnType) + "]");
                    builder.put(fields[i], converter);
                    System.out.println(columnType + ": " + Services.JDBC_TYPES.get(columnType));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error connecting to database through jdbc", e);
        }

        return builder.build();
    }
}
