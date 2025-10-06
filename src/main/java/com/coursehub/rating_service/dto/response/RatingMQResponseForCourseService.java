package com.coursehub.rating_service.dto.response;

import lombok.Builder;

@Builder
public record RatingMQResponseForCourseService(
        String courseId,
        Double rating
) {
}
