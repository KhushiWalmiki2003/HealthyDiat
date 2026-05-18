package com.example.aiservice.service;

import com.example.aiservice.model.Activity;
import com.example.aiservice.model.Recommendation;
import com.example.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    private final ActivityAIService aiService;
    private final  RecommendationRepository recommendationRepository;

//    @RabbitListener(queues = "activity.queue")
//    public void processActivity(Activity activity) {
//        log.info("Received activity for processing : {} ", activity.getId());
//        log.info("Genraated Recommendation : {} ", aiService.genrateRecommendation(activity));
//    }

    @RabbitListener(queues = "activity.queue")
    public void processActivity(Activity activity) {
        log.info("Received activity for processing : {} ", activity.getId());
        try {
            Recommendation recommendation = aiService.genrateRecommendation(activity);
            recommendationRepository.save(recommendation);
            log.info("Saved recommendation for activity : {}", activity.getId());
        } catch (Exception e) {
            // Log and swallow — prevents infinite requeue loop
            log.error("Failed to process activity {}: {}", activity.getId(), e.getMessage());
        }
    }

}
