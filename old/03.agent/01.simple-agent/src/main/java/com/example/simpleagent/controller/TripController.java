package com.example.simpleagent.controller;

import com.example.simpleagent.agent.HotelAgent;
import com.example.simpleagent.agent.RestaurantAgent;
import com.example.simpleagent.agent.TransportAgent;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TripController {

    private static final Logger log = LoggerFactory.getLogger(TripController.class);

    private final TransportAgent transportAgent;
    private final HotelAgent hotelAgent;
    private final RestaurantAgent restaurantAgent;

    public TripController(TransportAgent transportAgent, HotelAgent hotelAgent, RestaurantAgent restaurantAgent) {
        this.transportAgent = transportAgent;
        this.hotelAgent = hotelAgent;
        this.restaurantAgent = restaurantAgent;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/ask")
    public String ask(@RequestParam String agentType,
                      @RequestParam String destination,
                      @RequestParam LocalDate startDate,
                      @RequestParam LocalDate endDate,
                      @RequestParam int budget,
                      @RequestParam String request,
                      Model model) {

        long nights = ChronoUnit.DAYS.between(startDate, endDate);
        log.info("=== Agent 요청: [{}] {} / {} ~ {} ({}박) / 예산 {}원 / 요청: {} ===",
                agentType, destination, startDate, endDate, nights, budget, request);

        // 선택한 Agent에게 사용자 요청 프롬프트를 전달한다
        String agentName;
        String result;
        switch (agentType) {
            case "transport" -> {
                agentName = "교통편 Agent";
                result = transportAgent.run(destination, budget, request);
            }
            case "hotel" -> {
                agentName = "숙소 Agent";
                result = hotelAgent.run(destination, nights, budget, request);
            }
            default -> {
                agentName = "식당 Agent";
                result = restaurantAgent.run(destination, budget, request);
            }
        }

        model.addAttribute("destination", destination);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("nights", nights);
        model.addAttribute("budget", budget);
        model.addAttribute("agentType", agentType);
        model.addAttribute("request", request);
        model.addAttribute("agentName", agentName);
        model.addAttribute("result", result);
        return "index";
    }
}
