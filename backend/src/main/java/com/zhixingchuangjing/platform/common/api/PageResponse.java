package com.zhixingchuangjing.platform.common.api;

import java.util.List;

public record PageResponse<T>(
        List<T> list,
        int page,
        int pageSize,
        long total
) {
}
