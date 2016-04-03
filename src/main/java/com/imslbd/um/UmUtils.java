package com.imslbd.um;

import io.crm.util.DataTypes;
import io.crm.web.util.WebUtils;

import java.util.Map;

/**
 * Created by shahadat on 3/10/16.
 */
public class UmUtils {

    public static String limitOffset(int page, int size) {
        return "limit " + size + " offset " + WebUtils.offset(page, size);
    }

    public static DataTypes resolve(Object value) {
        return null;
    }

    public static void main() {

    }
}
