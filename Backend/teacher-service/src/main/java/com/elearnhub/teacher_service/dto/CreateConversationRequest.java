package com.elearnhub.teacher_service.dto;

import java.util.List;

public class CreateConversationRequest {
    private String type; // "DIRECT" or "GROUP"
    private String name; // For group chats
    private Long classId; // For class-based groups
    private List<Long> participantIds; // For direct chats or custom groups
	public CreateConversationRequest(String type, String name, Long classId, List<Long> participantIds) {
		super();
		this.type = type;
		this.name = name;
		this.classId = classId;
		this.participantIds = participantIds;
	}
	public CreateConversationRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getClassId() {
		return classId;
	}
	public void setClassId(Long classId) {
		this.classId = classId;
	}
	public List<Long> getParticipantIds() {
		return participantIds;
	}
	public void setParticipantIds(List<Long> participantIds) {
		this.participantIds = participantIds;
	}

    // Getters and setters...
    
    
}