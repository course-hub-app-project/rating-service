package com.coursehub.rating_service.client;

import lombok.Builder;

@Builder
public record ExceptionMessage(
        String timestamp,
        int status,
        String reasonPhrase,
        String message,
        String path
) {
}
