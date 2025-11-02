//package com.elearnhub.student_service.service;
//
//
//import com.elearnhub.student_service.entity.User;
//import com.elearnhub.student_service.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//
//@Service
//public class UserDetailsServiceImpl implements UserDetailsService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        // Temporary in-memory user for testing (remove after database setup)
//        if ("student10".equals(username)) {
//            return org.springframework.security.core.userdetails.User
//                    .withUsername("student10")
//                    .password("pass123")
//                    .roles("USER")
//                    .build();
//        }
//
//        // Database lookup
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
//        // Convert single role string to array for Spring Security User
//        String[] roles = {user.getRole()}; // Adjust if role needs to be split (e.g., user.getRole().split(","))
//        return org.springframework.security.core.userdetails.User
//                .withUsername(user.getUsername())
//                .password(user.getPassword())
//                .roles(roles)
//                .build();
//    }
//}