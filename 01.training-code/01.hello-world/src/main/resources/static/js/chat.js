// 전역 변수
let conversationId = null;
let isTyping = false;

// DOM 요소
const chatForm = document.getElementById('chatForm');
const messageInput = document.getElementById('messageInput');
const messagesContainer = document.getElementById('messages');
const sendBtn = document.getElementById('sendBtn');
const settingsBtn = document.getElementById('settingsBtn');
const newChatBtn = document.getElementById('newChatBtn');
const settingsModal = document.getElementById('settingsModal');
const closeModal = document.getElementById('closeModal');
const uploadArea = document.getElementById('uploadArea');
const fileInput = document.getElementById('fileInput');
const uploadStatus = document.getElementById('uploadStatus');

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
    
    settingsBtn.addEventListener('click', () => {
        settingsModal.style.display = 'block';
    });
    
    closeModal.addEventListener('click', () => {
        settingsModal.style.display = 'none';
    });
    
    window.addEventListener('click', (e) => {
        if (e.target === settingsModal) {
            settingsModal.style.display = 'none';
        }
    });
    
    newChatBtn.addEventListener('click', startNewConversation);
    
    uploadArea.addEventListener('click', () => {
        fileInput.click();
    });
    
    fileInput.addEventListener('change', handleFileUpload);
    
    uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.style.borderColor = 'var(--primary-color)';
    });
    
    uploadArea.addEventListener('dragleave', () => {
        uploadArea.style.borderColor = 'var(--border-color)';
    });
    
    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.style.borderColor = 'var(--border-color)';
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            handleFile(files[0]);
        }
    });
}

// 메시지 전송 처리
async function handleSubmit(e) {
    e.preventDefault();
    
    const message = messageInput.value.trim();
    if (!message || isTyping) return;
    
    const welcomeMessage = document.querySelector('.welcome-message');
    if (welcomeMessage) {
        welcomeMessage.remove();
    }
    
    appendMessage(message, 'user');
    messageInput.value = '';
    adjustTextareaHeight();
    
    showTypingIndicator();
    isTyping = true;
    sendBtn.disabled = true;
    
    try {
        const response = await fetch('/api/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                message: message,
                conversationId: conversationId
            })
        });
        
        if (!response.ok) {
            throw new Error('서버 오류가 발생했습니다.');
        }
        
        const data = await response.json();
        conversationId = data.conversationId;
        hideTypingIndicator();
        appendMessage(data.message, 'assistant');
        
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

// 파일 업로드 처리
function handleFileUpload(e) {
    const file = e.target.files[0];
    if (file) {
        handleFile(file);
    }
}

// 파일 처리
async function handleFile(file) {
    const allowedTypes = ['application/pdf', 'text/plain'];
    if (!allowedTypes.includes(file.type)) {
        showUploadStatus('지원하지 않는 파일 형식입니다. PDF 또는 TXT 파일만 업로드 가능합니다.', 'error');
        return;
    }
    
    if (file.size > 10 * 1024 * 1024) {
        showUploadStatus('파일 크기가 너무 큽니다. 10MB 이하의 파일만 업로드 가능합니다.', 'error');
        return;
    }
    
    const formData = new FormData();
    formData.append('file', file);
    
    try {
        showUploadStatus('파일 업로드 중...', 'info');
        
        const response = await fetch('/api/settings/upload', {
            method: 'POST',
            body: formData
        });
        
        const data = await response.json();
        
        if (response.ok && data.status === 'success') {
            showUploadStatus(data.message, 'success');
            console.log('File uploaded:', data);
        } else {
            showUploadStatus(data.message || '파일 업로드에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('Upload error:', error);
        showUploadStatus('파일 업로드 중 오류가 발생했습니다.', 'error');
    }
}

// 업로드 상태 표시
function showUploadStatus(message, type) {
    uploadStatus.textContent = message;
    uploadStatus.className = `upload-status ${type}`;
    uploadStatus.style.display = 'block';
    
    if (type === 'success' || type === 'error') {
        setTimeout(() => {
            uploadStatus.style.display = 'none';
        }, 5000);
    }
}
