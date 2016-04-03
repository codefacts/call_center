package com.imslbd.um.ex;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

/**
 * Created by shahadat on 3/27/16.
 */
public class UmException extends RuntimeException {
    private final int errorCode;
    private final MultiMap headers;
    private final JsonObject body;

    public UmException(int errorCode, MultiMap headers, JsonObject body) {
        this.errorCode = errorCode;
        this.headers = headers;
        this.body = body;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public MultiMap getHeaders() {
        return headers;
    }

    public JsonObject getBody() {
        return body;
    }
}
