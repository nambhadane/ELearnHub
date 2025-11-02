//package com.elearnhub.teacher_service.repository;
//
////import com.elearnhub.teacherservice.entity.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import com.elearnhub.teacher_service.entity.User;
//
//public interface UserRepository extends JpaRepository<User, Long> {
//    User findByUsername(String username);
//    boolean existsByEmail(String email);
//}

package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}