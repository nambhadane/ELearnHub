# Assignment Feature Implementation Guide

## Quick Start

### Step 1: Fix Critical Bugs First ⚠️
Read `CRITICAL_BACKEND_BUGS.md` and fix:
1. `getAssignmentsByClass()` bug (using classId as courseId)
2. Duplicate constructors in Assignment entity
3. Missing ClassService dependency

### Step 2: Database Migration
Run this SQL to add new columns:

```sql
ALTER TABLE assignment 
ADD COLUMN weight DOUBLE,
ADD COLUMN allow_late_submission BOOLEAN DEFAULT FALSE,
ADD COLUMN late_penalty DOUBLE,
ADD COLUMN additional_instructions VARCHAR(1000),
ADD COLUMN status VARCHAR(20) DEFAULT 'published';
```

### Step 3: Update Backend Code
Follow `BACKEND_ASSIGNMENT_CHANGES.md` to:
1. Update `AssignmentDTO` with new fields
2. Update `Assignment` entity with new fields
3. Update `AssignmentService` methods
4. Fix `getAssignmentsByClass()` endpoint

### Step 4: Frontend Will Be Updated
Once backend is ready, the frontend will:
- Fetch classes for the dropdown
- Combine date + time into LocalDateTime
- Extract courseId from selected class
- Send all fields to backend
- Handle "Save as Draft" vs "Create Assignment"

---

## Field Mapping (Frontend → Backend)

| Frontend Field | Backend Field | Type | Required | Notes |
|---------------|---------------|------|----------|-------|
| Select Class | courseId | Long | ✅ | Frontend extracts from class |
| Assignment Title | title | String | ✅ | |
| Description | description | String | ✅ | |
| Due Date + Due Time | dueDate | LocalDateTime | ✅ | Frontend combines |
| Total Points | maxGrade | Double | ✅ | |
| Weight (%) | weight | Double | ❌ | Optional |
| Allow Late Submission | allowLateSubmission | Boolean | ❌ | Default: false |
| Late Penalty (%) | latePenalty | Double | ❌ | Only if allowLateSubmission=true |
| Additional Instructions | additionalInstructions | String | ❌ | Optional |
| Create Assignment | status | String | ❌ | "published" |
| Save as Draft | status | String | ❌ | "draft" |

---

## API Endpoints Summary

### Required Endpoints (Already Exist, Need Updates):
- ✅ `POST /assignments` - Create assignment (needs new fields)
- ✅ `GET /assignments/class/{classId}` - Get assignments by class (needs bug fix)
- ✅ `DELETE /assignments/{id}` - Delete assignment (works as-is)

### Recommended New Endpoints:
- ⭐ `GET /assignments/{assignmentId}` - Get single assignment
- ⭐ `PUT /assignments/{assignmentId}` - Update assignment

---

## Testing Checklist

After backend changes:

1. ✅ Create assignment with all fields
2. ✅ Create assignment with only required fields
3. ✅ Create assignment as "draft"
4. ✅ Create assignment with late submission enabled
5. ✅ Get assignments by class ID
6. ✅ Verify assignments are linked to correct course
7. ✅ Delete assignment
8. ✅ Verify validation errors for missing required fields

---

## Next Steps

1. **Backend Team**: Implement changes in `BACKEND_ASSIGNMENT_CHANGES.md`
2. **Frontend Team**: Will update CreateAssignment.tsx once backend is ready
3. **Testing**: Test all scenarios in checklist above

---

## Questions?

- See `BACKEND_ASSIGNMENT_CHANGES.md` for detailed implementation
- See `CRITICAL_BACKEND_BUGS.md` for bugs that must be fixed first

