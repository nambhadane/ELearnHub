# Backend Fix Required: ClassEntity Constructor Issue

## Problem
When fetching classes, you're getting this error:
```
No default constructor for entity 'com.elearnhub.teacher_service.entity.ClassEntity'
```

## Root Cause
JPA/Hibernate requires a no-argument (default) constructor for all entity classes. Even though you have `@NoArgsConstructor` from Lombok, there might be an issue.

## Solution

### Option 1: Ensure Lombok @NoArgsConstructor is working (Recommended)

Make sure your `ClassEntity` has the Lombok annotation and it's properly configured:

```java
@Entity
@Data
@NoArgsConstructor  // ✅ This should create a no-args constructor
public class ClassEntity {
    // ... your fields
}
```

**If this doesn't work**, add an explicit constructor:

### Option 2: Add Explicit No-Args Constructor

Add this to your `ClassEntity` class:

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

    // ✅ ADD THIS: Explicit no-args constructor
    public ClassEntity() {
        this.students = new ArrayList<>();
    }

    // Your existing constructor
    public ClassEntity(String name, User teacher, Course course) {
        this.name = name;
        this.teacher = teacher;
        this.course = course;
        this.students = new ArrayList<>();
    }

    // ... rest of your methods
}
```

### Option 3: Check Lombok Configuration

Make sure Lombok is properly configured in your `pom.xml` or `build.gradle`:

**For Maven (pom.xml)**:
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

**For Gradle (build.gradle)**:
```gradle
dependencies {
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

## Additional Check

Also verify that your `ClassService.getClassesByTeacher()` method is correctly implemented and returns ClassDTO objects (not ClassEntity directly if that's causing issues).

## Test After Fix

1. Restart your Spring Boot application
2. Try fetching classes again
3. The error should be resolved

