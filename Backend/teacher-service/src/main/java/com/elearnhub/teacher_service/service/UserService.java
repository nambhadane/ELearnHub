package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.dto.UserDTO;
import com.elearnhub.teacher_service.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(User user);
    Optional<User> getUserById(Long id);
    List<UserDTO> getAllUsers();
    User updateUser(Long id, User user);
    User updateUser(User user); // ✅ ADD: Simple update method
    void deleteUser(Long id);
    boolean resetPassword(String username, String newPlainPassword);
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<ParticipantDTO> getAllTeachers();
    List<ParticipantDTO> getAllStudents();
    
    // ✅ CRITICAL: This method must save the user to database
    void save(User user);
}
