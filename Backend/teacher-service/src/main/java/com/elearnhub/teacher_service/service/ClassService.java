//package com.elearnhub.teacher_service.service;
//
//import com.elearnhub.teacher_service.dto.ClassDTO;
//import com.elearnhub.teacher_service.dto.ParticipantDTO;
//import com.elearnhub.teacher_service.entity.ClassEntity;
//import java.util.List;
//import java.util.Optional;
//
//public interface ClassService {
//    
//    ClassDTO createClass(Long teacherId, Long courseId, String name);
//    
//    List<ClassDTO> getClassesByTeacher(Long teacherId);
//    
//    Optional<ClassEntity> getClassById(Long classId);
//    
//    void addStudentToClass(Long classId, Long studentId);
//    
//    List<ClassDTO> getClassesForStudent(Long studentId);
//    
//    List<ParticipantDTO> getClassStudents(Long classId);
//    
//    boolean hasAccessToClass(Long classId, Long userId);
//    
//    List<ClassDTO> getClassesByStudent(Long studentId);
//
//	void removeStudentFromClass(Long classId, Long studentId);
//    
//  
//}

package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.ClassDTO;
import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.entity.ClassEntity;
import java.util.List;
import java.util.Optional;

public interface ClassService {
    
    // Create a new class
    ClassDTO createClass(Long teacherId, Long courseId, String name);
    
    // Get class by ID
    Optional<ClassEntity> getClassById(Long classId);
    
    // Get classes by teacher
    List<ClassDTO> getClassesByTeacher(Long teacherId);
    
    // Add student to class
    void addStudentToClass(Long classId, Long studentId);
    
    // Remove student from class
    void removeStudentFromClass(Long classId, Long studentId);
    
    // Check if user has access to a class
    boolean hasAccessToClass(Long classId, Long userId);
    
    // Get all students enrolled in a class
    List<ParticipantDTO> getClassStudents(Long classId);
    
    // Update class
    ClassDTO updateClass(Long classId, String name);
    
    // Delete class
    void deleteClass(Long classId);
    
    // Get all classes (for admin)
    List<ClassDTO> getAllClasses();
    
    List<ClassDTO> getClassesByStudent(Long studentId);
    
    // Get total number of students across all classes for a teacher
    int getTotalStudentsByTeacher(Long teacherId);

    
}