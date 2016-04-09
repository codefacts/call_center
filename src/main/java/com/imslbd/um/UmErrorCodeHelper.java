package com.imslbd.um;

import io.crm.ErrorCodeHelper;

/**
 * Created by shahadat on 3/31/16.
 */
class UmErrorCodeHelper {
    static private int validation = ErrorCodeHelper.validation();
    static private final int validationHttp = ErrorCodeHelper.validationHttp();

    static private int error = ErrorCodeHelper.error();
    static private final int errorHttp = ErrorCodeHelper.errorHttp();


    static int validation() {
        return validation++;
    }

    static int validationHttp() {
        return validationHttp;
    }

    public static int error() {
        return error++;
    }

    public static int errorHttp() {
        return errorHttp;
    }
}
