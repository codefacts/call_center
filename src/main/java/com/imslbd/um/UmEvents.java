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
    public static final java.lang.String FIND_ALL_PRODUCTS = "FIND_ALL_PRODUCTS";
    public static final java.lang.String FIND_PRODUCT = "FIND_PRODUCT";
    public static final java.lang.String FIND_PRODUCT_DECOMPOSED = "FIND_PRODUCT_DECOMPOSED";
    public static final java.lang.String CREATE_PRODUCT = "CREATE_PRODUCT";
    public static final java.lang.String UPDATE_PRODUCT = "UPDATE_PRODUCT";
    public static final java.lang.String DELETE_PRODUCT = "DELETE_PRODUCT";

    public static final String um(String des) {
        return "UM." + des;
    }
}