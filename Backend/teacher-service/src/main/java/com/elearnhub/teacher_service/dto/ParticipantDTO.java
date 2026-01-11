package com.elearnhub.teacher_service.dto;

public class ParticipantDTO {
    private Long id;
    private String name;
    private String username;
    private String role;
    private String avatar;
	public ParticipantDTO(Long id, String name, String username, String role, String avatar) {
		super();
		this.id = id;
		this.name = name;
		this.username = username;
		this.role = role;
		this.avatar = avatar;
	}
	public ParticipantDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

    // Getters and setters...
    
    
}