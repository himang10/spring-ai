package com.example.multiagent.controller;

import com.example.multiagent.orchestrator.AgentTools;
import com.example.multiagent.orchestrator.OrchestratorAgent;
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

    private final OrchestratorAgent orchestratorAgent;
    private final AgentTools agentTools;

    public TripController(OrchestratorAgent orchestratorAgent, AgentTools agentTools) {
        this.orchestratorAgent = orchestratorAgent;
        this.agentTools = agentTools;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/plan")
    public String plan(@RequestParam String destination,
                       @RequestParam LocalDate startDate,
                       @RequestParam LocalDate endDate,
                       @RequestParam int budget,
                       @RequestParam String request,
                       Model model) {

        long nights = ChronoUnit.DAYS.between(startDate, endDate);
        log.info("=== Multi-Agent 출장 계획 시작: {} / {} ~ {} ({}박) / 예산 {}원 / 요청: {} ===",
                destination, startDate, endDate, nights, budget, request);

        // Controller는 순서를 모른다. Orchestrator가 요청을 분해해서 하위 Agent 호출을 결정한다
        agentTools.clearSteps();
        String finalPlan = orchestratorAgent.run(destination, nights, budget, request);

        model.addAttribute("destination", destination);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("nights", nights);
        model.addAttribute("budget", budget);
        model.addAttribute("request", request);
        model.addAttribute("steps", agentTools.getSteps());
        model.addAttribute("finalPlan", finalPlan);
        return "index";
    }
}
