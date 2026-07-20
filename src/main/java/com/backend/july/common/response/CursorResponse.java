package com.backend.july.common.response;

import java.util.List;

public record CursorResponse<T, C>(
        List<T> content,
        C nextCursor,
        int size,
        boolean hasNext
) {

    public CursorResponse {
        content = List.copyOf(content);
    }

    public static <T, C> CursorResponse<T, C> of(
            List<T> content,
            C nextCursor,
            boolean hasNext
    ) {
        return new CursorResponse<>(
                content,
                nextCursor,
                content.size(),
                hasNext
        );
    }
}
