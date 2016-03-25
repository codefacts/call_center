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

    public static final String USER_CREATED = um("USER_CREATED");
    public static final String USER_UPDATED = um("USER_UPDATED");
    public static final String USER_DELETED = um("USER_DELETED");


    public static final String um(String des) {
        return "UM." + des;
    }
}