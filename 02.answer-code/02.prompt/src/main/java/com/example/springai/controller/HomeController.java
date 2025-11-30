package com.example.springai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Controller
public class HomeController {
    
    /**
     * 메인 채팅 페이지
     */
    @GetMapping("/")
    public String index() {
        return "home";
    }
}
