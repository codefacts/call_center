package com.imslbd.um;

/**
 * Created by shahadat on 3/31/16.
 */
class UmErrorCodeHelper {
    static private int validation = UmErrorCodeHelper.validation();
    static private final int validationHttp = UmErrorCodeHelper.validationHttp();

    static private int error = UmErrorCodeHelper.error();
    static private final int errorHttp = UmErrorCodeHelper.errorHttp();

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
