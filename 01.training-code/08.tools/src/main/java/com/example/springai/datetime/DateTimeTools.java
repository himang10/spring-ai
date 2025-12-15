package com.example.springai.datetime;

import java.time.LocalDateTime;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DateTimeTools {
  @Tool(description = "사용자의 시간대에 맞는 현재 날짜와 시간 정보를 제공합니다.")
  public String getCurrentDateTime() {
    String nowTime = LocalDateTime.now()
            .atZone(LocaleContextHolder.getTimeZone().toZoneId())
            .toString();
    log.info("현재 시간: {}", nowTime);
    return nowTime;
  }

  @Tool(description = "주어진 시간에 사용자 알람을 설정합니다. 시간은 ISO-8601 형식입니다. 결과는 현재 시간과 알람 시간을 포함하여 반환합니다.")
  public String setAlarm(
      @ToolParam(description = "ISO-8601 형식의 시간", required = true) 
      String time) {
        System.out.println("알람이 설정되었습니다: " + time);

        StringBuilder sb = new StringBuilder();
        sb.append("현재 시간: ")
          .append(getCurrentDateTime())
          .append("\n");
        sb.append("알람 시간: ")
          .append(time) 
          .append("\n");

        return sb.toString();
  } 
}
