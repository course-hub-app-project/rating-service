package com.coursehub.rating_service.service.concretes;

import com.coursehub.rating_service.client.IdentityServiceClient;
import com.coursehub.rating_service.dto.request.RateRequest;
import com.coursehub.rating_service.dto.response.RatingMQResponseForIdentityService;
import com.coursehub.rating_service.exception.AccessDeniedException;
import com.coursehub.rating_service.exception.NotFoundException;
import com.coursehub.rating_service.model.InstructorRating;
import com.coursehub.rating_service.repository.InstructorRatingRepository;
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
@Qualifier("instructorRatingService")
public class InstructorRatingService implements IRatingService {

    InstructorRatingRepository instructorRatingRepository;
    AmqpTemplate rabbitTemplate;
    IdentityServiceClient identityServiceClient;


    @Override
    @Transactional
    public void rate(String targetId, RateRequest request, UserPrincipal principal) {
        Boolean isInstructorExist;
        try {
            isInstructorExist = identityServiceClient.isInstructorExist(targetId).getBody();
        } catch (Throwable throwable) {
            isInstructorExist = identityServiceClient.isInstructorExistFallBack(targetId, throwable).getBody();
        }

        if (!TRUE.equals(isInstructorExist)) {
            throw new NotFoundException("Instructor not found");
        }

        InstructorRating instructorRating = InstructorRating.builder()
                .instructorId(targetId)
                .rating(request.rating())
                .userId(principal.getId())
                .build();

        instructorRatingRepository.save(instructorRating);

        var responseForIdentity = RatingMQResponseForIdentityService.builder()
                .instructorId(targetId)
                .rating(request.rating())
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rabbitTemplate.convertAndSend(EXCHANGE_NAME, ADD_INSTRUCTOR_RATING_ROUTING_KEY, responseForIdentity);
            }
        });

    }

    @Override
    @Transactional
    public void deleteRating(String rateId, UserPrincipal principal) {

        InstructorRating rating = instructorRatingRepository.findById(rateId).orElseThrow(() -> new NotFoundException("Rate not found"));

        if (!Objects.equals(principal.getId(), rating.getUserId())) {
            throw new AccessDeniedException("Access denied");
        }

        instructorRatingRepository.delete(rating);

        var responseForIdentity = RatingMQResponseForIdentityService.builder()
                .instructorId(rating.getInstructorId())
                .rating(rating.getRating())
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rabbitTemplate.convertAndSend(EXCHANGE_NAME, DELETE_INSTRUCTOR_RATING_ROUTING_KEY, responseForIdentity);
            }
        });

    }
}
