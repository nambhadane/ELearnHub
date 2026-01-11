package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ClassEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Name cannot be null")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    // Read-only teacherId field mapped to the same column
    @Column(name = "teacher_id", insertable = false, updatable = false)
    private Long teacherId;
    
    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;
    
    // Read-only courseId field mapped to the same column
    @Column(name = "course_id", insertable = false, updatable = false)
    private Long courseId;
    
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
    
    @ManyToMany
    @JoinTable(
        name = "class_student",
        joinColumns = @JoinColumn(name = "class_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<User> students = new ArrayList<>();
    
    // Default no-arg constructor (required by JPA)
    public ClassEntity() {
        this.students = new ArrayList<>();
    }
    
    public ClassEntity(String name, User teacher, Course course) {
        this();
        this.name = name;
        this.teacher = teacher;
        this.course = course;
    }
    
    public void addStudent(User student) {
        if ("STUDENT".equals(student.getRole())) {
            this.students.add(student);
        } else {
            throw new IllegalArgumentException("Only students can be added to classes");
        }
    }
    
    public ClassEntity(Long id,
                      @NotNull(message = "Name cannot be null") 
                      @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters") 
                      String name,
                      Long teacherId, 
                      User teacher, 
                      Long courseId,
                      Course course, 
                      List<User> students) {
        this();
        this.id = id;
        this.name = name;
        this.teacherId = teacherId;
        this.teacher = teacher;
        this.courseId = courseId;
        this.course = course;
        this.students = students != null ? students : new ArrayList<>();
    }
    
    // Getters and Setters
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
    
    public Long getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
    
    public User getTeacher() {
        return teacher;
    }
    
    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }
    
    public Long getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    
    public Course getCourse() {
        return course;
    }
    
    public void setCourse(Course course) {
        this.course = course;
    }
    
    public List<User> getStudents() {
        return students;
    }
    
    public void setStudents(List<User> students) {
        this.students = students;
    }
}