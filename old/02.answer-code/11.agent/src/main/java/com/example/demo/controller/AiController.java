package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.agent.Exam01WeatherAgent;
import com.example.demo.agent.Exam02WeatherAgent;
import com.example.demo.agent.Exam05AccommodationAgent;
import com.example.demo.agent.Exam03AttractionAgent;
import com.example.demo.agent.Exam04RestaurantAgent;
import com.example.demo.agent.Exam06YoutubeSearchAgent;
import com.example.demo.dto.Accommodation;
import com.example.demo.dto.Attraction;
import com.example.demo.dto.Restaurant;
import com.example.demo.dto.Youtube;

@RestController
@RequestMapping("/ai")
public class AiController {
  // ##### 필드 #####
  @Autowired
  private Exam01WeatherAgent exam01WeatherAgent;
  
  @Autowired
  private Exam02WeatherAgent exam02WeatherAgent;
  
  @Autowired
  private Exam03AttractionAgent exam03AttractionAgent;
  
  @Autowired
  private Exam04RestaurantAgent exam04RestaurantAgent;
  
  @Autowired
  private Exam05AccommodationAgent exam05AccommodationAgent;
  
  @Autowired
  private Exam06YoutubeSearchAgent exam06YoutubeSearchAgent;

  
  @PostMapping(
    value = "/exam01-weather-agent",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String exam01WeatherAgent(
      @RequestParam("conversationId") String conversationId,
      @RequestParam("question") String question) {
    return exam01WeatherAgent.execute(conversationId, question);
  }
  
  @PostMapping(
    value = "/exam02-weather-agent",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String exam02WeatherAgent(
      @RequestParam("conversationId") String conversationId,
      @RequestParam("question") String question) {
    return exam02WeatherAgent.execute(conversationId, question);
  }

  @PostMapping(
    value = "/exam03-attraction-agent",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public List<Attraction> exam03AttractionAgent(@RequestParam("question") String question) {
    return exam03AttractionAgent.execute(question);
  }
  
  @PostMapping(
    value = "/exam04-restaurant-agent",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public List<Restaurant> exam04RestaurantAgent(@RequestParam("question") String question) {
    return exam04RestaurantAgent.execute(question);
  }  
  
  @PostMapping(
    value = "/exam05-accommodation-agent",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public List<Accommodation> exam05AccommodationAgent(@RequestParam("question") String question) {
    return exam05AccommodationAgent.execute(question);
  }
  
  @PostMapping(
    value = "/exam06-youtube-search",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public List<Youtube> exam06YoutubeSearch(@RequestParam("question") String question) {
    return exam06YoutubeSearchAgent.execute(question);
  }
}
