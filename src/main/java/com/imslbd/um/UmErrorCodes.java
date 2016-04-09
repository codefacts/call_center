package com.imslbd.um;

import static com.imslbd.um.UmErrorCodeHelper.*;

/**
 * Created by shahadat on 3/27/16.
 */
public enum UmErrorCodes {
    UNIT_NOT_FOUND(error(), "unit.not.found.error", errorHttp()),
    USER_NOT_FOUND(error(), "user.not.found.error", errorHttp()),
    PRODUCT_NOT_FOUND(error(), "product.not.found.error", errorHttp()),
    INVENTORY_NOT_FOUND(error(), "inventory.not.found.error", errorHttp()),
    TWO_PASSWORD_MISMATCH(validation(), "two.password.mismatch.validation.error", validationHttp());
    private final int code;
    private final String messageCode;
    private final int httpResponseCode;

    UmErrorCodes(int code, String messageCode, int httpResponseCode) {
        this.code = code;
        this.messageCode = messageCode;
        this.httpResponseCode = httpResponseCode;
    }

    public int code() {
        return code;
    }

    public String messageCode() {
        return messageCode;
    }

    public int httpResponseCode() {
        return httpResponseCode;
    }
}

