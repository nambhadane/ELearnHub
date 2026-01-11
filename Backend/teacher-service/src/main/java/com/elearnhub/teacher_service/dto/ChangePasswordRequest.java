//package com.elearnhub.teacher_service.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class ChangePasswordRequest {
//    private String currentPassword;
//    private String newPassword;
//	public ChangePasswordRequest(String currentPassword, String newPassword) {
//		super();
//		this.currentPassword = currentPassword;
//		this.newPassword = newPassword;
//	}
//	public ChangePasswordRequest() {
//		super();
//		// TODO Auto-generated constructor stub
//	}
//	public String getCurrentPassword() {
//		return currentPassword;
//	}
//	public void setCurrentPassword(String currentPassword) {
//		this.currentPassword = currentPassword;
//	}
//	public String getNewPassword() {
//		return newPassword;
//	}
//	public void setNewPassword(String newPassword) {
//		this.newPassword = newPassword;
//	}
//    
//    


//}

package com.elearnhub.teacher_service.dto;

public class ChangePasswordRequest {

    private String currentPassword;
    private String newPassword;

    // No-Args Constructor
    public ChangePasswordRequest() {}

    // All-Args Constructor
    public ChangePasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    // Getters & Setters

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

