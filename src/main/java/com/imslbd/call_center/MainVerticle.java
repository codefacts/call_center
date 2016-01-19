package com.imslbd.call_center;

import com.imslbd.call_center.controller.*;
import com.imslbd.call_center.service.*;
import io.crm.QC;
import io.crm.model.User;
import io.crm.promise.Promises;
import io.crm.web.ApiEvents;
import io.crm.web.App;
import io.crm.web.Uris;
import io.crm.web.codec.ListToListCodec;
import io.crm.web.codec.RspListToRspListCodec;
import io.crm.web.controller.AuthController;
import io.crm.web.controller.GoogleMapController;
import io.crm.web.template.PageBuilder;
import io.crm.web.template.page.LoginTemplate;
import io.crm.web.util.RspList;
import io.crm.web.util.WebUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.crm.web.statichandler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpHeaders.TEXT_HTML;

final public class MainVerticle extends AbstractVerticle {
    private final Set<String> publicUris = publicUris();
    public static final String PROP_PORT = "PORT";
    public static final String PROP_CALL_REVIEW_PORT = "CALL_REVIEW_PORT";
    public static final String PROP_CALL_REVIEW_HOST = "CALL_REVIEW_HOST";

    @Override
    public void start() throws Exception {
        initialize();
        registerCodecs();
        registerEvents();

        //Configure Router
        final Router router = Router.router(vertx);
        router.route().handler(CookieHandler.create());
        SessionStore store = LocalSessionStore.create(vertx);
        SessionHandler sessionHandler = SessionHandler.create(store);
        router.route().handler(sessionHandler);

        //Register Listeners
        registerFilters(router);
        registerControllers(router);
        getVertx().createHttpServer().requestHandler(router::accept).listen(MyApp.loadConfig().getInteger(PROP_PORT));
        System.out.println("<----------------------------------WEB_SERVER_STARTED------------------------------------->");
        System.out.println("PORT: " + MyApp.loadConfig().getInteger(PROP_PORT));
    }

    private void initialize() {

    }

    private void registerCodecs() {
        vertx.eventBus().registerDefaultCodec(RspList.class, new RspListToRspListCodec());
        vertx.eventBus().registerDefaultCodec(List.class, new ListToListCodec());
    }

    @Override
    public void stop() throws Exception {
    }

    private void registerEvents() {

        final EventBus eventBus = vertx.eventBus();

        devLogin(eventBus);

        final HttpClient httpClient = vertx.createHttpClient(new HttpClientOptions()
            .setDefaultHost(MyApp.loadConfig().getString(PROP_CALL_REVIEW_HOST))
            .setDefaultPort(MyApp.loadConfig().getInteger(PROP_CALL_REVIEW_PORT)));

        devLogin(eventBus);

        final AreaService areaService = new AreaService(vertx, httpClient);
        eventBus.consumer(MyEvents.FIND_ALL_AREAS, areaService::findAll);
        final DistributionHouseService distributionHouseService = new DistributionHouseService(vertx, httpClient);
        eventBus.consumer(MyEvents.FIND_ALL_DISTRIBUTION_HOUSES, distributionHouseService::findAll);
        final BrService brService = new BrService(vertx, httpClient);
        eventBus.consumer(MyEvents.FIND_ALL_BRS, brService::findAll);

        final ConsumerContactService consumerContactService = new ConsumerContactService(httpClient);
        eventBus.consumer(MyEvents.CONSUMER_CONTACT_CALL_STEP_1, consumerContactService::consumerContactsCallStep_1);
        eventBus.consumer(MyEvents.CONSUMER_CONTACT_CALL_STEP_2, consumerContactService::consumerContactsCallStep_2);
        eventBus.consumer(MyEvents.BR_ACTIVITY_SUMMARY, consumerContactService::brActivitySummary);
        eventBus.consumer(MyEvents.CONTACT_DETAILS, consumerContactService::contactDetails);
        eventBus.consumer(MyEvents.FIND_CALL_OPERATOR, consumerContactService::findCallOperator);
        eventBus.consumer(MyEvents.FIND_BRAND, consumerContactService::findBrand);
        eventBus.consumer(MyEvents.CALL_CREATE, consumerContactService::createCall);
        eventBus.consumer(MyEvents.FIND_ALL_CALL_OPERATOR, consumerContactService::findAllCallOperator);
    }

    private void devLogin(EventBus eventBus) {
        eventBus.consumer(ApiEvents.LOGIN_API, (Message<JsonObject> m) -> {

            m.reply(new JsonObject()
                .put(QC.username, "Sohan")
                .put(QC.userId, "br-124")
                .put(User.mobile, "01553661069")
                .put(QC.userType,
                    new JsonObject()
                        .put(QC.id, 1)
                        .put(QC.name, "Programmer")));
        });
    }

    private void registerFilters(final Router router) {
        corsFilter(router);
        noCacheFilter(router);
        authFilter(router);
    }

    private void corsFilter(final Router router) {
        router.route().handler(context -> {
            context.response().headers()
                .set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, OPTIONS, DELETE")
                .set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600")
                .set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "x-requested-with")
            ;
            context.next();
        });
    }

    private void noCacheFilter(final Router router) {
        router.route().handler(context -> {
            if (!context.request().uri().startsWith("/static")) {
                context.response().headers().set(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
                context.response().headers().set("Pragma", "no-cache");
                context.response().headers().set(HttpHeaders.EXPIRES, "0");
            }
            context.next();
        });
    }

    private void authFilter(final Router router) {

        router.route().handler(context -> {
            if (System.getProperty("dev-mode") != null) {
                context.session().put(gv.currentUser,
                    new JsonObject()
                        .put(QC.username, "Sohan")
                        .put(QC.userId, "1001")
                        .put(User.mobile, "01553661069")
                        .put(QC.userType,
                            new JsonObject()
                                .put(QC.id, 1)
                                .put(QC.name, "Programmer")));
            }
            if (context.session().get(gv.currentUser) != null) {
                context.next();
                return;
            }
            final String uri = context.request().uri();

            if (publicUris.stream().filter(publicUri -> uri.startsWith(publicUri)).findAny().isPresent()) {
                context.next();
                return;
            }
            //Auth Failed
            if ("XMLHttpRequest".equalsIgnoreCase(context.request().headers().get("X-Requested-With"))) {
                context.response().setStatusCode(HttpResponseStatus.FORBIDDEN.code())
                    .end("Please login to authorize your request.");
                return;
            }
            context.response().setStatusCode(HttpResponseStatus.TEMPORARY_REDIRECT.code());
            context.response().headers().set(HttpHeaders.LOCATION, Uris.LOGIN.value);
            context.response().end();
        });
    }

    private void registerControllers(final Router router) {
        registerStaticFileHandlers(router);

        otherwiseController(router);

//        loginFormController(router);
//        new AuthController(vertx, router);
        new GoogleMapController(router);

        new DashboardController(vertx, router);
        new CallController(vertx, router);

        //Controllers
        new AreaController(vertx, router);
        new DistributionHouseController(vertx, router);
        new BrController(vertx, router);

        final ConsumerContactController consumerContactController = new ConsumerContactController(vertx, router);
        consumerContactController.consumerContactsCallStep_1(router);

        new CallOperator(vertx, router);

        new BrandController(vertx, router);
    }

    private void otherwiseController(final Router router) {
        router.get("/").handler(context -> {
            if (WebUtils.isLoggedIn(context.session())) {
                WebUtils.redirect(Uris.DASHBOARD.value, context.response());
            } else {
                WebUtils.redirect(Uris.LOGIN.value, context.response());
            }
        });
    }

    private void loginFormController(final Router router) {
        router.get(Uris.LOGIN.value).handler(context -> {
            if (WebUtils.isLoggedIn(context.session())) {
                WebUtils.redirect(Uris.DASHBOARD.value, context.response());
                return;
            }
            context.response().headers().set(CONTENT_TYPE, TEXT_HTML);

            context.response().end(
                new PageBuilder("Login")
                    .body(new LoginTemplate())
                    .build().render());
        });
    }

    private void registerStaticFileHandlers(final Router router) {
        router.route(Uris.STATIC_RESOURCES_PATTERN.value).handler(
            StaticHandler.create(App.STATIC_DIRECTORY)
                .setCachingEnabled(true)
                .setFilesReadOnly(true)
                .setMaxAgeSeconds(3 * 30 * 24 * 60 * 60)
                .setIncludeHidden(false)
                .setEnableFSTuning(true)
        );
        router.route(Uris.PUBLIC_RESOURCES_PATTERN.value).handler(
            StaticHandler.create(App.PUBLIC_DIRECTORY)
                .setFilesReadOnly(true)
                .setMaxAgeSeconds(0)
                .setIncludeHidden(false)
                .setEnableFSTuning(true)
        );

        router.route(MyUris.STATIC_RESOURCES_PATTERN.value).handler(
            StaticHandler.create(MyApp.MY_STATIC_DIRECTORY)
                .setCachingEnabled(true)
                .setFilesReadOnly(true)
                .setMaxAgeSeconds(3 * 30 * 24 * 60 * 60)
                .setIncludeHidden(false)
                .setEnableFSTuning(true)
        );
        router.route(MyUris.PUBLIC_RESOURCES_PATTERN.value).handler(
            StaticHandler.create(MyApp.MY_PUBLIC_DIRECTORY)
                .setFilesReadOnly(true)
                .setMaxAgeSeconds(0)
                .setIncludeHidden(false)
                .setEnableFSTuning(true)
        );
    }

    private Set<String> publicUris() {
        return Arrays.asList(
            Uris.SESSION_COUNT.value,
            Uris.STATIC_RESOURCES_PATTERN.value,
            Uris.PUBLIC_RESOURCES_PATTERN.value,
            Uris.LOGIN.value,
            Uris.REGISTER.value,
            Uris.EVENT_PUBLISH_FORM.value,
            MyUris.PUBLIC_RESOURCES_PATTERN.value,
            MyUris.STATIC_RESOURCES_PATTERN.value
        )
            .stream()
            .map(uri -> {
                final int index = uri.lastIndexOf('/');
                if (index > 0) {
                    return uri.substring(0, index);
                }
                return uri;
            })
            .collect(Collectors.toSet());
    }
}
