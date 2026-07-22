package com.example.demo.dto;

import lombok.Data;

@Data
public class Requirements {
	private String destination;      // 확정된 여행지
	private Integer days;            // 여행 기간
	private Integer maxBudget;       // 사용자 최대 예산
}
