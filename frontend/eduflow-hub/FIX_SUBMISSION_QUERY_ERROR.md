# Fix for "Query did not return a unique result: 4 results were returned"

## Problem

The error occurs in `AssignmentService.getSubmissionByStudentAndAssignment()` because there are **duplicate submissions** in the database for the same student and assignment. The repository method `findByAssignmentIdAndStudentId()` expects a unique result but finds 4 submissions.

## Solution

Update the `getSubmissionByStudentAndAssignment()` method in `AssignmentService.java` to handle multiple results by selecting the most recent submission:

### Updated AssignmentService.java Method

Replace the existing `getSubmissionByStudentAndAssignment` method with this:

```java
public SubmissionDTO getSubmissionByStudentAndAssignment(Long studentId, Long assignmentId) {
    // ✅ FIX: Handle multiple submissions by getting the most recent one
    List<Submission> submissions = submissionRepository.findByAssignmentIdAndStudentId(
            assignmentId, studentId);
    
    if (submissions == null || submissions.isEmpty()) {
        return null;
    }
    
    // If multiple submissions exist, get the most recent one (by submittedAt)
    Submission submission = submissions.stream()
            .filter(s -> s.getSubmittedAt() != null)
            .max(Comparator.comparing(Submission::getSubmittedAt))
            .orElse(submissions.get(0)); // Fallback to first if no dates
    
    return convertSubmissionToDTO(submission);
}
```

### Required Imports

Add these imports to `AssignmentService.java`:

```java
import java.util.Comparator;
import java.util.List;
```

### Alternative: Update Repository Method

If you want to fix it at the repository level, update `SubmissionRepository.java`:

```java
// Change from:
Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

// To:
List<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
```

Then update the service method as shown above.

### Long-term Fix: Add Unique Constraint

To prevent duplicate submissions in the future, add a unique constraint to your `Submission` entity or database:

```java
@Table(
    name = "submission",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"assignment_id", "student_id"})
    }
)
public class Submission {
    // ... existing code
}
```

**Note:** If you add a unique constraint, you'll need to clean up existing duplicates first, or handle the constraint violation when saving.

---

## Quick Fix Summary

1. **Change repository method** from `Optional<Submission>` to `List<Submission>`
2. **Update service method** to select the most recent submission from the list
3. **Add imports** for `List` and `Comparator`

This will fix the immediate error and allow the `/assignments/my-assignments` endpoint to work correctly.

