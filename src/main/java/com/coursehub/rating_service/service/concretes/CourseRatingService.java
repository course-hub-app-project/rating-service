package com.coursehub.rating_service.service.concretes;

import com.coursehub.rating_service.client.CourseServiceClient;
import com.coursehub.rating_service.dto.request.RateRequest;
import com.coursehub.rating_service.dto.response.RatingMQResponseForCourseService;
import com.coursehub.rating_service.exception.AccessDeniedException;
import com.coursehub.rating_service.exception.NotFoundException;
import com.coursehub.rating_service.model.CourseRating;
import com.coursehub.rating_service.repository.CourseRatingRepository;
import com.coursehub.rating_service.security.UserPrincipal;
import com.coursehub.rating_service.service.abstracts.IRatingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;

import static com.coursehub.rating_service.config.RabbitMQConfig.*;
import static java.lang.Boolean.TRUE;
import static lombok.AccessLevel.PRIVATE;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Qualifier("courseRatingService")
public class CourseRatingService implements IRatingService {

    CourseRatingRepository courseRatingRepository;
    AmqpTemplate rabbitTemplate;
    CourseServiceClient courseServiceClient;


//    @Override
//    @Transactional
//    public void rate(String targetId, RateRequest request, UserPrincipal principal) {
//        Boolean isCourseExist;
//        try {
//            isCourseExist = courseServiceClient.isPublishedCourseExist(targetId).getBody();
//        } catch (Throwable throwable) {
//            isCourseExist = courseServiceClient.isPublishedCourseExistFallBack(targetId, throwable).getBody();
//        }
//
//        if (!TRUE.equals(isCourseExist)) {
//            throw new NotFoundException("Course not found");
//        }
//
//        CourseRating courseRating = CourseRating.builder()
//                .courseId(targetId)
//                .rating(request.rating())
//                .userId(principal.getId())
//                .build();
//
//        courseRatingRepository.save(courseRating);
//
//        var responseForCourse = RatingMQResponseForCourseService.builder()
//                .courseId(targetId)
//                .rating(request.rating())
//                .build();
//
//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
//            @Override
//            public void afterCommit() {
//                rabbitTemplate.convertAndSend(EXCHANGE_NAME, ADD_COURSE_RATING_ROUTING_KEY, responseForCourse);
//            }
//        });
//
//    }


    @Override
    public void rate(String targetId, RateRequest request, UserPrincipal principal) {
        Boolean isCourseExist;
        try {
            isCourseExist = courseServiceClient.isPublishedCourseExist(targetId).getBody();
        } catch (Throwable throwable) {
            isCourseExist = courseServiceClient.isPublishedCourseExistFallBack(targetId, throwable).getBody();
        }

        if (!TRUE.equals(isCourseExist)) {
            throw new NotFoundException("Course not found");
        }

        CourseRating courseRating = CourseRating.builder()
                .courseId(targetId)
                .rating(request.rating())
                .userId(principal.getId())
                .build();

        courseRatingRepository.save(courseRating);

        var responseForCourse = RatingMQResponseForCourseService.builder()
                .courseId(targetId)
                .rating(request.rating())
                .build();

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ADD_COURSE_RATING_ROUTING_KEY, responseForCourse);
    }


    @Override
    @Transactional
    public void deleteRating(String rateId, UserPrincipal principal) {
        CourseRating rating = courseRatingRepository.findById(rateId).orElseThrow(() ->
                new NotFoundException("Rating not found"));


        if (!Objects.equals(principal.getId(), rating.getUserId())) {
            throw new AccessDeniedException("Access denied");
        }

        courseRatingRepository.delete(rating);

        var responseForCourse = RatingMQResponseForCourseService.builder()
                .courseId(rating.getCourseId())
                .rating(rating.getRating())
                .build();


        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rabbitTemplate.convertAndSend(EXCHANGE_NAME, DELETE_COURSE_RATING_ROUTING_KEY, responseForCourse);
            }
        });
    }

}
