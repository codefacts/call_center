package com.imslbd.um.service;

import com.imslbd.um.UmApp;
import com.imslbd.um.template.SalesReportTemplate;
import io.crm.promise.Promises;
import io.crm.promise.intfs.Defer;
import io.crm.promise.intfs.Promise;
import io.crm.util.Util;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.MailResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

/**
 * Created by shahadat on 4/13/16.
 */
final public class MailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);
    private final Vertx vertx;

    private final int afterNoonHour = 17;
    private final int afterNoonMin = 00;
    private final int nightHour = 0;
    private final int nightMin = 0;
    private final String from = "um3065651@gmail.com";
    private final String to = "sohan09014@gmail.com";

    public MailService(Vertx vertx) {
        this.vertx = vertx;
        loop(today());
//        send(from, to, subject(), reportHtml())
//            .then(v -> System.out.println("SUccess: " + v.toJson().encode()))
//            .error(e -> e.printStackTrace());
    }

    public static Promise<MailResult> send(String from, String to, String subject, String text) {
        final Defer<MailResult> defer = Promises.defer();
        UmApp.getMailClient().sendMail(new MailMessage(from, to, subject, text).setHtml(text), Util.makeDeferred(defer));
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
        } finally {
            if (!defer.promise().isComplete()) {
                defer.complete();
            }
        }
        return defer.promise();
    }

    private Promise<Void> sendReport() {
        String report = reportHtml();
        return null;
    }

    private String subject() {
        return "LASPARAGUS: Daily Sales Report";
    }

    private String reportHtml() {

        return new SalesReportTemplate().render();
    }
}
