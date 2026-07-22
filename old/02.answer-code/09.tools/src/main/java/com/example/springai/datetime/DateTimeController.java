package com.example.springai.datetime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/chat")
@Slf4j
public class DateTimeController {

  @Autowired
  private DateTimeService dateTimeService;
  

  @PostMapping(
    value = "/date-time-tools",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String dateTimeTools(@RequestParam("question") String question) {
    String answer = dateTimeService.chat(question);
    return answer;
  }  
}

