# Grading Null Pointer Exception Fix - COMPLETE

## Problem
Teachers were getting an error when trying to grade submissions:
```
Failed to grade submission: Cannot invoke "Object.toString()" because the return value of "java.util.Map.get(Object)" is null
```

## Root Cause Analysis

### 1. **Null Pointer Exception**
The backend was calling `.toString()` on a null value:
```java
// BEFORE (Problematic)
Double score = Double.valueOf(gradeData.get("score").toString());
```

If `gradeData.get("score")` returned `null`, calling `.toString()` would throw a NullPointerException.

### 2. **Field Name Mismatch**
**Frontend sends**: `{ grade: 85.5, feedback: "Good work!" }`
**Backend expected**: `{ score: 85.5, feedback: "Good work!" }`

The frontend was sending `grade` but the backend was looking for `score`.

## Solution Applied

### 1. **Added Null Safety**
```java
// AFTER (Safe)
Object scoreObj = gradeData.get("score");
if (scoreObj == null) {
    scoreObj = gradeData.get("grade"); // Try "grade" field as fallback
}
if (scoreObj == null) {
    throw new RuntimeException("Score/grade is required");
}

Double score;
try {
    score = Double.valueOf(scoreObj.toString());
} catch (NumberFormatException e) {
    throw new RuntimeException("Invalid score format: " + scoreObj);
}
```

### 2. **Enhanced Validation**
- **Request validation**: Check if gradeData is null or empty
- **Field validation**: Check both "score" and "grade" field names
- **Type validation**: Proper number format validation with error handling
- **Feedback handling**: Safe extraction of optional feedback field

### 3. **Improved Error Messages**
- Clear error messages for missing data
- Specific error for invalid number formats
- Better debugging information

## API Usage

### Frontend Request Format (Both supported):
```javascript
// Option 1: Using "grade" field (current frontend)
{ grade: 85.5, feedback: "Good work!" }

// Option 2: Using "score" field (also supported)
{ score: 85.5, feedback: "Good work!" }
```

### Backend Processing:
1. Validates request data exists
2. Looks for "score" field first, then "grade" field
3. Validates the value is a valid number
4. Extracts optional feedback
5. Calls grading service
6. Returns updated submission

## Error Handling

### Validation Errors:
- **"Grade data is required"** - Empty request body
- **"Score/grade is required"** - Missing both score and grade fields
- **"Invalid score format: X"** - Non-numeric value provided

### Service Errors:
- **"Submission not found with id: X"** - Invalid submission ID
- **Database errors** - Connection or constraint issues

## Testing Scenarios

### ✅ Valid Requests:
```json
{ "grade": 85.5, "feedback": "Excellent work!" }
{ "score": 92.0, "feedback": "Well done!" }
{ "grade": 78.5 }  // No feedback
```

### ❌ Invalid Requests:
```json
{}  // Empty object
{ "feedback": "Good" }  // Missing grade/score
{ "grade": "invalid" }  // Non-numeric grade
null  // Null request body
```

## Files Modified
- `src/main/java/com/elearnhub/teacher_service/Controller/AssignmentController.java`

## Status: ✅ GRADING ERROR FIXED
Teachers can now grade submissions without encountering null pointer exceptions. The system handles both "grade" and "score" field names and provides clear error messages for invalid data.