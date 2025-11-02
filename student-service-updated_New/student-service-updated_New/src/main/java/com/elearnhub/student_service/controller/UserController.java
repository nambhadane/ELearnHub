//package com.elearnhub.student_service.controller;
//
//import com.elearnhub.student_service.entity.User;
//import com.elearnhub.student_service.service.UserService;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/users")
//public class UserController {
//
//    @Autowired
//    private UserService userService;
//
//    // Add a new user
//    @PostMapping
//    public ResponseEntity<String> registerUser(@Valid @RequestBody User user) {
//        try {
//            userService.addUser(user);
//            return ResponseEntity.ok("User registered successfully!");
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
//        }
//    }
//
//    // Get all users
//    @GetMapping
//    public ResponseEntity<List<User>> getAllUsers() {
//        List<User> users = userService.getAllUsers();
//        return ResponseEntity.ok(users);
//    }
//
//    // Get user by ID
//    @GetMapping("/{userId}")
//    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
//        return userService.getUserById(userId)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    // Update user
//    @PutMapping("/{userId}")
//    public ResponseEntity<String> updateUser(@PathVariable Long userId, @Valid @RequestBody User userDetails) {
//        userService.updateUser(userId, userDetails);
//        return ResponseEntity.ok("User updated successfully!");
//    }
//
//    // Delete user
//    @DeleteMapping("/{userId}")
//    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
//        userService.deleteUser(userId);
//        return ResponseEntity.ok("User deleted successfully!");
//    }
//}