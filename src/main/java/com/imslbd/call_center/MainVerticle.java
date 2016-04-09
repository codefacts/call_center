package com.imslbd.call_center;

import com.imslbd.call_center.controller.*;
import com.imslbd.call_center.service.*;
import com.imslbd.um.Tables;
import com.imslbd.um.UmEvents;
import com.imslbd.um.controller.AuthController;
import com.imslbd.um.controller.UmUris;
import com.imslbd.um.controller.UserController;
import com.imslbd.um.model.Product;
import com.imslbd.um.model.Sell;
import com.imslbd.um.service.*;
import io.crm.QC;
import io.crm.pipelines.transformation.impl.json.object.RemoveNullsTransformation;
import io.crm.promise.Promises;
import io.crm.util.Util;
import io.crm.web.ApiEvents;
import io.crm.web.App;
import io.crm.web.Uris;
import io.crm.web.codec.ListToListCodec;
import io.crm.web.codec.RspListToRspListCodec;
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
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.crm.web.statichandler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpHeaders.TEXT_HTML;

final public class MainVerticle extends AbstractVerticle {
    public static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
    private final Set<String> publicUris = publicUris();
    public static final String PROP_PORT = "PORT";
    public static final String PROP_CALL_REVIEW_PORT = "CALL_REVIEW_PORT";
    public static final String PROP_CALL_REVIEW_HOST = "CALL_REVIEW_HOST";
    private JDBCClient jdbcClient;
    private JDBCClient jdbcClientUm;

    @Override
    public void start() throws Exception {
        initialize();
        registerCodecs();
        registerEvents();

        //Configure Router
        final Router router = Router.router(vertx);

        router.route("/eventbus/*").handler(SockJSHandler.create(vertx)
            .bridge(bridgeOptions()));

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

    private BridgeOptions bridgeOptions() {
        BridgeOptions bridgeOptions = new BridgeOptions();

        bridgeOptions
            .addInboundPermitted(new PermittedOptions().setAddress(MyEvents.LOCK_CONTACT_ID))
            .addOutboundPermitted(new PermittedOptions().setAddress(MyEvents.LOCK_CONTACT_ID))
            .addOutboundPermitted(new PermittedOptions().setAddress(MyEvents.ALREADY_LOCKED))
            .addOutboundPermitted(new PermittedOptions().setAddress(MyEvents.CONTACT_UPDATED));

        bridgeOptions
            .addInboundPermitted(new PermittedOptions().setAddress(MyEvents.UN_LOCK_CONTACT_ID))
            .addOutboundPermitted(new PermittedOptions().setAddress(MyEvents.UN_LOCK_CONTACT_ID))
            .addInboundPermitted(new PermittedOptions().setAddress(MyEvents.ALREADY_LOCKED))
            .addInboundPermitted(new PermittedOptions().setAddress(MyEvents.CONTACT_UPDATED))
            .addInboundPermitted(new PermittedOptions().setAddress(MyEvents.FIND_ALL_CALL_OPERATOR));

        //UM

        umBridgeOptions(bridgeOptions);

        return bridgeOptions;
    }

    private void umBridgeOptions(BridgeOptions bridgeOptions) {
        bridgeOptions
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.FIND_ALL_USERS))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.FIND_USER))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.CREATE_USER))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.UPDATE_USER))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.DELETE_USER))

            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.FIND_ALL_UNITS))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.FIND_UNIT))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.CREATE_UNIT))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.UPDATE_UNIT))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.DELETE_UNIT))

            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.FIND_ALL_PRODUCTS))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.FIND_ALL_PRODUCTS_DECOMPOSED))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.FIND_PRODUCT))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.FIND_PRODUCT_DECOMPOSED))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.PRODUCTS_UNIT_WISE_PRICE))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.CREATE_PRODUCT))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.UPDATE_PRODUCT))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.DELETE_PRODUCT))

            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.FIND_ALL_INVENTORIES))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.FIND_ALL_INVENTORY_PRODUCTS))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.FIND_INVENTORY))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.CREATE_INVENTORY))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.UPDATE_INVENTORY))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.DELETE_INVENTORY))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.INSERT_INVENTORY_PRODUCT))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.DELETE_INVENTORY_PRODUCT))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.ADD_PRODUCT_TO_INVENTORY))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.REMOVE_PRODUCT_FROM_INVENTORY))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.EDIT_INVENTORY_PRODUCT_QUANTITY))

            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.FIND_ALL_SELLS))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.FIND_SELL))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.FIND_SELL_DECOMPOSED))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.CREATE_SELL))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.UPDATE_SELL))
            .addInboundPermitted(new PermittedOptions().setAddress(UmEvents.DELETE_SELL))
        ;

        bridgeOptions
            .addOutboundPermitted(new PermittedOptions().setAddress(UmEvents.USER_CREATED))
            .addOutboundPermitted(new PermittedOptions().setAddress(UmEvents.USER_UPDATED))
            .addOutboundPermitted(new PermittedOptions().setAddress(UmEvents.USER_DELETED))

            .addOutboundPermitted(new PermittedOptions().setAddress(UmEvents.UNIT_CREATED))
            .addOutboundPermitted(new PermittedOptions().setAddress(UmEvents.UNIT_UPDATED))
            .addOutboundPermitted(new PermittedOptions().setAddress(UmEvents.UNIT_DELETED))
        ;
    }

    private void initialize() {
        jdbcClient = JDBCClient.createShared(vertx, MyApp.loadConfig().getJsonObject("database"));
    }

    private void registerCodecs() {
        vertx.eventBus().registerDefaultCodec(RspList.class, new RspListToRspListCodec());
        vertx.eventBus().registerDefaultCodec(List.class, new ListToListCodec());
    }

    @Override
    public void stop() throws Exception {
        if (jdbcClient != null) {
            jdbcClient.close();
        }
        if (jdbcClientUm != null) {
            jdbcClientUm.close();
        }
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
        eventBus.consumer(MyEvents.BR_INFO, brService::findBrInfo);

        final ConsumerContactService consumerContactService = new ConsumerContactService(httpClient, vertx);
        eventBus.consumer(MyEvents.CONSUMER_CONTACT_CALL_STEP_1, consumerContactService::consumerContactsCallStep_1);
        eventBus.consumer(MyEvents.CONSUMER_CONTACT_CALL_STEP_2, consumerContactService::consumerContactsCallStep_2);
        eventBus.consumer(MyEvents.BR_ACTIVITY_SUMMARY, consumerContactService::brActivitySummary);
        eventBus.consumer(MyEvents.CONTACT_DETAILS, consumerContactService::contactDetails);
        eventBus.consumer(MyEvents.FIND_CALL_OPERATOR, consumerContactService::findCallOperator);
        eventBus.consumer(MyEvents.FIND_BRAND, consumerContactService::findBrand);
        eventBus.consumer(MyEvents.CALL_CREATE, consumerContactService::createCall);
        eventBus.consumer(MyEvents.FIND_ALL_CALL_OPERATOR, consumerContactService::findAllCallOperator);
        eventBus.consumer(MyEvents.LOCK_CONTACT_ID, consumerContactService::lockContactId);
        eventBus.consumer(MyEvents.UN_LOCK_CONTACT_ID, consumerContactService::unLockContactId);

        CampaignService campaignService = new CampaignService(jdbcClient);
        eventBus.consumer(MyEvents.FIND_ALL_CAMPAIGN, campaignService::findAllCampaign);
        eventBus.consumer(MyEvents.FIND_CAMPAIGN, campaignService::findCampaign);

        DbService dbService = new DbService(jdbcClient);
        eventBus.consumer(MyEvents.FIND_ALL_DATA_SOURCES, dbService::findAllDataSources);

        //UM
        jdbcClientUm = JDBCClient.createNonShared(vertx, MyApp.loadConfig().getJsonObject("um_database"));


        Promises
            .when(
                WebUtils.query("select * from " + Tables.products + " where id < 0", jdbcClientUm)
                    .map(rs -> {
                        String[] colNames = new String[rs.getNumColumns()];
                        return rs.getColumnNames().toArray(colNames);
                    }),
                WebUtils.query("select * from " + Tables.productUnitPrices + " where id < 0", jdbcClientUm)
                    .map(rs -> {
                        String[] colNames = new String[rs.getNumColumns()];
                        return rs.getColumnNames().toArray(colNames);
                    }),
                WebUtils.query("select * from " + Tables.units + " where id < 0", jdbcClientUm)
                    .map(rs -> {
                        String[] colNames = new String[rs.getNumColumns()];
                        return rs.getColumnNames().toArray(colNames);
                    }),
                WebUtils.query("select max(" + Product.ID + ") as maxId from " + Tables.products
                    + " where 1", jdbcClientUm)
                    .map(rs -> rs.getRows().get(0).getLong("maxId"))
            )
            .then(tpl2 -> tpl2.accept((names, priceNames, unitFields, maxId) -> {
                ProductService productService = new ProductService(jdbcClientUm, names, priceNames, unitFields, Util.or(maxId, 1L), vertx);
                eventBus.consumer(UmEvents.FIND_ALL_PRODUCTS, productService::findAll);
                eventBus.consumer(UmEvents.FIND_ALL_PRODUCTS_DECOMPOSED, productService::findAllDecomposed);
                eventBus.consumer(UmEvents.FIND_PRODUCT, productService::find);
                eventBus.consumer(UmEvents.FIND_PRODUCT_DECOMPOSED, productService::findDecomposed);
                eventBus.consumer(UmEvents.PRODUCTS_UNIT_WISE_PRICE, productService::unitWisePrice);
                eventBus.consumer(UmEvents.CREATE_PRODUCT, productService::create);
                eventBus.consumer(UmEvents.UPDATE_PRODUCT, productService::update);
                eventBus.consumer(UmEvents.DELETE_PRODUCT, productService::delete);
            }))
            .error(e -> LOGGER.error("Error creating UnitService", e))
        ;

        Promises
            .when(
                WebUtils.query("select * from " + Tables.sells + " where id < 0", jdbcClientUm)
                    .map(rs -> {
                        String[] colNames = new String[rs.getNumColumns()];
                        return rs.getColumnNames().toArray(colNames);
                    }),
                WebUtils.query("select * from " + Tables.sellUnits + " where id < 0", jdbcClientUm)
                    .map(rs -> {
                        String[] colNames = new String[rs.getNumColumns()];
                        return rs.getColumnNames().toArray(colNames);
                    }),
                WebUtils.query("select * from " + Tables.products + " where id < 0", jdbcClientUm)
                    .map(rs -> {
                        String[] colNames = new String[rs.getNumColumns()];
                        return rs.getColumnNames().toArray(colNames);
                    }),
                WebUtils.query("select * from " + Tables.units + " where id < 0", jdbcClientUm)
                    .map(rs -> {
                        String[] colNames = new String[rs.getNumColumns()];
                        return rs.getColumnNames().toArray(colNames);
                    }),
                WebUtils.query("select max(" + Sell.ID + ") as maxId, max(transactionId) as maxTransactionId, max(orderId) as maxOrderId from " + Tables.sells
                    + " where 1", jdbcClientUm)
                    .map(rs -> rs.getRows().get(0))
                    .map(new RemoveNullsTransformation()::transform)
            )
            .then(tpl2 -> tpl2.accept(
                (fields, sellUnitFields, productFields, unitFields, maxId) -> {
                    SellService sellService = new SellService(jdbcClientUm, fields, sellUnitFields, productFields, unitFields,
                        maxId.getLong("maxId", 0L),
                        maxId.getLong("maxTransactionId", 0L),
                        maxId.getLong("maxOrderId", 0L), vertx);
                    eventBus.consumer(UmEvents.FIND_ALL_SELLS, sellService::findAll);
                    eventBus.consumer(UmEvents.FIND_SELL, sellService::find);
                    eventBus.consumer(UmEvents.FIND_SELL_DECOMPOSED, sellService::findDecomposed);
                    eventBus.consumer(UmEvents.CREATE_SELL, sellService::create);
                    eventBus.consumer(UmEvents.UPDATE_SELL, sellService::update);
                    eventBus.consumer(UmEvents.DELETE_SELL, sellService::delete);
                }))
            .error(e -> LOGGER.error("Error creating UnitService", e))
        ;

        Promises
            .when(
                WebUtils.query("select * from " + Tables.inventories + " where id < 0", jdbcClientUm)
                    .map(rs -> {
                        String[] colNames = new String[rs.getNumColumns()];
                        return rs.getColumnNames().toArray(colNames);
                    }),
                WebUtils.query("select * from " + Tables.inventoryProducts + " where id < 0", jdbcClientUm)
                    .map(rs -> {
                        String[] colNames = new String[rs.getNumColumns()];
                        return rs.getColumnNames().toArray(colNames);
                    })
            )
            .then(tpl2 -> tpl2.accept(
                (fields, inventoryProductFields) -> {
                    InventoryService inventoryService = new InventoryService(jdbcClientUm, fields,
                        inventoryProductFields, vertx);
                    eventBus.consumer(UmEvents.FIND_ALL_INVENTORIES, inventoryService::findAll);
                    eventBus.consumer(UmEvents.FIND_ALL_INVENTORY_PRODUCTS, inventoryService::findAllProducts);
                    eventBus.consumer(UmEvents.FIND_INVENTORY, inventoryService::find);
                    eventBus.consumer(UmEvents.CREATE_INVENTORY, inventoryService::create);
                    eventBus.consumer(UmEvents.UPDATE_INVENTORY, inventoryService::update);
                    eventBus.consumer(UmEvents.DELETE_INVENTORY, inventoryService::delete);
                    eventBus.consumer(UmEvents.INSERT_INVENTORY_PRODUCT, inventoryService::insertProduct);
                    eventBus.consumer(UmEvents.DELETE_INVENTORY_PRODUCT, inventoryService::deleteProduct);
                    eventBus.consumer(UmEvents.ADD_PRODUCT_TO_INVENTORY, inventoryService::addProduct);
                    eventBus.consumer(UmEvents.REMOVE_PRODUCT_FROM_INVENTORY, inventoryService::removeProduct);
                    eventBus.consumer(UmEvents.EDIT_INVENTORY_PRODUCT_QUANTITY, inventoryService::editProductQuantity);
                }))
            .error(e -> LOGGER.error("Error creating UnitService", e))
        ;

        WebUtils.query("select * from " + Tables.users + " where id < 0", jdbcClientUm)
            .map(rs -> {
                String[] colNames = new String[rs.getNumColumns()];
                return rs.getColumnNames().toArray(colNames);
            })
            .then(columnNames -> {

                UserService userService = new UserService(jdbcClientUm, columnNames, vertx);
                eventBus.consumer(UmEvents.FIND_ALL_USERS, userService::findAll);
                eventBus.consumer(UmEvents.FIND_USER, userService::find);
                eventBus.consumer(UmEvents.CREATE_USER, userService::create);
                eventBus.consumer(UmEvents.UPDATE_USER, userService::update);
                eventBus.consumer(UmEvents.DELETE_USER, userService::delete);

            })
            .error(e -> {
                LOGGER.error("Error creating UnitService", e);
            })
        ;

        WebUtils.query("select * from units where id < 0", jdbcClientUm)
            .map(rs -> {
                String[] colNames = new String[rs.getNumColumns()];
                return rs.getColumnNames().toArray(colNames);
            })
            .then(columnNames -> {
                UnitService unitService = new UnitService(jdbcClientUm, columnNames, vertx);
                eventBus.consumer(UmEvents.FIND_ALL_UNITS, unitService::findAllUnits);
                eventBus.consumer(UmEvents.FIND_UNIT, unitService::findUnit);
                eventBus.consumer(UmEvents.CREATE_UNIT, unitService::createUnit);
                eventBus.consumer(UmEvents.UPDATE_UNIT, unitService::updateUnit);
                eventBus.consumer(UmEvents.DELETE_UNIT, unitService::deleteUnit);

                System.out.println("Unit Service Registered");
            })
            .error(e -> {
                LOGGER.error("Error creating UnitService", e);
            })
        ;
    }

    private void devLogin(EventBus eventBus) {
        eventBus.consumer(ApiEvents.LOGIN_API, (Message<JsonObject> m) -> {

            m.reply(new JsonObject()
                .put(QC.username, "Sohan")
                .put(QC.userId, "br-124")
                .put(QC.mobile, "01553661069")
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
                        .put(QC.mobile, "01553661069")
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

        new CampaignController(vertx, router);

        new DBController(vertx, router);

        //UM Controllers

        UserController userController = new UserController(vertx);

        router.get(UmUris.USERS_HOME.value).handler(userController::index);

        //Auth Um

        AuthController authController = new AuthController(vertx, jdbcClientUm);
        router.post(UmUris.LOGIN.value).handler(BodyHandler.create());
        router.post(UmUris.LOGIN.value).handler(authController::login);
        router.post(UmUris.LOGOUT.value).handler(authController::logout);
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
