package com.example.springai.advisor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
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
        System.out.println("\n" + "=".repeat(80));
        System.out.println("[FLOW] ChatClient -> LLM 요청 시작");
        System.out.println("=".repeat(80));
        
        logRequest(request);
        
        System.out.println("\n" + "-".repeat(80));
        System.out.println("[FLOW] ChatClient가 LLM API를 호출합니다...");
        System.out.println("-".repeat(80) + "\n");
        
        ChatClientResponse response = chain.nextCall(request);
        
        System.out.println("\n" + "-".repeat(80));
        System.out.println("[FLOW] LLM -> ChatClient 응답 수신 완료");
        System.out.println("-".repeat(80) + "\n");
        
        logResponse(response);
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("[FLOW] ChatClient -> LLM 요청 완료");
        System.out.println("=".repeat(80) + "\n");
        
        return response;
    }

    /**
     * LLM 요청 정보를 JSON 형식으로 로깅합니다.
     * <p>
     * 다음 정보가 포함됩니다:
     * <ul>
     *   <li>messages: 메시지 목록 (시스템, 사용자, 어시스턴트 메시지)</li>
     *   <li>options: ChatOptions (모델, 온도, topP, maxTokens 등)</li>
     *   <li>tools: Tool 정의 목록 (Tool Calling 사용 시)</li>
     *   <li>context: 컨텍스트 정보</li>
     * </ul>
     * </p>
     * 
     * @param request ChatClient 요청 객체
     */
    private void logRequest(ChatClientRequest request) {
        Prompt prompt = request.prompt();
        Map<String, Object> payload = new LinkedHashMap<>();
        
        System.out.println("[REQUEST PAYLOAD] ChatClient가 LLM에 전송하는 데이터:");
        
        // Messages 로깅
        payload.put("messages", prompt.getInstructions().stream()
            .map(this::messageToMap)
            .collect(Collectors.toList()));
        
        // ChatOptions 로깅 (Tool 정보 포함)
        Map<String, Object> optionsMap = chatOptionsToMap(prompt.getOptions());
        payload.put("options", optionsMap);
        
        // Tool Definitions 명시적 로깅
        boolean hasToolDefinitions = checkAndLogToolDefinitions(request);
        if (hasToolDefinitions) {
            System.out.println("\n[TOOL DEFINITIONS] ChatClient가 다음 Tool 정보를 LLM에 전달합니다:");
            System.out.println("- LLM은 이 Tool 정의를 보고 필요한 Tool을 선택할 수 있습니다");
            System.out.println("- Tool 선택 시 LLM은 JSON 형식으로 Tool Call을 응답합니다\n");
        }
        
        // Context 로깅
        payload.put("context", request.context());
        
        writeJson("LLM 요청 내용", payload);
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
        ChatResponse chatResponse = response.chatResponse();
        
        // Tool Call 응답인지 일반 텍스트 응답인지 확인
        boolean hasToolCalls = chatResponse.getResults().stream()
            .anyMatch(result -> result.getOutput() != null && result.getOutput().hasToolCalls());
        
        if (hasToolCalls) {
            System.out.println("[RESPONSE TYPE] LLM이 Tool Call을 요청하는 JSON 응답을 반환했습니다:");
            System.out.println("- LLM이 어떤 Tool을 호출해야 하는지 결정했습니다");
            System.out.println("- ChatClient는 이를 FunctionCall로 변환하여 실제 Tool을 실행합니다\n");
        } else {
            System.out.println("[RESPONSE TYPE] LLM이 최종 AssistantMessage를 반환했습니다:");
            System.out.println("- Tool 실행 결과를 바탕으로 생성된 최종 답변입니다\n");
        }
        
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("metadata", chatResponse.getMetadata());
        payload.put("results", chatResponse.getResults().stream()
            .map(this::generationToMap)
            .collect(Collectors.toList()));
        payload.put("context", response.context());
        writeJson("LLM 응답 내용", payload);
    }

    /**
     * Message 객체를 Map으로 변환합니다.
     * <p>
     * 메시지의 타입, 텍스트 내용, 메타데이터를 포함합니다.
     * Tool Response 메시지인 경우 추가 정보를 로깅합니다.
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
        
        // Tool Response 메시지인 경우 추가 정보
        if (message instanceof ToolResponseMessage) {
            ToolResponseMessage toolResponse = (ToolResponseMessage) message;
            Map<String, Object> toolInfo = new LinkedHashMap<>();
            toolInfo.put("responses", toolResponse.getResponses());
            map.put("toolResponseInfo", toolInfo);
            
            // Tool Response가 LLM에 전달되는 형식 로깅
            System.out.println("\n" + "=".repeat(80));
            System.out.println("[TOOL RESPONSE MESSAGE] Tool 실행 결과를 LLM에 전송하는 형식");
            System.out.println("=".repeat(80));
            System.out.println("\n[설명]");
            System.out.println("- ChatClient가 Tool 실행 결과를 ToolResponseMessage로 변환합니다");
            System.out.println("- 이 메시지는 role: 'tool'로 LLM에 전달됩니다");
            System.out.println("- LLM은 이 결과를 보고 최종 AssistantMessage를 생성합니다\n");
            
            System.out.println("[ToolResponseMessage 형식]");
            toolResponse.getResponses().forEach(response -> {
                System.out.println("{");
                System.out.println("  \"role\": \"tool\",");
                System.out.println("  \"tool_call_id\": \"" + response.id() + "\",");
                System.out.println("  \"name\": \"" + response.name() + "\",");
                System.out.println("  \"content\": \"" + response.responseData() + "\"");
                System.out.println("}");
            });
            System.out.println("\n" + "=".repeat(80) + "\n");
        }
        
        return map;
    }

    /**
     * ChatOptions 객체를 Map으로 변환합니다.
     * <p>
     * 모델, 온도, topP, maxTokens 등의 옵션 정보와
     * Tool Calling 관련 정보를 포함합니다.
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
        
        // Tool Calling 관련 정보 로깅 (toString()에서 확인)
        String optionsString = chatOptions.toString();
        if (optionsString.contains("toolContext") || optionsString.contains("toolCallback")) {
            map.put("toolCallbacksConfigured", true);
        }
        
        return map;
    }

    /**
     * Generation 객체를 Map으로 변환합니다.
     * <p>
     * 어시스턴트 메시지, 메타데이터, 도구 호출 정보를 포함합니다.
     * </p>
     * 
     * @param generation 변환할 Generation 객체
     * @return 생성 결과 정보를 담은 Map
     */
    private Map<String, Object> generationToMap(Generation generation) {
        Map<String, Object> map = new LinkedHashMap<>();
        AssistantMessage assistantMessage = generation.getOutput();
        
        if (assistantMessage != null) {
            map.put("assistantMessage", messageToMap(assistantMessage));
            
            // Tool Call 정보 상세 로깅
            if (assistantMessage.hasToolCalls()) {
                map.put("hasToolCalls", true);
                map.put("toolCalls", assistantMessage.getToolCalls().stream()
                    .map(this::toolCallToMap)
                    .collect(Collectors.toList()));
                
                // Tool Call 처리 플로우 상세 표시
                System.out.println("\n" + ">".repeat(80));
                System.out.println("[TOOL CALL FLOW - Step 1] LLM이 JSON 형식으로 Tool Call을 응답했습니다");
                System.out.println(">".repeat(80));
                
                assistantMessage.getToolCalls().forEach(toolCall -> {
                    System.out.println("\n  [LLM Response - Tool Call JSON]");
                    System.out.println("  {");
                    System.out.println("    \"id\": \"" + toolCall.id() + "\",");
                    System.out.println("    \"type\": \"" + toolCall.type() + "\",");
                    System.out.println("    \"function\": {");
                    System.out.println("      \"name\": \"" + toolCall.name() + "\",");
                    System.out.println("      \"arguments\": " + toolCall.arguments());
                    System.out.println("    }");
                    System.out.println("  }");
                });
                
                System.out.println("\n" + ">".repeat(80));
                System.out.println("[TOOL CALL FLOW - Step 2] ChatClient가 JSON을 FunctionCall로 변환합니다");
                System.out.println(">".repeat(80));
                
                assistantMessage.getToolCalls().forEach(toolCall -> {
                    System.out.println("\n  [ChatClient Internal Processing]");
                    System.out.println("  JSON Tool Call -> FunctionCall 변환");
                    System.out.println("  - Function Name: " + toolCall.name());
                    System.out.println("  - Call ID: " + toolCall.id());
                    System.out.println("  - Arguments: " + toolCall.arguments());
                });
                
                System.out.println("\n" + ">".repeat(80));
                System.out.println("[TOOL CALL FLOW - Step 3] ChatClient가 Tool을 실행합니다");
                System.out.println(">".repeat(80));
                
                System.out.println("\n[Tool 실행 방식 구분]");
                System.out.println("1. 로컬 Tool (@Tool 어노테이션):");
                System.out.println("   - ChatClient -> ToolService.methodName() 직접 호출");
                System.out.println("   - 현재 JVM 내에서 메서드 실행\n");
                
                System.out.println("2. MCP Server Tool (Model Context Protocol):");
                System.out.println("   - ChatClient -> MCP Server (tools/list 요청)");
                System.out.println("   - MCP Server -> ChatClient (사용 가능한 Tool 목록 응답)");
                System.out.println("   - ChatClient -> MCP Server (tools/call 요청)");
                System.out.println("   - MCP Server -> ChatClient (Tool 실행 결과 응답)\n");
                
                assistantMessage.getToolCalls().forEach(toolCall -> {
                    System.out.println("\n  [Tool 실행 - " + toolCall.name() + "]");
                    System.out.println("  방식: 로컬 Tool (ToolService 직접 호출)");
                    System.out.println("  Executing: ToolService." + toolCall.name() + "()");
                    System.out.println("  Tool이 실행되고 결과를 반환합니다...");
                });
                
                System.out.println("\n" + ">".repeat(80));
                System.out.println("[TOOL CALL FLOW - Step 4] Tool 실행 결과를 받았습니다");
                System.out.println(">".repeat(80));
                System.out.println("\n[다음 단계]");
                System.out.println("- ChatClient가 Tool 결과를 ToolResponseMessage로 변환합니다");
                System.out.println("- ToolResponseMessage를 포함하여 LLM에 재요청합니다");
                System.out.println("- LLM은 Tool 결과를 바탕으로 최종 AssistantMessage를 생성합니다");
                System.out.println(">".repeat(80) + "\n");
            } else {
                map.put("hasToolCalls", false);
            }
        } else {
            map.put("assistantMessage", null);
            map.put("hasToolCalls", false);
        }
        
        map.put("metadata", generation.getMetadata());
        return map;
    }
    
    /**
     * ToolCall 객체를 Map으로 변환합니다.
     * 
     * @param toolCall 변환할 ToolCall 객체
     * @return Tool 호출 정보를 담은 Map
     */
    private Map<String, Object> toolCallToMap(AssistantMessage.ToolCall toolCall) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", toolCall.id());
        map.put("type", toolCall.type());
        map.put("name", toolCall.name());
        map.put("arguments", toolCall.arguments());
        return map;
    }
    
    /**
     * 요청에 Tool Definitions가 포함되어 있는지 확인하고 로깅합니다.
     * 
     * @param request ChatClient 요청 객체
     * @return Tool이 포함되어 있으면 true, 아니면 false
     */
    private boolean checkAndLogToolDefinitions(ChatClientRequest request) {
        try {
            // ChatOptions를 통해 Tool 설정 여부 확인
            Prompt prompt = request.prompt();
            ChatOptions options = prompt.getOptions();
            
            if (options != null) {
                String optionsString = options.toString();
                // Tool callback이나 tool context가 있는지 확인
                if (optionsString.contains("toolCallback") || optionsString.contains("toolContext") 
                    || optionsString.contains("function")) {
                    
                    // Tool Definitions 상세 로깅
                    System.out.println("\n" + "=".repeat(80));
                    System.out.println("[TOOL DEFINITIONS] ChatClient가 Tool 정보를 LLM에 전달합니다");
                    System.out.println("=".repeat(80));
                    System.out.println("\n[설명]");
                    System.out.println("- LLM은 이 Tool 정의(JSON Schema)를 보고 필요한 Tool을 선택합니다");
                    System.out.println("- Tool 선택 시 LLM은 JSON 형식으로 Tool Call을 응답합니다");
                    System.out.println("- OpenAI Function Calling 형식: tools 파라미터로 전달됩니다\n");
                    
                    System.out.println("[Tool Definitions 형식 예시]");
                    System.out.println("{");
                    System.out.println("  \"tools\": [");
                    System.out.println("    {");
                    System.out.println("      \"type\": \"function\",");
                    System.out.println("      \"function\": {");
                    System.out.println("        \"name\": \"getCurrentTime\",");
                    System.out.println("        \"description\": \"현재 시간 정보를 가져옵니다...\",");
                    System.out.println("        \"parameters\": {");
                    System.out.println("          \"type\": \"object\",");
                    System.out.println("          \"properties\": {},");
                    System.out.println("          \"required\": []");
                    System.out.println("        }");
                    System.out.println("      }");
                    System.out.println("    },");
                    System.out.println("    { ...다른 Tool 정의들... }");
                    System.out.println("  ]");
                    System.out.println("}");
                    System.out.println("\n" + "=".repeat(80) + "\n");
                    
                    return true;
                }
            }
            
            // Context에서 tools 확인
            Map<String, Object> context = request.context();
            if (context != null && context.containsKey("tools")) {
                return true;
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
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
