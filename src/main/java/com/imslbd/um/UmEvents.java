package com.imslbd.um;

/**
 * Created by shahadat on 3/9/16.
 */
public class UmEvents {
    public static final String FIND_ALL_USERS = um("FIND_ALL_USERS");
    public static final String FIND_USER = um("FIND_USER");
    public static final String CREATE_USER = um("CREATE_USER");
    public static final String UPDATE_USER = um("UPDATE_USER");
    public static final String DELETE_USER = um("DELETE_USER");

    public static final String FIND_ALL_UNITS = um("FIND_ALL_UNITS");
    public static final String FIND_UNIT = um("FIND_UNIT");
    public static final String CREATE_UNIT = um("CREATE_UNIT");
    public static final String UPDATE_UNIT = um("UPDATE_UNIT");
    public static final String DELETE_UNIT = um("DELETE_UNIT");

    public static final String USER_CREATED = um("USER_CREATED");
    public static final String USER_UPDATED = um("USER_UPDATED");
    public static final String USER_DELETED = um("USER_DELETED");


    public static final String UNIT_CREATED = um("UNIT_CREATED");
    public static final String UNIT_UPDATED = um("UNIT_UPDATED");
    public static final String UNIT_DELETED = um("UNIT_DELETED");
    public static final String FIND_ALL_PRODUCTS = "FIND_ALL_PRODUCTS";
    public static final String FIND_ALL_PRODUCTS_DECOMPOSED = "FIND_ALL_PRODUCTS_DECOMPOSED";
    public static final String FIND_PRODUCT = "FIND_PRODUCT";
    public static final String FIND_PRODUCT_DECOMPOSED = "FIND_PRODUCT_DECOMPOSED";
    public static final String PRODUCTS_UNIT_WISE_PRICE = "PRODUCTS_UNIT_WISE_PRICE";
    public static final String CREATE_PRODUCT = "CREATE_PRODUCT";
    public static final String UPDATE_PRODUCT = "UPDATE_PRODUCT";
    public static final String DELETE_PRODUCT = "DELETE_PRODUCT";

    public static final String FIND_ALL_INVENTORIES = "FIND_ALL_INVENTORIES";
    public static final String FIND_ALL_INVENTORY_PRODUCTS = "FIND_ALL_INVENTORY_PRODUCTS";
    public static final String FIND_INVENTORY = "FIND_INVENTORY";
    public static final String CREATE_INVENTORY = "CREATE_INVENTORY";
    public static final String UPDATE_INVENTORY = "UPDATE_INVENTORY";
    public static final String DELETE_INVENTORY = "DELETE_INVENTORY";

    public static final String INSERT_INVENTORY_PRODUCT = "INSERT_INVENTORY_PRODUCT";
    public static final String DELETE_INVENTORY_PRODUCT = "DELETE_INVENTORY_PRODUCT";

    public static final String PRODUCT_ADDED_TO_INVENTORY = "PRODUCT_ADDED_TO_INVENTORY";

    public static final String ADD_PRODUCT_TO_INVENTORY = "ADD_PRODUCT_TO_INVENTORY";
    public static final String REMOVE_PRODUCT_FROM_INVENTORY = "REMOVE_PRODUCT_FROM_INVENTORY";
    public static final String EDIT_INVENTORY_PRODUCT_QUANTITY = "EDIT_INVENTORY_PRODUCT_QUANTITY";

    public static final String FIND_ALL_SELLS = "FIND_ALL_SELLS";
    public static final String FIND_SELL = "FIND_SELL";
    public static final String CREATE_SELL = "CREATE_SELL";
    public static final String UPDATE_SELL = "UPDATE_SELL";
    public static final String DELETE_SELL = "DELETE_SELL";
    public static final java.lang.String FIND_SELL_DECOMPOSED = "FIND_SELL_DECOMPOSED";
    public static final java.lang.String CHANGE_PASSWORD = "CHANGE_PASSWORD";
    public static final java.lang.String TRANSFER_PRODUCT_TO_INVENTORY = "TRANSFER_PRODUCT_TO_INVENTORY";

    public static final String um(String des) {
        return "UM." + des;
    }
}