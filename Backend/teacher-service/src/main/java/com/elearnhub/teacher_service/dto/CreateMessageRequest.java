package com.elearnhub.teacher_service.dto;

public class CreateMessageRequest {
    private Long conversationId;
    private String content;
	public CreateMessageRequest(Long conversationId, String content) {
		super();
		this.conversationId = conversationId;
		this.content = content;
	}
	public CreateMessageRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Long getConversationId() {
		return conversationId;
	}
	public void setConversationId(Long conversationId) {
		this.conversationId = conversationId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

    // Getters and setters...
    
    
}