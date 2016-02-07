package com.imslbd.call_center.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imslbd.call_center.MyApp;
import com.imslbd.call_center.template.Sidebar;
import com.imslbd.call_center.template.page.DashboardTmptBuilder;
import com.imslbd.call_center.template.page.PageTmptBuilder;
import io.crm.promise.Promises;
import io.crm.promise.intfs.Defer;
import io.crm.promise.intfs.Promise;
import io.crm.util.ExceptionUtil;
import io.crm.util.Util;
import io.crm.util.touple.immutable.Tpl2;
import io.crm.util.touple.immutable.Tpls;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import org.watertemplate.Template;

import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by someone on 12/11/2015.
 */
public class MyUtil {
    public static final String GLOBAL_DATE_FORMAT = "dd-MMM-yyyy hh:mm:ss a";
    public static final ThreadLocal<DateFormat> DATE_FORMAT_THREAD_LOCAL = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat(GLOBAL_DATE_FORMAT);
        }
    };

    public static final String EXCEL_DATE_FORMAT = "dd/MM/yyyy kk:mm";
    public static final ThreadLocal<DateFormat> EXCEL_DATE_FORMAT_THREAD_LOCAL = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat(EXCEL_DATE_FORMAT);
        }
    };

    public static final ObjectMapper mapper = Util.accept(new ObjectMapper(), m -> {
        m.setDateFormat(new SimpleDateFormat(GLOBAL_DATE_FORMAT));
    });

    public static final String GLOBAL_DATE_FORMAT_PATTERN = "\\d{1,2}-[a-zA-Z]{1,3}-\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2} [AP]M";
    public static final String EXCEL_DATE_FORMAT_PATTERN = "\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}:\\d{1,2}";

    public static PageTmptBuilder dashboardPage(
        final String title,
        final JsonObject currentUser,
        final String uri,
        final Template body) {
        return new PageTmptBuilder(title)
            .body(
                new DashboardTmptBuilder()
                    .setUser(currentUser)
                    .setSidebarTemplate(new Sidebar(uri))
                    .setContentTemplate(body)
                    .build()
            );
    }

    public static String templatePath(final String path) {
        return "file:" + Paths.get(MyApp.myTemplateDir, path).toString();
    }

    public static Promise<SQLConnection> getConnection(final JDBCClient jdbcClient) {
        final Defer<SQLConnection> defer = Promises.defer();
        jdbcClient.getConnection(Util.makeDeferred(defer));
        return defer.promise();
    }

    public static final Date parseDate(String val) {
        final String string = Util.or(val, "").trim();
        if (string.matches(GLOBAL_DATE_FORMAT_PATTERN)) {
            return ExceptionUtil.toRuntimeCall(() -> DATE_FORMAT_THREAD_LOCAL.get().parse(string));
        } else {
            return ExceptionUtil.toRuntimeCall(() -> EXCEL_DATE_FORMAT_THREAD_LOCAL.get().parse(string));
        }
    }

    public static String formatDate(final Date date, final String defaultValue) {
        try {
            return DATE_FORMAT_THREAD_LOCAL.get().format(date);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static JsonObject convertToJsonObject(Object obj) {
        return ExceptionUtil.toRuntimeCall(() -> new JsonObject(mapper.writeValueAsString(obj)));
    }

    public static JsonArray convertToJsonArray(List areas) {
        return ExceptionUtil.toRuntimeCall(() -> new JsonArray(mapper.writeValueAsString(areas)));
    }

    public static Tpl2<String, String> splitPair(String valuePair, String regex) {
        final String aDefault = Util.or(valuePair, "");
        final String[] split = aDefault.split(regex, 2);
        return Tpls.of(split[0], split.length > 1 ? split[1] : "");
    }

    public static String mobiBaseUrl(String host, int port) {
        return "http://" + host + ":" + port;
    }
}
