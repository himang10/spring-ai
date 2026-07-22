package com.example.demo.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

//import com.example.demo.agent.Exam01WeatherAgent;
//import com.example.demo.agent.Exam02WeatherAgent;
import com.example.demo.agent.Exam05AccommodationAgent;
import com.example.demo.agent.TravelOrchestrator;
import com.example.demo.agent.Exam03AttractionAgent;
import com.example.demo.agent.Exam04RestaurantAgent;
//import com.example.demo.agent.Exam06YoutubeSearchAgent;
import com.example.demo.dto.Accommodation;
import com.example.demo.dto.Attraction;
import com.example.demo.dto.Restaurant;
import com.example.demo.dto.Youtube;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/ai")
public class AiController {

  // 여행 관련 멀티 에이전트 처리를 위한 오케스트레이터
  @Autowired
  private TravelOrchestrator travelOrchestrator;

  @GetMapping("/chat")
  public SseEmitter chat(@RequestParam("message") String userQuery, HttpSession session) {
    String sessionId = session.getId();

    // 실시간 이벤트 스트리밍을 위한 통로(SseEmitter) 생성
    // 데이터를 보내지 않을 경우 5분 후 자동 종료 설정
    SseEmitter emitter = new SseEmitter(300000L);

    // 비동기 스레드에서 오케스트레이터 실행
    CompletableFuture.runAsync(() -> {
      try {
        // 오케스트레이터에 emitter를 인자로 전달
        String response = travelOrchestrator.execute(userQuery, sessionId, emitter);
        // 최종 응답 전송 및 종료
        sendSseEvent(emitter, "message", response);
        sendSseEvent(emitter, "complete", "");
        emitter.complete();
      } catch (Exception e) {
        emitter.completeWithError(e);
      }

    });

    // 생성된 통로(SseEmitter)를 즉시 반환하여 연결 유지
    return emitter;
  }

  // SSE 이벤트 전송
  private void sendSseEvent(SseEmitter emitter, String event, String data) {
    try {
      emitter.send(SseEmitter.event().name(event).data(data));
    } catch (Exception e) {
      // SSE 전송 실패 시 무시 
    }
  }
  // ##### 필드 #####
  /** 
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
    */
}
