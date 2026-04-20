package com.example.aiservice.service;

import com.example.aiservice.model.Activity;
import com.example.aiservice.model.Recommendation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {

    @Autowired
     GeminiService  geminiService;

    public Recommendation genrateRecommendation(Activity activity){
      String prompt=createPromptForActivity(activity);
      String aiResponse=geminiService.getAnswer(prompt);
      log.info("RESPONSE FROM AI :{}",aiResponse);

       return processAiResponse(activity,aiResponse);
    }

    private Recommendation processAiResponse(Activity activity,String aiResponse){
        try{
            ObjectMapper mapper=new ObjectMapper();
            //JsonNode rootnode= mapper.readTree(aiResponse);

            JsonNode rootNode = mapper.readTree(aiResponse);

            JsonNode textNode = rootNode
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String jsonContent = textNode.asText()
                    .replaceAll("```json\\n","")
                    .replaceAll("\\n```","")
                    .trim();
            //log.info("PARSED RESPONSE FROM AI :",jsonContent);

             JsonNode analysisJson=mapper.readTree(jsonContent);
             JsonNode analysisNode=analysisJson.path("analysis");
             StringBuilder fullAnalysis=new StringBuilder();
             addAnalysisSection(fullAnalysis,analysisNode,"overall","Overall :");
            addAnalysisSection(fullAnalysis,analysisNode,"pace","Pace :");
            addAnalysisSection(fullAnalysis,analysisNode,"HeartBeat","HeartBeat :");
            addAnalysisSection(fullAnalysis,analysisNode,"caloriesBurned","Calories :");

            List<String> improvements=extractImprovements(analysisJson.path("improvements"));

            List<String> suggestions=extractSuggestions(analysisJson.path("suggestions"));

            List<String> safety=extractSafetyGuidelines(analysisJson.path("Safety Measure"));

             return   Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType())
                    .recommendation(fullAnalysis.toString())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(safety)
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return  createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .activityType(activity.getType())
                .recommendation("Unable to genrate detailed analysis")
                .improvements(Collections.singletonList("Continue with your current routine"))
                .suggestions(Collections.singletonList("Consider consulting a fitness professional"))
                .safety(Arrays.asList(
                       "Always warm up before exercise",
                        "Stay hydrated",
                        "Listen to your body"
                ))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<String> extractSafetyGuidelines(JsonNode safetyNode) {
        List<String> safety=new ArrayList<>();
        if(safetyNode.isArray()){
            safetyNode.forEach(item->safety.add(item.asText()));
        }
        return safety.isEmpty() ?
                Collections.singletonList("Follow general safety guidelines"):
                safety;
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
      List<String> suggestions =new ArrayList<>();
      if(suggestionsNode.isArray()){
          suggestionsNode.forEach(suggestion->{
              String workout=suggestion.path("workout").asText();
              String description=suggestion.path("description").asText();
              suggestions.add(String.format("%s: %s",workout,description));
          });

      }
        return  suggestions.isEmpty() ?
                Collections.singletonList("No specific suggestions provided"):
                suggestions;
    }

    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();
        if (improvementsNode.isArray()) {
            improvementsNode.forEach(improvement -> {
                String area = improvement.path("area").asText();
                String exercise = improvement.path("Exercise required").asText();
                String detail = improvement.path("recommendations").asText();
                improvements.add(String.format("%s: %s %s", area, exercise, detail));

            });
        }
        return improvements.isEmpty() ?
                Collections.singletonList("No Specific improvements provided"):
                improvements;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
     if(!analysisNode.path(key).isMissingNode()){
         fullAnalysis.append(prefix)
                 .append(analysisNode.path(key).asText())
                 .append("\n\n");
     }
    }


    private String createPromptForActivity(Activity activity){
        return String.format("""
                Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON Format:
                {
                "analysis":{
                  "overall" : "Overall analysis here",
                  "pace" : "Pace analysis here",
                  "HeartBeat" : "herat rate analysis here",
                  "caloriesBurned" : "Calorie Burned"
                },
                "improvements":[
                {
                  "area":"Area name",
                  "Exercise required" : "Exercise required",
                  "recommendations":"Detailed recommendations"
                }
                ],
                "suggestions":[
                {
                "workout":"Workout name with Sets to Perform",
                "description":"Detailed workout guide"
                }
                ],
                "Safety Measure" : [
                  "Safety Point 1",
                  "Safety Point 2"
                ],
               
                }
                
                Analyze this activity:
                Activity Type : %s
                Duration : %d minutes
                Calories Burned : %d
                Additional Metrics : %s
                
                Provide Detailed analysis on performance , improvements , next workout and safety guidelines
                Ensure the response follows the Exact JSON Format shown above
                """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()

        );
    }
}
