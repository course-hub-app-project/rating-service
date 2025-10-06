package com.coursehub.rating_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ADD_COURSE_RATING_QUEUE = "rate-course-queue";
    public static final String DELETE_COURSE_RATING_QUEUE = "delete-rate-course-queue";

    public static final String ADD_INSTRUCTOR_RATING_QUEUE = "rate-instructor-queue";
    public static final String DELETE_INSTRUCTOR_RATING_QUEUE = "delete-rate-instructor-queue";

    public static final String EXCHANGE_NAME = "rating-exchange";

    public static final String ADD_COURSE_RATING_ROUTING_KEY = "rate.course";
    public static final String DELETE_COURSE_RATING_ROUTING_KEY = "delete.rate.course";

    public static final String ADD_INSTRUCTOR_RATING_ROUTING_KEY = "rate.instructor";
    public static final String DELETE_INSTRUCTOR_RATING_ROUTING_KEY = "delete.rate.instructor";


    @Bean
    public Queue addRateCourseQueue() {
        return new Queue(ADD_COURSE_RATING_QUEUE, true);
    }

    @Bean
    public Queue deleteRateCourseQueue() {
        return new Queue(DELETE_COURSE_RATING_QUEUE, true);
    }

    @Bean
    public Queue addRateInstructorQueue() {
        return new Queue(ADD_INSTRUCTOR_RATING_QUEUE, true);
    }

    @Bean
    public Queue deleteRateInstructorQueue() {
        return new Queue(DELETE_INSTRUCTOR_RATING_QUEUE, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding addRateCourseBinding() {
        return BindingBuilder.bind(addRateCourseQueue())
                .to(exchange())
                .with(ADD_COURSE_RATING_ROUTING_KEY);
    }

    @Bean
    public Binding deleteRateCourseBinding() {
        return BindingBuilder.bind(deleteRateCourseQueue())
                .to(exchange())
                .with(DELETE_COURSE_RATING_ROUTING_KEY);
    }

    @Bean
    public Binding addRateInstructorBinding() {
        return BindingBuilder.bind(addRateInstructorQueue())
                .to(exchange())
                .with(ADD_INSTRUCTOR_RATING_ROUTING_KEY);
    }

    @Bean
    public Binding deleteRateInstructorBinding() {
        return BindingBuilder.bind(deleteRateInstructorQueue())
                .to(exchange())
                .with(DELETE_INSTRUCTOR_RATING_ROUTING_KEY);
    }


    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}
