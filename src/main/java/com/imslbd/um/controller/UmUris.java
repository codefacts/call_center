package com.imslbd.um.controller;

import static com.imslbd.um.controller.H.api;

/**
 * Created by shahadat on 3/6/16.
 */
public enum UmUris {
    USERS_HOME("/users-home", "USERS"),
    USERS(api("/users"), "USERS"),
    LOGIN("/um-login", "Login"),
    LOGOUT("/um-logout", "Login");
    public final String value;
    public final String label;

    UmUris(final String value, String label) {
        this.value = value;
        this.label = label;
    }
}

class H {
    public static String api(String uri) {
        return "/api" + uri;
    }
}