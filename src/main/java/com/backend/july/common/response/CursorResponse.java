package com.backend.july.common.response;

import java.util.List;

public record CursorResponse<T, C>(
        List<T> content,
        C nextCursor,
        int size,
        boolean hasNext
) {

    public CursorResponse {
        content = content == null
                ? List.of()
                : List.copyOf(content);
    }

    public static <T, C> CursorResponse<T, C> of(
            List<T> content,
            C nextCursor,
            boolean hasNext
    ) {
        return new CursorResponse<>(
                content,
                nextCursor,
                content == null ? 0 : content.size(),
                hasNext
        );
    }

    public static <T, C> CursorResponse<T, C> empty() {
        return new CursorResponse<>(
                List.of(),
                null,
                0,
                false
        );
    }
}
