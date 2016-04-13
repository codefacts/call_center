package com.imslbd.um.service;

import com.imslbd.call_center.MyApp;
import com.imslbd.um.UmApp;
import com.imslbd.um.template.SalesReportTemplate;
import io.crm.promise.Promises;
import io.crm.promise.intfs.Defer;
import io.crm.promise.intfs.Promise;
import io.crm.util.Util;
import io.crm.web.util.Converters;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.MailResult;
import io.vertx.ext.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by shahadat on 4/13/16.
 */
final public class MailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);
    private final Vertx vertx;
    private final String from;

    private int afterNoonHour = 17;
    private int afterNoonMin = 00;
    private int nightHour = 23;
    private int nightMin = 59;
    private String to;

    {
        final JsonObject mail = MyApp.loadConfig().getJsonObject("mail");
        from = mail.getString("from");
        to = mail.getString("to");
        afterNoonHour = mail.getInteger("afterNoonHour");
        afterNoonMin = mail.getInteger("afterNoonMin");
        nightHour = mail.getInteger("nightHour");
        nightMin = mail.getInteger("nightMin");
    }

    private ThreadLocal<DateFormat> dateFormatThreadLocal = new ThreadLocal<DateFormat>() {
        @Override
        public DateFormat get() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    public MailService(Vertx vertx) {
        this.vertx = vertx;
        loop(today());
    }

    public static Promise<MailResult> send(String from, String to, String subject, String html) {
        final Defer<MailResult> defer = Promises.defer();
        UmApp.getMailClient().sendMail(new MailMessage(from, to, subject, "").setHtml(html), Util.makeDeferred(defer));
        return defer.promise();
    }

    private void loop(LocalDate date) {
        Promises
            .when(
                scheduleReportSend(date, afterNoonHour, afterNoonMin)
                    .then(v -> LOGGER.info("SEND REPORT Sceduled for afternoon."))
                    .then(v -> System.out.println("afternoon shceduled."))
                    .error(v -> System.out.println("afternoon error.")),
                scheduleReportSend(date, nightHour, nightMin)
                    .then(v -> LOGGER.info("SEND REPORT Sceduled for night."))
                    .then(v -> System.out.println("night shceduled."))
                    .error(v -> System.out.println("night error.")))
            .error(e -> {
                final LocalDateTime dest = LocalDate.now().plusDays(1).atTime(0, 0, 0);
                vertx.setTimer(LocalDateTime.now().until(dest, ChronoUnit.MILLIS), id -> loop(today()));
            })
            .then(v -> loop(nextDay(date)))
        ;
    }

    private LocalDate today() {
        return LocalDate.now();
    }

    private LocalDate nextDay(LocalDate date) {
        return date.plusDays(1);
    }

    private Promise<Void> scheduleReportSend(LocalDate date, int hour, int min) {
        final Defer<Void> defer = Promises.defer();

        try {
            final LocalDateTime dest = date.atTime(hour, min);
            final long timer = LocalDateTime.now().until(dest, ChronoUnit.MILLIS);

            if (timer >= 0) {
                vertx.setTimer(timer,
                    (id) -> sendReport().then(v -> defer.complete()).error(defer::fail));
            } else {
                defer.fail(new Exception("Schedule time already passed"));
            }
        } catch (Exception ex) {
            defer.fail(ex);
        }
        return defer.promise();
    }

    private Promise<Void> sendReport() {
        return reportHtml()
            .then(val -> send(from, to, subject(), val))
            .map(v -> null)
            ;
    }

    private String subject() {
        return "LASPARAGUS: Daily Sales Report";
    }

    private Promise<String> reportHtml() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(1, Calendar.DAY_OF_MONTH);

        return WebUtils.query("select p.name, sum(s.quantity), u.name, s.unitPrice, sum(s.total)" +
            " from sellUnits s" +
            " join sells sl on sl.id = s.sellId" +
            " join products p on s.productId = p.id" +
            " join units u on s.unitId = u.id" +
            " where DATE(sl.sellDate) = '" + dateFormatThreadLocal.get().format(new Date()) + "'" +
            " group by s.productId, s.unitId", UmApp.getJdbcClient())
            .map(ResultSet::getResults)
            .map(jsonArrays -> new SalesReportTemplate(jsonArrays).render())
            ;
    }
}
