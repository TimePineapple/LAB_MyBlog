package com.myblog.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

public record PageResult<T>(List<T> items, long total, long page, long size) {
    public static <T> PageResult<T> of(IPage<T> result) {
        return new PageResult<>(result.getRecords(), result.getTotal(),
                result.getCurrent(), result.getSize());
    }
}
