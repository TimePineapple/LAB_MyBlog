package com.myblog.support;

public final class QuerySupport {
    private QuerySupport() {
    }

    public static void validatePage(long page, long size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page 必须大于 0，size 必须在 1 到 100 之间");
        }
        if (page - 1 > Long.MAX_VALUE / size) {
            throw new IllegalArgumentException("分页偏移量过大");
        }
    }

    // LIKE 仍使用绑定参数，同时将用户输入中的通配符当作普通字符。
    public static String literalKeyword(String keyword) {
        return keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
