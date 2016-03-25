package com.imslbd.um;

import io.crm.web.util.WebUtils;

import java.util.Map;
import java.util.Objects;

/**
 * Created by shahadat on 3/10/16.
 */
public class UmUtils {
    private static Map<Class, DataTypes> dataTypesMap;

    static {

    }

    public static String limitOffset(int page, int size) {
        return "limit " + size + " offset " + WebUtils.offset(page, size);
    }

    public static DataTypes resolve(Object value) {
        return null;
    }

    public static void main() {

    }
}
