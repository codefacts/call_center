package com.imslbd.call_center.util;

import io.crm.util.exceptions.InvalidArgumentException;

/**
 * Created by someone on 12/11/2015.
 */
public class MyValidators {
    public static final String dateValidator(String s) {
        final String val = s.trim();
        if (val.matches(MyUtil.GLOBAL_DATE_FORMAT_PATTERN) || val.matches(MyUtil.EXCEL_DATE_FORMAT_PATTERN)) {
            return val;
        } else {
            throw new InvalidArgumentException("Date value is invalid. value: " + s);
        }
    }
}
