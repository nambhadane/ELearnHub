# 🔴 URGENT: Backend Fix Required

## Error
```
No default constructor for entity 'com.elearnhub.teacher_service.entity.ClassEntity'
```

## Problem
Your `ClassEntity` class needs an explicit no-argument constructor for Hibernate/JPA to work properly.

## Solution

### Update your ClassEntity.java file:

Add this explicit no-args constructor:

```java
@Entity
@Data
public class ClassEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Name cannot be null")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

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

    // ✅ ADD THIS: Explicit no-argument constructor (REQUIRED)
    public ClassEntity() {
        this.students = new ArrayList<>();
    }

    // Your existing constructor with parameters
    public ClassEntity(String name, User teacher, Course course) {
        this.name = name;
        this.teacher = teacher;
        this.course = course;
        this.students = new ArrayList<>();
    }

    // ... rest of your getters and setters
}
```

## Why This Happens
Hibernate uses reflection to create proxy objects and needs an explicit no-argument constructor. Even with Lombok's `@NoArgsConstructor`, sometimes JPA still requires it to be explicit.

## After Fixing
1. Restart your Spring Boot application
2. Try fetching classes again
3. The error should be resolved

## Quick Test
After fixing, test with:
```bash
curl -X GET http://localhost:8082/classes/teacher/11 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

This should return an empty array `[]` if no classes exist, or a list of classes if they do.

