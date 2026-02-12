// 전역 변수
let conversationId = null;
let projectName = 'default-project'; // Vector Store용 프로젝트 이름
let isTyping = false;
let apiPath = '/chat/first-embedding'; // 기본 API 경로

// URL 파라미터에서 API 경로 가져오기
const urlParams = new URLSearchParams(window.location.search);
const pathParam = urlParams.get('path');
if (pathParam) {
    apiPath = pathParam;
}

// DOM 요소
const chatForm = document.getElementById('chatForm');
const messageInput = document.getElementById('messageInput');
const messagesContainer = document.getElementById('messages');
const sendBtn = document.getElementById('sendBtn');
const newChatBtn = document.getElementById('newChatBtn');

// 초기화
document.addEventListener('DOMContentLoaded', () => {
    initializeEventListeners();
    adjustTextareaHeight();
});

// 이벤트 리스너 초기화
function initializeEventListeners() {
    chatForm.addEventListener('submit', handleSubmit);
    messageInput.addEventListener('input', adjustTextareaHeight);
    messageInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey && !e.isComposing) {
            e.preventDefault();
            handleSubmit(e);
        }
    });
    
    // Path Selector 이벤트
    const pathSelector = document.getElementById('pathSelector');
    if (pathSelector) {
        // URL 파라미터로 초기값 설정
        pathSelector.value = apiPath;
        
        // Path별 placeholder 매핑
        const placeholderMap = {
            '/chat/first-embedding': 'Embedding 텍스트를 입력하세요',
            '/chat/text-embedding': 'Embedding 텍스트를 입력하세요',
            '/chat/add-documents': '문서들을 초기화합니다(입력 불필요)',
            '/chat/search': 'Spring AI에 대해 알려주세요',
            '/chat/search-with-filter': 'Spring boot에 대해 알려주세요',
            '/chat/delete-document': 'kubernetes 문서를 삭제합니다',
            '/chat/delete-all-documents': '클릭하여 모든 문서를 삭제하세요 (입력 불필요)'
        };
        
        // 선택 변경 시 API Path 및 placeholder 업데이트
        pathSelector.addEventListener('change', function() {
            apiPath = this.value;
            console.log('API Path 변경됨:', apiPath);
            
            // placeholder와 값 업데이트
            if (placeholderMap[apiPath]) {
                messageInput.value = placeholderMap[apiPath];
                messageInput.placeholder = placeholderMap[apiPath];
            }
        });
    }
    
    newChatBtn.addEventListener('click', startNewConversation);
}

// 메시지 전송 처리
async function handleSubmit(e) {
    e.preventDefault();
    
    const message = messageInput.value.trim();
    
    // add-documents와 delete-all-documents는 입력 없이도 실행 가능
    if ((apiPath !== '/chat/add-documents' && apiPath !== '/chat/delete-all-documents') && !message) return;
    if (isTyping) return;
    
    const welcomeMessage = document.querySelector('.welcome-message');
    if (welcomeMessage) {
        welcomeMessage.remove();
    }
    
    // 메시지가 있는 경우에만 표시
    if (message) {
        appendMessage(message, 'user');
    }
    messageInput.value = '';
    adjustTextareaHeight();
    
    showTypingIndicator();
    isTyping = true;
    sendBtn.disabled = true;
    
    try {
        // Form data 생성
        const formData = new URLSearchParams();
        
        // API별 파라미터 설정
        if (apiPath === '/chat/first-embedding' || apiPath === '/chat/text-embedding' || apiPath === '/chat/search' || apiPath === '/chat/search-with-filter') {
            formData.append('question', message);
        } else if (apiPath === '/chat/delete-document-for-similarity' || apiPath === '/chat/add-document') {
            formData.append('request', message);
        } 
        // add-documents와 delete-all-documents는 파라미터 불필요
        
        const response = await fetch(apiPath, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: formData.toString()
        });
        
        if (!response.ok) {
            throw new Error('서버 오류가 발생했습니다.');
        }
        
        const contentType = response.headers.get('content-type');
        hideTypingIndicator();
        
        // JSON 응답인 경우 (search, search-with-filter)
        if (contentType && contentType.includes('application/json')) {
            const documents = await response.json();
            console.log('Received documents:', documents); // 디버깅용
            if (documents && documents.length > 0) {
                let resultText = `검색 결과 (${documents.length}건):\n\n`;
                documents.forEach((doc, index) => {
                    console.log(`Document ${index}:`, doc); // 각 문서 구조 확인
                    // Spring AI Document 객체는 보통 id, content, metadata 속성을 가짐
                    const text = doc.content || doc.text || doc.docContent || '내용 없음';
                    resultText += `[${index + 1}] ${text}\n`;
                    if (doc.metadata) {
                        resultText += `   메타데이터: ${JSON.stringify(doc.metadata)}\n`;
                    }
                    resultText += `\n`;
                });
                appendMessage(resultText, 'assistant');
            } else {
                appendMessage('검색 결과가 없습니다.', 'assistant');
            }
        } else {
            // Text 응답인 경우
            const data = await response.text();
            appendMessage(data, 'assistant');
        }
        
    } catch (error) {
        console.error('Error:', error);
        hideTypingIndicator();
        appendMessage('죄송합니다. 오류가 발생했습니다. 다시 시도해주세요.', 'assistant');
    } finally {
        isTyping = false;
        sendBtn.disabled = false;
        messageInput.focus();
    }
}

// 메시지 추가
function appendMessage(content, role) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${role}`;
    
    const avatar = document.createElement('div');
    avatar.className = 'message-avatar';
    avatar.textContent = role === 'user' ? '👤' : '🤖';
    
    const contentDiv = document.createElement('div');
    contentDiv.className = 'message-content';
    
    // 줄바꿈 처리
    if (content.includes('\n')) {
        contentDiv.style.whiteSpace = 'pre-wrap';
    }
    contentDiv.textContent = content;
    
    messageDiv.appendChild(avatar);
    messageDiv.appendChild(contentDiv);
    
    messagesContainer.appendChild(messageDiv);
    scrollToBottom();
}

// 타이핑 인디케이터 표시
function showTypingIndicator() {
    const indicator = document.createElement('div');
    indicator.id = 'typingIndicator';
    indicator.className = 'message assistant';
    indicator.innerHTML = `
        <div class="message-avatar">🤖</div>
        <div class="typing-indicator">
            <span></span>
            <span></span>
            <span></span>
        </div>
    `;
    messagesContainer.appendChild(indicator);
    scrollToBottom();
}

// 타이핑 인디케이터 제거
function hideTypingIndicator() {
    const indicator = document.getElementById('typingIndicator');
    if (indicator) {
        indicator.remove();
    }
}

// 스크롤을 맨 아래로
function scrollToBottom() {
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

// textarea 높이 자동 조절
function adjustTextareaHeight() {
    messageInput.style.height = 'auto';
    messageInput.style.height = Math.min(messageInput.scrollHeight, 200) + 'px';
}

// 새 대화 시작
async function startNewConversation() {
    if (confirm('새 대화를 시작하시겠습니까? 현재 대화 내용이 초기화됩니다.')) {
        try {
            const response = await fetch('/api/chat/new', {
                method: 'POST'
            });
            
            if (response.ok) {
                conversationId = await response.text();
                messagesContainer.innerHTML = `
                    <div class="welcome-message">
                        <h2>👋 안녕하세요!</h2>
                        <p>무엇을 도와드릴까요?</p>
                    </div>
                `;
            }
        } catch (error) {
            console.error('Error starting new conversation:', error);
            alert('새 대화를 시작하는 중 오류가 발생했습니다.');
        }
    }
}
