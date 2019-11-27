package com.hold.electrify.util;

import com.blankj.utilcode.util.SPUtils;

public class UserCache {
    private static final SPUtils SP_UTILS = SPUtils.getInstance();

    public enum Field {
        mobile, token
    }

    public static void save(Field field, Object value) {
        SP_UTILS.put(field.name(), value + "");
    }

    public static String get(Field field) {
        return SP_UTILS.getString(field.name());
    }

    public static void clear() {
        Field[] values = Field.values();
        for (Field field : values) {
            SP_UTILS.remove(field.name());
        }
    }
}
