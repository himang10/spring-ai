package com.example.springai.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleLoggingAdvisor implements CallAdvisor {

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public int getOrder() {
		return 0;
	}


	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
		logRequest(chatClientRequest);

		ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

		logResponse(chatClientResponse);

		return chatClientResponse;
	}

	private void logRequest(ChatClientRequest request) {
		System.out.println("SimpleLoggingAdvisor - Request 로그 출력: " + request);
	}

	private void logResponse(ChatClientResponse chatClientResponse) {
		System.out.println("SimpleLoggingAdvisor - Response 로그 출력: " + chatClientResponse);
	}

}
