# Quiz Taking Implementation

## Overview
Implemented complete quiz-taking functionality for students, allowing them to attempt quizzes, answer questions, and submit their responses.

## New Files Created

### 1. src/pages/student/QuizAttempt.tsx
Complete quiz-taking interface with:
- Timer countdown
- Question display
- Answer input (MCQ, True/False, Short Answer)
- Progress tracking
- Auto-submit on timeout
- Manual submit with confirmation

## Features Implemented

### Quiz Attempt Page Features:

1. **Timer Functionality**
   - Countdown timer showing remaining time
   - Visual warning when less than 5 minutes remain
   - Auto-submit when time runs out
   - Timer displayed in MM:SS format

2. **Question Display**
   - Shows all questions in the quiz
   - Question number and marks displayed
   - Check mark indicator for answered questions
   - Support for three question types:
     - Multiple Choice (radio buttons)
     - True/False (radio buttons)
     - Short Answer (textarea)

3. **Answer Tracking**
   - Real-time tracking of answered vs unanswered questions
   - Progress indicator (X/Y questions answered)
   - Visual feedback for completed questions

4. **Submission**
   - Submit button always visible at bottom
   - Warning if submitting with unanswered questions
   - Confirmation dialog for incomplete submissions
   - Loading state during submission
   - Success/error feedback

5. **User Experience**
   - Sticky header with quiz info and timer
   - Fixed bottom bar with submit button
   - Responsive design
   - Clear visual hierarchy
   - Disabled back button during submission

## Routes Added

- `/student/quiz/:quizId` - Quiz attempt page

## API Endpoints Used

### Student Endpoints:
- `GET /quizzes/{quizId}` - Get quiz details with questions
- `POST /quizzes/{quizId}/start` - Start a new quiz attempt
- `POST /quizzes/attempts/{attemptId}/submit` - Submit quiz answers

## How It Works

### Student Workflow:

1. **View Available Quizzes**
   - Navigate to class detail page
   - See list of published quizzes
   - Check availability status and time window

2. **Start Quiz**
   - Click "Start Quiz" button
   - System creates a new quiz attempt
   - Timer starts automatically
   - Questions are displayed

3. **Answer Questions**
   - For MCQ: Select one option
   - For True/False: Select True or False
   - For Short Answer: Type answer in text area
   - Progress tracked in real-time

4. **Submit Quiz**
   - Click "Submit Quiz" button
   - Confirm if there are unanswered questions
   - System submits all answers
   - Auto-grades objective questions (MCQ, True/False)
   - Short answers marked for manual grading

5. **After Submission**
   - Redirected back to class page
   - Can view results (if enabled)
   - Attempt count updated

## Question Types Supported

### 1. Multiple Choice
- Radio button selection
- Only one answer can be selected
- Auto-graded based on correct option

### 2. True/False
- Two radio buttons (True/False)
- Auto-graded based on correct answer

### 3. Short Answer
- Text area for typing answer
- Requires manual grading by teacher
- Model answer available for teacher reference

## Validation & Error Handling

### Client-Side:
- Warns about unanswered questions before submit
- Prevents multiple submissions
- Handles timer expiration gracefully
- Shows loading states

### Server-Side:
- Validates quiz availability
- Checks attempt limits
- Verifies time window
- Validates student enrollment

## Auto-Grading Logic

### Objective Questions (MCQ, True/False):
- Automatically graded on submission
- Correct answers award full marks
- Incorrect answers award 0 marks
- Score calculated immediately

### Subjective Questions (Short Answer):
- Marked for manual grading
- Initially awarded 0 marks
- Teacher can grade later
- Score recalculated after grading

## Timer Behavior

- Starts when quiz attempt begins
- Counts down from quiz duration
- Shows warning at 5 minutes remaining
- Auto-submits when time reaches 0
- Cannot be paused or reset

## UI Components Used

- Card - Question containers
- Badge - Status indicators, timer, progress
- RadioGroup - MCQ and True/False options
- Textarea - Short answer input
- Button - Submit and navigation
- Alert - Time warning
- Toast - Success/error messages

## Testing Checklist

### Student Side:
- [ ] Click "Start Quiz" button
- [ ] Verify timer starts counting down
- [ ] Answer MCQ questions
- [ ] Answer True/False questions
- [ ] Answer Short Answer questions
- [ ] Check progress indicator updates
- [ ] Try submitting with unanswered questions
- [ ] Submit complete quiz
- [ ] Verify success message
- [ ] Check attempt count updated

### Edge Cases:
- [ ] Timer expires (auto-submit)
- [ ] Navigate away during quiz
- [ ] Submit with all questions unanswered
- [ ] Maximum attempts reached
- [ ] Quiz outside time window

## Future Enhancements

1. **Question Navigation**
   - Jump to specific question
   - Question palette/overview
   - Mark for review

2. **Save Progress**
   - Auto-save answers periodically
   - Resume incomplete attempts
   - Draft mode

3. **Enhanced UI**
   - Question bookmarking
   - Calculator tool
   - Scratch pad
   - Full-screen mode

4. **Results View**
   - View submitted answers
   - See correct answers (if enabled)
   - View explanations
   - Download results PDF

5. **Accessibility**
   - Keyboard navigation
   - Screen reader support
   - High contrast mode
   - Font size adjustment

## Notes

- Quiz must be PUBLISHED to be accessible
- Current time must be within start/end window
- Student must have attempts remaining
- Timer cannot be paused once started
- Answers are submitted all at once (not individually)
- Page refresh will lose unsaved answers
