package com.example.springai.advisor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JSON 형식으로 LLM 요청/응답을 로깅하는 Advisor 구현체.
 * <p>
 * Spring AI의 {@link CallAdvisor} 인터페이스를 구현하여 ChatClient의 요청과 응답을
 * JSON 형식으로 콘솔에 출력합니다. 디버깅 및 모니터링 용도로 사용됩니다.
 * </p>
 * <p>
 * {@link org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor} 패턴을 참고하여 구현되었습니다.
 * </p>
 * 
 * @author SKALA Team
 * @since 1.0.0
 * @see org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
 */
public class JsonLoggingAdvisor implements CallAdvisor {

    /**
     * JSON 직렬화를 위한 ObjectMapper 인스턴스.
     * INDENT_OUTPUT이 활성화되어 가독성 있는 JSON 형식으로 출력됩니다.
     */
    private final ObjectMapper objectMapper;
    
    /**
     * Advisor의 실행 순서를 결정하는 order 값.
     * 값이 작을수록 먼저 실행됩니다.
     */
    private final int order;

    /**
     * 기본 생성자.
     * <p>
     * 새로운 {@link ObjectMapper} 인스턴스를 생성하고 order를 0으로 설정합니다.
     * </p>
     */
    public JsonLoggingAdvisor() {
        this(new ObjectMapper(), 0);
    }

    /**
     * ObjectMapper를 받는 생성자.
     * <p>
     * order는 기본값 0으로 설정됩니다.
     * </p>
     * 
     * @param objectMapper JSON 직렬화에 사용할 ObjectMapper
     */
    public JsonLoggingAdvisor(ObjectMapper objectMapper) {
        this(objectMapper, 0);
    }

    /**
     * 전체 파라미터를 받는 생성자.
     * 
     * @param objectMapper JSON 직렬화에 사용할 ObjectMapper
     * @param order Advisor 실행 순서 (값이 작을수록 먼저 실행)
     */
    public JsonLoggingAdvisor(ObjectMapper objectMapper, int order) {
        this.objectMapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
        this.order = order;
    }

    /**
     * Advisor의 이름을 반환합니다.
     * 
     * @return 클래스의 단순 이름 (JsonLoggingAdvisor)
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Advisor의 실행 순서를 반환합니다.
     * 
     * @return order 값
     */
    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * ChatClient 호출을 가로채서 요청과 응답을 JSON 형식으로 로깅합니다.
     * <p>
     * 이 메소드는 다음 순서로 실행됩니다:
     * <ol>
     *   <li>요청 로깅 시작 헤더 출력</li>
     *   <li>LLM 요청 정보를 JSON 형식으로 출력</li>
     *   <li>다음 advisor 체인 호출</li>
     *   <li>LLM 응답 정보를 JSON 형식으로 출력</li>
     *   <li>요청 로깅 종료 헤더 출력</li>
     * </ol>
     * </p>
     * 
     * @param request ChatClient 요청 객체
     * @param chain 다음 advisor를 호출하기 위한 체인
     * @return ChatClient 응답 객체
     */
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logRequest(request);
        
        ChatClientResponse response = chain.nextCall(request);
        
        logResponse(response);
        
        return response;
    }

    /**
     * LLM 요청 정보를 JSON 형식으로 로깅합니다.
     * <p>
     * 다음 정보가 포함됩니다:
     * <ul>
     *   <li>messages: 메시지 목록 (시스템, 사용자, 어시스턴트 메시지)</li>
     *   <li>options: ChatOptions (모델, 온도, topP, maxTokens 등)</li>
     *   <li>context: 컨텍스트 정보</li>
     * </ul>
     * </p>
     * 
     * @param request ChatClient 요청 객체
     */
    private void logRequest(ChatClientRequest request) {
        Prompt prompt = request.prompt();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messages", prompt.getInstructions().stream()
            .map(this::messageToMap)
            .collect(Collectors.toList()));
        payload.put("options", chatOptionsToMap(prompt.getOptions()));
        payload.put("context", request.context());
        writeJson("[Advisor] LLM 요청", payload);
    }

    /**
     * LLM 응답 정보를 JSON 형식으로 로깅합니다.
     * <p>
     * 다음 정보가 포함됩니다:
     * <ul>
     *   <li>metadata: 응답 메타데이터 (ID, 모델, usage, rateLimit 등)</li>
     *   <li>results: 생성된 응답 결과 목록</li>
     *   <li>context: 컨텍스트 정보</li>
     * </ul>
     * </p>
     * 
     * @param response ChatClient 응답 객체
     */
    private void logResponse(ChatClientResponse response) {
        Map<String, Object> payload = new LinkedHashMap<>();
        ChatResponse chatResponse = response.chatResponse();
        payload.put("metadata", chatResponse.getMetadata());
        payload.put("results", chatResponse.getResults().stream()
            .map(this::generationToMap)
            .collect(Collectors.toList()));
        payload.put("context", response.context());
        writeJson("[Advisor] LLM 응답", payload);
    }

    /**
     * Message 객체를 Map으로 변환합니다.
     * <p>
     * 메시지의 타입, 텍스트 내용, 메타데이터를 포함합니다.
     * </p>
     * 
     * @param message 변환할 Message 객체
     * @return 메시지 정보를 담은 Map
     */
    private Map<String, Object> messageToMap(Message message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", message.getMessageType().name());
        map.put("text", message.getText());
        map.put("metadata", message.getMetadata());
        return map;
    }

    /**
     * ChatOptions 객체를 Map으로 변환합니다.
     * <p>
     * 모델, 온도, topP, maxTokens 등의 옵션 정보를 포함합니다.
     * </p>
     * 
     * @param chatOptions 변환할 ChatOptions 객체 (null 가능)
     * @return 옵션 정보를 담은 Map (chatOptions가 null이면 빈 Map 반환)
     */
    private Map<String, Object> chatOptionsToMap(ChatOptions chatOptions) {
        if (chatOptions == null) {
            return Map.of();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("model", chatOptions.getModel());
        map.put("temperature", chatOptions.getTemperature());
        map.put("topP", chatOptions.getTopP());
        map.put("maxTokens", chatOptions.getMaxTokens());
        map.put("presencePenalty", chatOptions.getPresencePenalty());
        map.put("frequencyPenalty", chatOptions.getFrequencyPenalty());
        return map;
    }

    /**
     * Generation 객체를 Map으로 변환합니다.
     * <p>
     * 어시스턴트 메시지, 메타데이터, 도구 호출 여부를 포함합니다.
     * </p>
     * 
     * @param generation 변환할 Generation 객체
     * @return 생성 결과 정보를 담은 Map
     */
    private Map<String, Object> generationToMap(Generation generation) {
        Map<String, Object> map = new LinkedHashMap<>();
        AssistantMessage assistantMessage = generation.getOutput();
        map.put("assistantMessage", assistantMessage != null ? messageToMap(assistantMessage) : null);
        map.put("metadata", generation.getMetadata());
        map.put("hasToolCalls", assistantMessage != null && assistantMessage.hasToolCalls());
        return map;
    }

    /**
     * 제목과 페이로드를 JSON 형식으로 콘솔에 출력합니다.
     * 
     * @param title 출력할 제목
     * @param payload JSON으로 변환할 Map 데이터
     */
    private void writeJson(String title, Map<String, Object> payload) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println(title);
        System.out.println("=".repeat(80));
        try {
            String json = objectMapper.writeValueAsString(payload);
            System.out.println(json);
        } catch (Exception e) {
            System.err.println("JSON 직렬화 실패: " + e.getMessage());
        }
        System.out.println("=".repeat(80) + "\n");
    }

    /**
     * Advisor의 문자열 표현을 반환합니다.
     * 
     * @return 클래스의 단순 이름
     */
    @Override
    public String toString() {
        return JsonLoggingAdvisor.class.getSimpleName();
    }
}

