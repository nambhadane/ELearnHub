package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.ClassDTO;
import com.elearnhub.teacher_service.dto.ParticipantDTO;
import com.elearnhub.teacher_service.dto.ConversationDTO;
import com.elearnhub.teacher_service.entity.ClassEntity;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.entity.Course;
//import com.elearnhub.teacher_service.repository.ClassRepository;
import com.elearnhub.teacher_service.repository.UserRepository;
import com.elearnhub.teacher_service.repository.ClassEntityRepository;
import com.elearnhub.teacher_service.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClassServiceImpl implements ClassService {

    @Autowired
    private ClassEntityRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired(required = false)
    private MessageService messageService;

    // Create a new class
    @Override
    @Transactional
    public ClassDTO createClass(Long teacherId, Long courseId, String name) {
        // Find teacher
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // Find course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Create class entity
        ClassEntity classEntity = new ClassEntity();
        classEntity.setName(name);
        classEntity.setTeacher(teacher);
        classEntity.setCourse(course);

        // Save class
        ClassEntity savedClass = classRepository.save(classEntity);

        // ✅ AUTO-CREATE: Create group conversation for this class
        try {
            if (messageService != null) {
                System.out.println("🔄 Attempting to create group conversation for class ID: " + savedClass.getId() + ", Name: " + savedClass.getName());
                ConversationDTO conversation = messageService.createClassConversation(savedClass.getId());
                if (conversation != null) {
                    System.out.println("✅ Group conversation created successfully! Conversation ID: " + conversation.getId() + ", Name: " + conversation.getName());
                } else {
                    System.err.println("❌ Group conversation creation returned null for class: " + savedClass.getName());
                }
            } else {
                System.err.println("⚠️ MessageService is NULL - group conversation not created for class: " + savedClass.getName());
            }
        } catch (Exception e) {
            // Log error but don't fail class creation
            System.err.println("❌ Failed to create group conversation for class " + savedClass.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        // Return DTO
        ClassDTO dto = new ClassDTO();
        dto.setId(savedClass.getId());
        dto.setName(savedClass.getName());
        dto.setTeacherId(teacherId);
        dto.setTeacherName(teacher.getName());
        dto.setCourseId(courseId);
        dto.setCourseName(course.getName());
        dto.setStudentCount(0);
        
        return dto;
    }

    // Get class by ID
    @Override
    @Transactional(readOnly = true)
    public Optional<ClassEntity> getClassById(Long classId) {
        // Use the method that eagerly loads teacher, course, and students
        return classRepository.findByIdWithAllRelationships(classId);
    }

    // Get classes by teacher
    @Override
    @Transactional(readOnly = true)
    public List<ClassDTO> getClassesByTeacher(Long teacherId) {
        List<ClassEntity> classes = classRepository.findByTeacherId(teacherId);
        
        return classes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Add student to class
    @Override
    @Transactional
    public void addStudentToClass(Long classId, Long studentId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Check if student is already enrolled
        if (classEntity.getStudents() == null) {
            classEntity.setStudents(new ArrayList<>());
        }

        boolean alreadyEnrolled = classEntity.getStudents().stream()
                .anyMatch(s -> s.getId().equals(studentId));

        if (alreadyEnrolled) {
            throw new RuntimeException("Student is already enrolled in this class");
        }

        // Add student to class
        classEntity.getStudents().add(student);
        classRepository.save(classEntity);
        
        // ✅ AUTO-ADD: Add student to class group conversation
        try {
            if (messageService != null) {
                ConversationDTO classConversation = messageService.getClassConversation(classId);
                if (classConversation != null) {
                    messageService.addParticipantToConversation(classConversation.getId(), studentId);
                    System.out.println("✅ Student " + student.getName() + " added to class conversation");
                } else {
                    System.err.println("⚠️ Class conversation not found for classId: " + classId);
                }
            } else {
                System.err.println("⚠️ MessageService not available - student not added to conversation");
            }
        } catch (Exception e) {
            // Log error but don't fail student addition
            System.err.println("❌ Failed to add student to class conversation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Remove student from class
    @Override
    @Transactional
    public void removeStudentFromClass(Long classId, Long studentId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (classEntity.getStudents() != null) {
            boolean removed = classEntity.getStudents().removeIf(s -> s.getId().equals(studentId));
            if (!removed) {
                throw new RuntimeException("Student is not enrolled in this class");
            }
            classRepository.save(classEntity);
        } else {
            throw new RuntimeException("Student is not enrolled in this class");
        }
    }

    // Check if user has access to a class
    @Override
    @Transactional(readOnly = true)
    public boolean hasAccessToClass(Long classId, Long userId) {
        Optional<ClassEntity> classOpt = classRepository.findById(classId);
        if (classOpt.isEmpty()) {
            return false;
        }

        ClassEntity classEntity = classOpt.get();

        // Check if user is the teacher
        if (classEntity.getTeacher() != null && 
            classEntity.getTeacher().getId().equals(userId)) {
            return true;
        }

        // Check if user is an enrolled student
        if (classEntity.getStudents() != null) {
            return classEntity.getStudents().stream()
                    .anyMatch(student -> student.getId().equals(userId));
        }

        return false;
    }

    // Get all students enrolled in a class
    @Override
    @Transactional(readOnly = true)
    public List<ParticipantDTO> getClassStudents(Long classId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (classEntity.getStudents() == null || classEntity.getStudents().isEmpty()) {
            return new ArrayList<>();
        }

        return classEntity.getStudents().stream()
                .map(student -> {
                    ParticipantDTO dto = new ParticipantDTO();
                    dto.setId(student.getId());
                    dto.setName(student.getName());
                    dto.setUsername(student.getUsername());
                    dto.setRole(student.getRole());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Update class
    @Override
    @Transactional
    public ClassDTO updateClass(Long classId, String name) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        classEntity.setName(name);
        ClassEntity savedClass = classRepository.save(classEntity);

        return convertToDTO(savedClass);
    }

    // Delete class
    @Override
    @Transactional
    public void deleteClass(Long classId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        
        classRepository.delete(classEntity);
    }

    // Get all classes (for admin)
    @Override
    @Transactional(readOnly = true)
    public List<ClassDTO> getAllClasses() {
        List<ClassEntity> classes = classRepository.findAll();
        
        return classes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Get classes for student
    @Override
    @Transactional(readOnly = true)
    public List<ClassDTO> getClassesByStudent(Long studentId) {
        List<ClassEntity> classes = classRepository.findClassesByStudentId(studentId);
        
        return classes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Helper method to convert ClassEntity to ClassDTO
    private ClassDTO convertToDTO(ClassEntity classEntity) {
        ClassDTO dto = new ClassDTO();
        dto.setId(classEntity.getId());
        dto.setName(classEntity.getName());
        
        if (classEntity.getTeacher() != null) {
            dto.setTeacherId(classEntity.getTeacher().getId());
            dto.setTeacherName(classEntity.getTeacher().getName());
        }
        
        if (classEntity.getCourse() != null) {
            dto.setCourseId(classEntity.getCourse().getId());
            dto.setCourseName(classEntity.getCourse().getName());
        }
        
        dto.setStudentCount(classEntity.getStudents() != null ? classEntity.getStudents().size() : 0);
        
        return dto;
    }
    
    // Get total number of students across all classes for a teacher
    @Override
    @Transactional(readOnly = true)
    public int getTotalStudentsByTeacher(Long teacherId) {
        try {
            System.out.println("🔍 ClassServiceImpl: Getting total students for teacher ID: " + teacherId);
            
            List<ClassEntity> teacherClasses = classRepository.findByTeacherId(teacherId);
            System.out.println("✅ ClassServiceImpl: Found " + teacherClasses.size() + " classes for teacher");
            
            // Use a Set to avoid counting the same student multiple times
            // if they are enrolled in multiple classes by the same teacher
            int totalStudents = teacherClasses.stream()
                    .flatMap(classEntity -> {
                        if (classEntity.getStudents() != null) {
                            System.out.println("📚 Class '" + classEntity.getName() + "' has " + classEntity.getStudents().size() + " students");
                            return classEntity.getStudents().stream();
                        }
                        System.out.println("📚 Class '" + classEntity.getName() + "' has no students");
                        return java.util.stream.Stream.empty();
                    })
                    .collect(Collectors.toSet()) // Remove duplicates
                    .size();
            
            System.out.println("✅ ClassServiceImpl: Total unique students: " + totalStudents);
            return totalStudents;
        } catch (Exception e) {
            System.err.println("❌ ClassServiceImpl: Error in getTotalStudentsByTeacher: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}