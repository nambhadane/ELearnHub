// API configuration
// In development, use proxy path to avoid CORS issues
// In production, use full backend URL
const API_BASE_URL = import.meta.env.DEV 
  ? '/api'  // Use proxy in development
  : import.meta.env.VITE_API_BASE_URL || 'http://localhost:8082';  // Use env variable or default in production

// Types for API requests and responses
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  role: string;
}

export interface RegisterRequest {
  username: string;
  name: string;
  password: string;
  email: string;
  role?: string;
}

export interface RegisterResponse {
  message: string;
  emailVerificationRequired?: boolean;
  emailSent?: boolean;
  user?: {
    id: number;
    username: string;
    name: string;
    email: string;
    role: string;
    emailVerified: boolean;
  };
}

export interface ApiError {
  message: string;
}

// Helper function to get auth token
function getAuthToken(): string | null {
  return localStorage.getItem("authToken");
}

// Helper function to get auth headers
function getAuthHeaders(): HeadersInit {
  const token = getAuthToken();
  const headers: HeadersInit = {
    "Content-Type": "application/json",
  };
  
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  
  return headers;
}

// Helper function to handle API errors
async function handleApiError(response: Response): Promise<never> {
  let errorMessage = 'An error occurred';
  let errorData: any = null;
  
  try {
    errorData = await response.json();
    errorMessage = errorData.message || errorMessage;
  } catch {
    errorMessage = `HTTP ${response.status}: ${response.statusText}`;
  }
  
  // Create enhanced error with additional data for email verification
  const error = new Error(errorMessage) as any;
  if (errorData) {
    error.data = errorData;
    error.status = response.status;
  }
  
  throw error;
}

// Login API call
export async function loginApi(request: LoginRequest): Promise<LoginResponse> {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Register API call
export async function registerApi(request: RegisterRequest): Promise<RegisterResponse> {
  const response = await fetch(`${API_BASE_URL}/auth/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// ============= CLASS API =============

export interface ClassDTO {
  id: number;
  name: string;
  teacherId: number;
  courseId: number;
}

export interface CreateClassRequest {
  courseId: number;
  name: string;
}

// Create a new class
export async function createClass(request: CreateClassRequest): Promise<ClassDTO> {
  const params = new URLSearchParams({
    courseId: request.courseId.toString(),
    name: request.name,
  });

  const response = await fetch(`${API_BASE_URL}/classes?${params.toString()}`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get classes by teacher ID
export async function getClassesByTeacher(teacherId: number): Promise<ClassDTO[]> {
  const response = await fetch(`${API_BASE_URL}/classes/teacher/${teacherId}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get currently authenticated student's classes
export async function getMyClasses(): Promise<ClassDTO[]> {
  const response = await fetch(`${API_BASE_URL}/student/classes`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get classes by student ID (fallback endpoint)
export async function getClassesByStudent(studentId: number): Promise<ClassDTO[]> {
  const response = await fetch(`${API_BASE_URL}/classes/student/${studentId}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get class by ID
export async function getClassById(classId: number): Promise<ClassDTO> {
  const response = await fetch(`${API_BASE_URL}/classes/${classId}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get current user info (profile)
export interface UserProfile {
  id: number;
  username: string;
  name?: string;
  email: string;
  role: string;
  phone?: string;
  location?: string;
  joinDate?: string;
  // Student specific
  studentId?: string;
  major?: string;
  year?: string;
  expectedGraduation?: string;
  gpa?: number;
  creditsCompleted?: number;
  totalCredits?: number;
  attendanceRate?: number;
  // Teacher specific
  department?: string;
  specialization?: string;
  yearsOfExperience?: number;
  totalClasses?: number;
  totalStudents?: number;
  // Admin specific
  adminLevel?: string;
  permissions?: string[];
}

// Get teacher profile
export async function getTeacherProfile(): Promise<UserProfile> {
  const response = await fetch(`${API_BASE_URL}/teacher/profile`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  const data = await response.json();
  // ✅ Map backend field names to frontend interface
  return {
    ...data,
    phone: data.phone || data.phoneNumber || null,
    location: data.location || data.address || null,
  };
}

// Get student profile
export async function getStudentProfile(): Promise<UserProfile> {
  const response = await fetch(`${API_BASE_URL}/student/profile`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  const data = await response.json();
  // ✅ Map backend field names to frontend interface
  return {
    ...data,
    phone: data.phone || data.phoneNumber || null,
    location: data.location || data.address || null,
  };
}

// Get admin profile
export async function getAdminProfile(): Promise<UserProfile> {
  const response = await fetch(`${API_BASE_URL}/admin/profile`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  const data = await response.json();
  // ✅ Map backend field names to frontend interface
  return {
    ...data,
    phone: data.phone || data.phoneNumber || null,
    location: data.location || data.address || null,
  };
}

// Generic get profile function (tries role-specific endpoints, falls back to teacher)
export async function getUserProfile(role?: string): Promise<UserProfile> {
  const userRole = role || localStorage.getItem("authRole")?.toUpperCase();
  
  try {
    if (userRole === "STUDENT") {
      return await getStudentProfile();
    } else if (userRole === "ADMIN") {
      return await getAdminProfile();
    } else {
      // Default to teacher or try teacher endpoint
      return await getTeacherProfile();
    }
  } catch (err) {
    // If role-specific endpoint fails, try teacher endpoint as fallback
    if (userRole !== "TEACHER") {
      try {
        return await getTeacherProfile();
      } catch {
        throw err;
      }
    }
    throw err;
  }
}

// Update profile interfaces
export interface UpdateProfileRequest {
  name?: string;
  email?: string;
  phone?: string;
  phoneNumber?: string; // Backend accepts both
  location?: string;
  address?: string; // Backend accepts both
}

// Update teacher profile
export async function updateTeacherProfile(request: UpdateProfileRequest): Promise<UserProfile> {
  // Map frontend fields to backend fields
  const backendRequest: any = {};
  if (request.name !== undefined) backendRequest.name = request.name;
  if (request.email !== undefined) backendRequest.email = request.email;
  if (request.phone !== undefined) backendRequest.phoneNumber = request.phone;
  if (request.location !== undefined) backendRequest.address = request.location;

  const response = await fetch(`${API_BASE_URL}/teacher/profile`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify(backendRequest),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  const data = await response.json();
  return {
    ...data,
    phone: data.phone || data.phoneNumber || null,
    location: data.location || data.address || null,
  };
}

// Update student profile
export async function updateStudentProfile(request: UpdateProfileRequest): Promise<UserProfile> {
  // Map frontend fields to backend fields
  const backendRequest: any = {};
  if (request.name !== undefined) backendRequest.name = request.name;
  if (request.email !== undefined) backendRequest.email = request.email;
  if (request.phone !== undefined) backendRequest.phoneNumber = request.phone;
  if (request.location !== undefined) backendRequest.address = request.location;

  const response = await fetch(`${API_BASE_URL}/student/profile`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify(backendRequest),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  const data = await response.json();
  return {
    ...data,
    phone: data.phone || data.phoneNumber || null,
    location: data.location || data.address || null,
  };
}

// Update admin profile
export async function updateAdminProfile(request: UpdateProfileRequest): Promise<UserProfile> {
  // Map frontend fields to backend fields
  const backendRequest: any = {};
  if (request.name !== undefined) backendRequest.name = request.name;
  if (request.email !== undefined) backendRequest.email = request.email;
  if (request.phone !== undefined) backendRequest.phoneNumber = request.phone;
  if (request.location !== undefined) backendRequest.address = request.location;

  const response = await fetch(`${API_BASE_URL}/admin/profile`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify(backendRequest),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  const data = await response.json();
  return {
    ...data,
    phone: data.phone || data.phoneNumber || null,
    location: data.location || data.address || null,
  };
}

// Generic update profile function
export async function updateUserProfile(request: UpdateProfileRequest, role?: string): Promise<UserProfile> {
  const userRole = role || localStorage.getItem("authRole")?.toUpperCase();
  
  if (userRole === "STUDENT") {
    return await updateStudentProfile(request);
  } else if (userRole === "ADMIN") {
    return await updateAdminProfile(request);
  } else {
    return await updateTeacherProfile(request);
  }
}

// ============= COURSE API =============

export interface Course {
  id: number;
  name: string;
  subject?: string;
  description?: string;
  teacherId: number;
  students?: number;
}

export interface CreateCourseRequest {
  name: string;
  subject?: string;
  description?: string;
}

export interface CreateCourseResponse {
  message: string;
  course: Course;
}

// Get all courses for authenticated teacher
export async function getCourses(): Promise<Course[]> {
  console.log('Fetching courses from:', `${API_BASE_URL}/courses`);
  const response = await fetch(`${API_BASE_URL}/courses`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  const courses = await response.json();
  console.log('Courses API response:', courses);
  console.log('Number of courses received:', Array.isArray(courses) ? courses.length : 'Not an array');
  return courses;
}

// Create a new course
export async function createCourse(request: CreateCourseRequest): Promise<CreateCourseResponse> {
  const response = await fetch(`${API_BASE_URL}/courses`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get course by ID
export async function getCourseById(courseId: number): Promise<Course> {
  const response = await fetch(`${API_BASE_URL}/courses/${courseId}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Update course
export async function updateCourse(courseId: number, request: Partial<CreateCourseRequest>): Promise<Course> {
  const response = await fetch(`${API_BASE_URL}/courses/${courseId}`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Delete course
export async function deleteCourse(courseId: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/courses/${courseId}`, {
    method: 'DELETE',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }
}

// Add student to class
export async function addStudentToClass(classId: number, studentId: number): Promise<void> {
  const params = new URLSearchParams({
    studentId: studentId.toString(),
  });

  const response = await fetch(`${API_BASE_URL}/classes/${classId}/students?${params.toString()}`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }
}

// ============= ASSIGNMENT API =============

export interface AssignmentDTO {
  id?: number;
  title: string;
  description: string;
  dueDate: string; // ISO 8601 format (LocalDateTime from backend)
  maxGrade: number;
  courseId?: number;
  classId?: number; // For frontend to send classId
  weight?: number;
  allowLateSubmission?: boolean;
  latePenalty?: number;
  additionalInstructions?: string;
  status?: string; // "draft" or "published"
}

// ============= SUBMISSION API TYPES =============

export interface SubmissionDTO {
  id?: number;
  assignmentId: number;
  studentId?: number;
  studentName?: string;
  content?: string;
  filePath?: string;
  submittedAt?: string; // ISO 8601 format
  grade?: number;
  feedback?: string;
}

export interface CreateAssignmentRequest {
  classId: number; // Frontend sends classId
  title: string;
  description: string;
  dueDate: string; // ISO 8601 format
  maxGrade: number;
  weight?: number;
  allowLateSubmission?: boolean;
  latePenalty?: number;
  additionalInstructions?: string;
  status?: string;
}

// Create a new assignment
export async function createAssignment(request: CreateAssignmentRequest): Promise<AssignmentDTO> {
  const response = await fetch(`${API_BASE_URL}/assignments`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get assignments by class ID
export async function getAssignmentsByClass(classId: number): Promise<AssignmentDTO[]> {
  const response = await fetch(`${API_BASE_URL}/assignments/class/${classId}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get all assignments for the authenticated student with submission status
export interface StudentAssignmentDTO {
  id: number;
  title: string;
  description: string;
  dueDate: string;
  maxGrade: number;
  courseId: number;
  className: string;
  status: "pending" | "submitted" | "graded";
  submissionId?: number;
  submittedAt?: string;
  grade?: number;
  feedback?: string;
}

export async function getMyAssignments(): Promise<StudentAssignmentDTO[]> {
  const response = await fetch(`${API_BASE_URL}/assignments/my-assignments`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  const data = await response.json();
  console.log("getMyAssignments response:", data);
  
  // Handle both array and object responses, and convert dueDate if needed
  if (Array.isArray(data)) {
    return data.map((item: any) => ({
      ...item,
      dueDate: item.dueDate ? (typeof item.dueDate === 'string' ? item.dueDate : String(item.dueDate)) : '',
    }));
  }
  
  return [];
}

// Get assignment by ID
export async function getAssignmentById(assignmentId: number): Promise<AssignmentDTO> {
  const response = await fetch(`${API_BASE_URL}/assignments/${assignmentId}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get current student's submission for a specific assignment
export async function getMySubmission(assignmentId: number): Promise<SubmissionDTO | null> {
  const response = await fetch(`${API_BASE_URL}/assignments/${assignmentId}/submission/me`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (response.status === 404) {
    // No submission yet
    return null;
  }

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Submit or resubmit an assignment (supports text content + optional files)
export async function submitAssignment(
  assignmentId: number,
  content: string,
  files: File[]
): Promise<SubmissionDTO> {
  const formData = new FormData();
  formData.append('assignmentId', assignmentId.toString());
  if (content && content.trim().length > 0) {
    formData.append('content', content);
  }
  files.forEach((file) => {
    formData.append('files', file);
  });

  // For multipart, don't set Content-Type manually so the browser can add the boundary
  const token = localStorage.getItem('authToken');
  const headers: HeadersInit = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}/assignments/submissions`, {
    method: 'POST',
    headers,
    body: formData,
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Delete assignment
export async function deleteAssignment(assignmentId: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/assignments/${assignmentId}`, {
    method: 'DELETE',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }
}

// Download submission file
export async function downloadSubmissionFile(submissionId: number, filename?: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/assignments/submissions/${submissionId}/file`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
    return;
  }

  // Get filename from Content-Disposition header or use provided/default
  let downloadFilename = filename || 'submission';
  const contentDisposition = response.headers.get('Content-Disposition');
  if (contentDisposition) {
    const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
    if (filenameMatch && filenameMatch[1]) {
      downloadFilename = filenameMatch[1].replace(/['"]/g, '');
    }
  }

  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = downloadFilename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

// ============= MESSAGING API =============

export interface ParticipantDTO {
  id: number;
  name?: string;
  username: string;
  role?: string;
  avatar?: string;
}

export interface MessageAttachment {
  id?: number;
  name: string;
  url: string;
  type?: string;
  size?: number;
}

export interface MessageDTO {
  id: number;
  conversationId: number;
  senderId: number;
  senderName: string;
  content: string;
  createdAt: string; // ISO 8601 format
  isRead?: boolean;
  attachments?: MessageAttachment[];
}

export interface ConversationDTO {
  id: number;
  type: "DIRECT" | "GROUP";
  name?: string;
  classId?: number;
  participants?: ParticipantDTO[];
  lastMessage?: MessageDTO;
  createdAt: string;
  updatedAt?: string;
  unreadCount?: number;
}

export interface CreateMessageRequest {
  conversationId: number;
  content: string;
  files?: File[];
}

export interface CreateDirectConversationRequest {
  participantId: number;
}

// Get all conversations for current user
export async function getConversations(): Promise<ConversationDTO[]> {
  const response = await fetch(`${API_BASE_URL}/messages/conversations`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get messages for a conversation
export async function getMessages(conversationId: number, page: number = 0, size: number = 50): Promise<MessageDTO[]> {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
  });

  const response = await fetch(`${API_BASE_URL}/messages/conversations/${conversationId}/messages?${params.toString()}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Send a message (with optional file attachments)
export async function sendMessage(request: CreateMessageRequest): Promise<MessageDTO> {
  const formData = new FormData();
  formData.append('conversationId', request.conversationId.toString());
  formData.append('content', request.content || '');
  
  // Add files if provided
  if (request.files && request.files.length > 0) {
    request.files.forEach((file) => {
      formData.append('files', file);
    });
  }

  const token = localStorage.getItem('authToken');
  const headers: HeadersInit = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  // Don't set Content-Type for FormData - browser will set it with boundary

  const response = await fetch(`${API_BASE_URL}/messages`, {
    method: 'POST',
    headers: headers,
    body: formData,
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Create direct conversation (1-on-1)
export async function createDirectConversation(participantId: number): Promise<ConversationDTO> {
  const response = await fetch(`${API_BASE_URL}/messages/conversations/direct?userId=${participantId}`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get class group conversation
export async function getClassConversation(classId: number): Promise<ConversationDTO> {
  const response = await fetch(`${API_BASE_URL}/messages/conversations/class/${classId}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get students in a class (for teacher to start direct chats)
export async function getClassStudents(classId: number): Promise<ParticipantDTO[]> {
  const response = await fetch(`${API_BASE_URL}/classes/${classId}/students`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// ============= TEACHER SUBMISSION MANAGEMENT =============

// Get all submissions for a specific assignment (teacher view)
export async function getSubmissionsByAssignment(assignmentId: number): Promise<SubmissionDTO[]> {
  const response = await fetch(`${API_BASE_URL}/assignments/${assignmentId}/submissions`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Grade a submission
export async function gradeSubmission(
  submissionId: number,
  grade: number,
  feedback?: string
): Promise<SubmissionDTO> {
  const response = await fetch(`${API_BASE_URL}/assignments/submissions/${submissionId}/grade`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify({ grade, feedback }),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get all teachers (for teacher-to-teacher chats)
export async function getAllTeachers(): Promise<ParticipantDTO[]> {
  // Try multiple possible endpoints
  const endpoints = [
    `${API_BASE_URL}/user/teachers`,
    `${API_BASE_URL}/teachers`,
    `${API_BASE_URL}/messages/teachers`,
    `${API_BASE_URL}/teacher/teachers`,
  ];

  for (const endpoint of endpoints) {
    try {
      const response = await fetch(endpoint, {
        method: 'GET',
        headers: getAuthHeaders(),
      });

      if (response.ok) {
        return await response.json();
      }
      
      // If 404, try next endpoint
      if (response.status === 404 && endpoint !== endpoints[endpoints.length - 1]) {
        continue;
      }
      
      // For other errors, throw
      await handleApiError(response);
    } catch (err) {
      // If last endpoint, throw the error
      if (endpoint === endpoints[endpoints.length - 1]) {
        throw err;
      }
      // Otherwise, try next endpoint
      continue;
    }
  }

  // If all endpoints failed, return empty array
  return [];
}

// Download message attachment with authentication
export async function downloadMessageFile(filename: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/messages/files/${filename}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
    },
  });

  if (!response.ok) {
    await handleApiError(response);
    return;
  }

  // Get filename from Content-Disposition header or use provided
  let downloadFilename = filename;
  const contentDisposition = response.headers.get('Content-Disposition');
  if (contentDisposition) {
    const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
    if (filenameMatch && filenameMatch[1]) {
      downloadFilename = filenameMatch[1].replace(/['"]/g, '');
    }
  }

  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = downloadFilename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

// Get authenticated image URL as blob
export async function getAuthenticatedImageUrl(filename: string): Promise<string> {
  const response = await fetch(`${API_BASE_URL}/messages/files/${filename}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
    },
  });

  if (!response.ok) {
    throw new Error('Failed to load image');
  }

  const blob = await response.blob();
  return window.URL.createObjectURL(blob);
}

// Get all students (for teacher to add to classes)
export async function getAllStudents(): Promise<ParticipantDTO[]> {
  // Try multiple possible endpoints
  const endpoints = [
    `${API_BASE_URL}/user/students`,
    `${API_BASE_URL}/students`,
  ];

  for (const endpoint of endpoints) {
    try {
      const response = await fetch(endpoint, {
        method: 'GET',
        headers: getAuthHeaders(),
      });

      if (response.ok) {
        return await response.json();
      }
      
      // If 404, try next endpoint
      if (response.status === 404 && endpoint !== endpoints[endpoints.length - 1]) {
        continue;
      }
      
      // For other errors, throw
      await handleApiError(response);
    } catch (err) {
      // If last endpoint, throw the error
      if (endpoint === endpoints[endpoints.length - 1]) {
        throw err;
      }
      // Otherwise, try next endpoint
      continue;
    }
  }

  // If all endpoints failed, return empty array
  return [];
}


// ============================================
// NOTIFICATION API
// ============================================

export interface NotificationDTO {
  id: number;
  userId: number;
  title: string;
  message: string;
  type: 'MESSAGE' | 'ASSIGNMENT' | 'GRADE' | 'ANNOUNCEMENT' | 'ENROLLMENT' | 'SUBMISSION' | 'REMINDER' | 'SYSTEM';
  referenceId?: number;
  referenceType?: string;
  isRead: boolean;
  createdAt: string;
  readAt?: string;
}

// Get all notifications
export async function getNotifications(): Promise<NotificationDTO[]> {
  const response = await fetch(`${API_BASE_URL}/notifications`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get unread notifications
export async function getUnreadNotifications(): Promise<NotificationDTO[]> {
  const response = await fetch(`${API_BASE_URL}/notifications/unread`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get unread count
export async function getUnreadNotificationCount(): Promise<number> {
  const response = await fetch(`${API_BASE_URL}/notifications/unread/count`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  const data = await response.json();
  return data.count;
}

// Mark notification as read
export async function markNotificationAsRead(notificationId: number): Promise<NotificationDTO> {
  const response = await fetch(`${API_BASE_URL}/notifications/${notificationId}/read`, {
    method: 'PUT',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Mark all notifications as read
export async function markAllNotificationsAsRead(): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/notifications/read-all`, {
    method: 'PUT',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }
}

// Delete notification
export async function deleteNotification(notificationId: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/notifications/${notificationId}`, {
    method: 'DELETE',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }
}

// Delete all read notifications
export async function deleteAllReadNotifications(): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/notifications/read`, {
    method: 'DELETE',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }
}


// ============================================
// DASHBOARD API
// ============================================

export interface StudentDashboardDTO {
  stats: {
    enrolledClasses: number;
    activeAssignments: number;
    completedAssignments: number;
    totalAssignments: number;
  };
  enrolledClasses: Array<{
    id: number;
    name: string;
    description: string;
    teacherName: string;
    studentCount: number;
  }>;
  upcomingAssignments: Array<{
    id: number;
    title: string;
    description: string;
    className: string;
    dueDate: string;
    status: string;
    grade?: number;
    isOverdue: boolean;
  }>;
  classProgress: Array<{
    classId: number;
    className: string;
    totalAssignments: number;
    completedAssignments: number;
    averageGrade?: number;
    progressPercentage: number;
  }>;
}

// Get student dashboard data
export async function getStudentDashboard(): Promise<StudentDashboardDTO> {
  const response = await fetch(`${API_BASE_URL}/dashboard/student`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}


// ============================================
// USER MANAGEMENT API
// ============================================

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

// Change password
export async function changePassword(data: ChangePasswordRequest): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/user/change-password`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    await handleApiError(response);
  }
}

// Update profile
export interface ProfileData {
  id: number;
  username: string;
  name: string;
  email: string;
  phoneNumber?: string;
  address?: string;
  profilePicture?: string;
  role: string;
}

// Get current user profile
export async function getCurrentProfile(): Promise<ProfileData> {
  const response = await fetch(`${API_BASE_URL}/user/profile`, {
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Update profile
export async function updateProfile(updates: {
  name?: string;
  email?: string;
  phoneNumber?: string;
  address?: string;
}): Promise<ProfileData> {
  const response = await fetch(`${API_BASE_URL}/user/profile`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify(updates),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  const data = await response.json();
  return data.profile;
}

// Upload profile picture
export async function uploadProfilePicture(file: File): Promise<ProfileData> {
  const formData = new FormData();
  formData.append('file', file);

  const token = localStorage.getItem('authToken');
  
  if (!token) {
    throw new Error('No authentication token found');
  }

  const response = await fetch(`${API_BASE_URL}/user/profile/picture`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
    body: formData,
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  const data = await response.json();
  return data.profile;
}

// Delete profile picture
export async function deleteProfilePicture(): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/user/profile/picture`, {
    method: 'DELETE',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }
}

// ============================================
// MATERIALS API
// ============================================

export interface Material {
  id: number;
  title: string;
  description?: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  classId: number;
  uploadedByName: string;
  uploadedAt: string;
}

// Get materials by class
export async function getMaterialsByClass(classId: number): Promise<Material[]> {
  const response = await fetch(`${API_BASE_URL}/materials/class/${classId}`, {
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Upload material
export async function uploadMaterial(
  file: File,
  title: string,
  description: string,
  classId: number
): Promise<void> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('title', title);
  formData.append('description', description);
  formData.append('classId', classId.toString());

  const token = localStorage.getItem('authToken');
  
  if (!token) {
    throw new Error('No authentication token found');
  }

  const response = await fetch(`${API_BASE_URL}/materials`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      // Don't set Content-Type - browser will set it with boundary for multipart
    },
    body: formData,
  });

  if (!response.ok) {
    await handleApiError(response);
  }
}

// Download material
export async function downloadMaterial(materialId: number, fileName: string): Promise<void> {
  const token = localStorage.getItem('authToken');
  
  if (!token) {
    throw new Error('No authentication token found');
  }

  const response = await fetch(`${API_BASE_URL}/materials/${materialId}/download`, {
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = fileName;
  document.body.appendChild(a);
  a.click();
  window.URL.revokeObjectURL(url);
  document.body.removeChild(a);
}

// Delete material
export async function deleteMaterial(materialId: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/materials/${materialId}`, {
    method: 'DELETE',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }
}

// ============================================
// SCHEDULES API
// ============================================

export interface Schedule {
  id: number;
  classId: number;
  className?: string;
  courseName?: string;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  room?: string;
  location?: string;
  notes?: string;
}

export interface ScheduleRequest {
  classId: number;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  room?: string;
  location?: string;
  notes?: string;
}

// Get schedules by class
export async function getSchedulesByClass(classId: number): Promise<Schedule[]> {
  const response = await fetch(`${API_BASE_URL}/schedules/class/${classId}`, {
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get student's timetable
export async function getMyTimetable(): Promise<Schedule[]> {
  const response = await fetch(`${API_BASE_URL}/schedules/my-timetable`, {
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Create schedule (Teacher only)
export async function createSchedule(schedule: ScheduleRequest): Promise<Schedule> {
  const response = await fetch(`${API_BASE_URL}/schedules`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(schedule),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Update schedule (Teacher only)
export async function updateSchedule(id: number, schedule: ScheduleRequest): Promise<Schedule> {
  const response = await fetch(`${API_BASE_URL}/schedules/${id}`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify(schedule),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Delete schedule (Teacher only)
export async function deleteSchedule(id: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/schedules/${id}`, {
    method: 'DELETE',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }
}

// ============================================
// USER SETTINGS API
// ============================================

export interface UserSettings {
  id: number;
  userId: number;
  // Appearance
  theme: string;
  language: string;
  // Notifications
  emailNotifications: boolean;
  pushNotifications: boolean;
  assignmentReminders: boolean;
  gradeNotifications: boolean;
  messageNotifications: boolean;
  // Privacy
  profileVisible: boolean;
  showEmail: boolean;
  showPhone: boolean;
  // Display
  itemsPerPage: number;
  dateFormat: string;
  timeFormat: string;
}

// Get user settings
export async function getUserSettings(): Promise<UserSettings> {
  const response = await fetch(`${API_BASE_URL}/settings`, {
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Update user settings
export async function updateUserSettings(updates: Partial<UserSettings>): Promise<UserSettings> {
  const response = await fetch(`${API_BASE_URL}/settings`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify(updates),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  const data = await response.json();
  return data.settings;
}

// Reset settings to defaults
export async function resetUserSettings(): Promise<UserSettings> {
  const response = await fetch(`${API_BASE_URL}/settings/reset`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  const data = await response.json();
  return data.settings;
}


// ============================================
// QUIZ API
// ============================================

export interface QuizDTO {
  id?: number;
  classId: number;
  title: string;
  description?: string;
  startTime: string;
  endTime: string;
  duration: number;
  totalMarks: number;
  passingMarks: number;
  randomizeQuestions?: boolean;
  showResultsImmediately?: boolean;
  maxAttempts?: number;
  status?: string;
  questionCount?: number;
  createdAt?: string;
  questions?: QuestionDTO[];
  attemptsUsed?: number;
  bestScore?: number;
  canAttempt?: boolean;
}

export interface QuestionDTO {
  id?: number;
  questionText: string;
  questionType: string;
  marks: number;
  explanation?: string;
  options?: QuestionOptionDTO[];
  correctAnswer?: string;
}

export interface QuestionOptionDTO {
  id?: number;
  optionText: string;
  isCorrect?: boolean;
}

export interface QuizAttemptDTO {
  id?: number;
  quizId: number;
  quizTitle?: string;
  studentId?: number;
  studentName?: string;
  attemptNumber?: number;
  startedAt?: string;
  submittedAt?: string;
  score?: number;
  totalMarks?: number;
  status?: string;
  answers?: StudentAnswerDTO[];
}

export interface StudentAnswerDTO {
  id?: number;
  questionId: number;
  questionText?: string;
  answerText?: string;
  selectedOptionId?: number;
  isCorrect?: boolean;
  marksAwarded?: number;
}

// Create quiz
export async function createQuiz(quiz: QuizDTO): Promise<QuizDTO> {
  const response = await fetch(`${API_BASE_URL}/quizzes`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(quiz),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get quizzes by class
export async function getQuizzesByClass(classId: number): Promise<QuizDTO[]> {
  const response = await fetch(`${API_BASE_URL}/quizzes/class/${classId}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get quiz by ID
export async function getQuizById(quizId: number): Promise<QuizDTO> {
  const response = await fetch(`${API_BASE_URL}/quizzes/${quizId}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Update quiz
export async function updateQuiz(quizId: number, quiz: QuizDTO): Promise<QuizDTO> {
  const response = await fetch(`${API_BASE_URL}/quizzes/${quizId}`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify(quiz),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Delete quiz
export async function deleteQuiz(quizId: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/quizzes/${quizId}`, {
    method: 'DELETE',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }
}

// Publish quiz
export async function publishQuiz(quizId: number): Promise<QuizDTO> {
  const response = await fetch(`${API_BASE_URL}/quizzes/${quizId}/publish`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Add question to quiz
export async function addQuestion(quizId: number, question: QuestionDTO): Promise<QuestionDTO> {
  const response = await fetch(`${API_BASE_URL}/quizzes/${quizId}/questions`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(question),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Update question
export async function updateQuestion(questionId: number, question: QuestionDTO): Promise<QuestionDTO> {
  const response = await fetch(`${API_BASE_URL}/quizzes/questions/${questionId}`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify(question),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Delete question
export async function deleteQuestion(questionId: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/quizzes/questions/${questionId}`, {
    method: 'DELETE',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }
}

// Get available quizzes for student
export async function getAvailableQuizzes(classId: number): Promise<QuizDTO[]> {
  const response = await fetch(`${API_BASE_URL}/quizzes/available/class/${classId}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Start quiz attempt
export async function startQuizAttempt(quizId: number): Promise<QuizAttemptDTO> {
  const response = await fetch(`${API_BASE_URL}/quizzes/${quizId}/start`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Submit quiz attempt
export async function submitQuizAttempt(attemptId: number, answers: StudentAnswerDTO[]): Promise<QuizAttemptDTO> {
  const response = await fetch(`${API_BASE_URL}/quizzes/attempts/${attemptId}/submit`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(answers),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get quiz attempts (teacher)
export async function getQuizAttempts(quizId: number): Promise<QuizAttemptDTO[]> {
  const response = await fetch(`${API_BASE_URL}/quizzes/${quizId}/attempts`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}


// ============================================
// LIVE CLASS API
// ============================================

export interface LiveClassDTO {
  id?: number;
  classId: number;
  className?: string;
  title: string;
  description?: string;
  scheduledStartTime: string;
  scheduledEndTime: string;
  actualStartTime?: string;
  actualEndTime?: string;
  status?: string;
  meetingId?: string;
  meetingPassword?: string;
  hostId?: number;
  hostName?: string;
  recordingUrl?: string;
  allowRecording?: boolean;
  allowChat?: boolean;
  allowScreenShare?: boolean;
  maxParticipants?: number;
  currentParticipants?: number;
  createdAt?: string;
}

// Schedule live class (teacher)
export async function scheduleLiveClass(liveClass: LiveClassDTO): Promise<LiveClassDTO> {
  const response = await fetch(`${API_BASE_URL}/live-classes`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(liveClass),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Update live class (teacher)
export async function updateLiveClass(id: number, liveClass: LiveClassDTO): Promise<LiveClassDTO> {
  const response = await fetch(`${API_BASE_URL}/live-classes/${id}`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify(liveClass),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Cancel live class (teacher)
export async function cancelLiveClass(id: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/live-classes/${id}`, {
    method: 'DELETE',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }
}

// Start live class (teacher)
export async function startLiveClass(id: number): Promise<LiveClassDTO> {
  const response = await fetch(`${API_BASE_URL}/live-classes/${id}/start`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// End live class (teacher)
export async function endLiveClass(id: number): Promise<LiveClassDTO> {
  const response = await fetch(`${API_BASE_URL}/live-classes/${id}/end`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get live classes by class ID (teacher)
export async function getLiveClassesByClass(classId: number): Promise<LiveClassDTO[]> {
  const response = await fetch(`${API_BASE_URL}/live-classes/class/${classId}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get available live classes (student)
export async function getAvailableLiveClasses(classId: number): Promise<LiveClassDTO[]> {
  const response = await fetch(`${API_BASE_URL}/live-classes/available/class/${classId}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Join live class (student)
export async function joinLiveClass(id: number): Promise<LiveClassDTO> {
  const response = await fetch(`${API_BASE_URL}/live-classes/${id}/join`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}

// Get live class by ID
export async function getLiveClassById(id: number): Promise<LiveClassDTO> {
  const response = await fetch(`${API_BASE_URL}/live-classes/${id}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return await response.json();
}


// ============================================
// ATTENDANCE API
// ============================================

export interface AttendanceSessionDTO {
  id?: number;
  classId: number;
  sessionDate: string;
  sessionTime?: string;
  title: string;
  description?: string;
  createdBy: number;
  createdByName?: string;
  createdAt?: string;
  updatedAt?: string;
  records?: AttendanceRecordDTO[];
  totalStudents?: number;
  presentCount?: number;
  absentCount?: number;
  lateCount?: number;
  attendancePercentage?: number;
}

export interface AttendanceRecordDTO {
  id?: number;
  sessionId: number;
  studentId: number;
  studentName?: string;
  status: string;
  markedAt?: string;
  markedBy?: number;
  markedByName?: string;
  notes?: string;
}

export interface AttendanceStatisticsDTO {
  studentId: number;
  studentName?: string;
  classId: number;
  totalSessions: number;
  presentCount: number;
  absentCount: number;
  lateCount: number;
  attendancePercentage: number;
}

// Create attendance session
export async function createAttendanceSession(session: AttendanceSessionDTO): Promise<AttendanceSessionDTO> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/attendance/sessions`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(session),
  });

  if (!response.ok) throw new Error('Failed to create attendance session');
  return response.json();
}

// Update attendance session
export async function updateAttendanceSession(id: number, session: AttendanceSessionDTO): Promise<AttendanceSessionDTO> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/attendance/sessions/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(session),
  });

  if (!response.ok) throw new Error('Failed to update attendance session');
  return response.json();
}

// Delete attendance session
export async function deleteAttendanceSession(id: number): Promise<void> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/attendance/sessions/${id}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) throw new Error('Failed to delete attendance session');
}

// Get attendance session by ID
export async function getAttendanceSessionById(id: number): Promise<AttendanceSessionDTO> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/attendance/sessions/${id}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) throw new Error('Failed to get attendance session');
  return response.json();
}

// Get attendance sessions by class
export async function getAttendanceSessionsByClass(classId: number): Promise<AttendanceSessionDTO[]> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/attendance/sessions/class/${classId}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) throw new Error('Failed to get attendance sessions');
  return response.json();
}

// Mark attendance for a student
export async function markAttendance(sessionId: number, studentId: number, status: string, notes?: string): Promise<AttendanceRecordDTO> {
  const token = getAuthToken();
  const params = new URLSearchParams({
    studentId: studentId.toString(),
    status: status,
  });
  if (notes) params.append('notes', notes);

  const response = await fetch(`${API_BASE_URL}/attendance/sessions/${sessionId}/mark?${params}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) throw new Error('Failed to mark attendance');
  return response.json();
}

// Mark bulk attendance
export async function markBulkAttendance(sessionId: number, studentStatusMap: Record<number, string>): Promise<AttendanceRecordDTO[]> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/attendance/sessions/${sessionId}/mark-bulk`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(studentStatusMap),
  });

  if (!response.ok) throw new Error('Failed to mark bulk attendance');
  return response.json();
}

// Get attendance records by session
export async function getAttendanceRecordsBySession(sessionId: number): Promise<AttendanceRecordDTO[]> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/attendance/records/session/${sessionId}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) throw new Error('Failed to get attendance records');
  return response.json();
}

// Get student statistics
export async function getStudentAttendanceStatistics(classId: number, studentId: number): Promise<AttendanceStatisticsDTO> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/attendance/statistics/student?classId=${classId}&studentId=${studentId}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) throw new Error('Failed to get student statistics');
  return response.json();
}

// Get class statistics
export async function getClassAttendanceStatistics(classId: number): Promise<AttendanceStatisticsDTO[]> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/attendance/statistics/class/${classId}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) throw new Error('Failed to get class statistics');
  return response.json();
}

// ==================== Discussion Forum API ====================

export interface DiscussionTopicDTO {
  id?: number;
  classId: number;
  createdBy: number;
  createdByName?: string;
  createdByRole?: string;
  title: string;
  content: string;
  isPinned?: boolean;
  isLocked?: boolean;
  isSolved?: boolean;
  viewsCount?: number;
  repliesCount?: number;
  likesCount?: number;
  isLikedByCurrentUser?: boolean;
  createdAt?: string;
  updatedAt?: string;
  replies?: DiscussionReplyDTO[];
}

export interface DiscussionReplyDTO {
  id?: number;
  topicId: number;
  userId: number;
  userName?: string;
  userRole?: string;
  content: string;
  isSolution?: boolean;
  likesCount?: number;
  isLikedByCurrentUser?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// Topic operations
export async function createDiscussionTopic(topic: DiscussionTopicDTO): Promise<DiscussionTopicDTO> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/discussions/topics`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(topic),
  });

  if (!response.ok) throw new Error('Failed to create discussion topic');
  return response.json();
}

export async function getDiscussionTopicById(topicId: number, currentUserId: number): Promise<DiscussionTopicDTO> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/discussions/topics/${topicId}?currentUserId=${currentUserId}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) throw new Error('Failed to get discussion topic');
  return response.json();
}

export async function getDiscussionTopicsByClass(classId: number, currentUserId: number): Promise<DiscussionTopicDTO[]> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/discussions/topics/class/${classId}?currentUserId=${currentUserId}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) throw new Error('Failed to get discussion topics');
  return response.json();
}

export async function updateDiscussionTopic(topicId: number, topic: DiscussionTopicDTO): Promise<DiscussionTopicDTO> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/discussions/topics/${topicId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(topic),
  });

  if (!response.ok) throw new Error('Failed to update discussion topic');
  return response.json();
}

export async function deleteDiscussionTopic(topicId: number): Promise<void> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/discussions/topics/${topicId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) throw new Error('Failed to delete discussion topic');
}

export async function pinDiscussionTopic(topicId: number, isPinned: boolean): Promise<DiscussionTopicDTO> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/discussions/topics/${topicId}/pin`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify({ isPinned }),
  });

  if (!response.ok) throw new Error('Failed to pin discussion topic');
  return response.json();
}

export async function lockDiscussionTopic(topicId: number, isLocked: boolean): Promise<DiscussionTopicDTO> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/discussions/topics/${topicId}/lock`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify({ isLocked }),
  });

  if (!response.ok) throw new Error('Failed to lock discussion topic');
  return response.json();
}

export async function markTopicAsSolved(topicId: number, isSolved: boolean): Promise<DiscussionTopicDTO> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/discussions/topics/${topicId}/solve`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify({ isSolved }),
  });

  if (!response.ok) throw new Error('Failed to mark topic as solved');
  return response.json();
}

// Reply operations
export async function createDiscussionReply(reply: DiscussionReplyDTO): Promise<DiscussionReplyDTO> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/discussions/replies`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(reply),
  });

  if (!response.ok) throw new Error('Failed to create discussion reply');
  return response.json();
}

export async function getDiscussionReplies(topicId: number, currentUserId: number): Promise<DiscussionReplyDTO[]> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/discussions/replies/topic/${topicId}?currentUserId=${currentUserId}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) throw new Error('Failed to get discussion replies');
  return response.json();
}

export async function updateDiscussionReply(replyId: number, reply: DiscussionReplyDTO): Promise<DiscussionReplyDTO> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/discussions/replies/${replyId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(reply),
  });

  if (!response.ok) throw new Error('Failed to update discussion reply');
  return response.json();
}

export async function deleteDiscussionReply(replyId: number): Promise<void> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/discussions/replies/${replyId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) throw new Error('Failed to delete discussion reply');
}

export async function markReplyAsSolution(replyId: number, isSolution: boolean): Promise<DiscussionReplyDTO> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/discussions/replies/${replyId}/solution`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify({ isSolution }),
  });

  if (!response.ok) throw new Error('Failed to mark reply as solution');
  return response.json();
}

// Like operations
export async function toggleTopicLike(topicId: number, userId: number): Promise<void> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/discussions/topics/${topicId}/like?userId=${userId}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) throw new Error('Failed to toggle topic like');
}

export async function toggleReplyLike(replyId: number, userId: number): Promise<void> {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/discussions/replies/${replyId}/like?userId=${userId}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) throw new Error('Failed to toggle reply like');
}


// ============================================
// AXIOS-LIKE API INSTANCE FOR ADMIN PAGES
// ============================================

// Create an axios-like API instance for compatibility with admin pages
export const api = {
  get: async (url: string, config?: { params?: Record<string, any> }) => {
    const queryParams = config?.params 
      ? '?' + new URLSearchParams(
          Object.entries(config.params)
            .filter(([_, v]) => v !== undefined && v !== null)
            .map(([k, v]) => [k, String(v)])
        ).toString()
      : '';
    
    const response = await fetch(`${API_BASE_URL}${url}${queryParams}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const error: any = new Error('API Error');
      error.response = {
        data: await response.json().catch(() => ({ message: response.statusText })),
        status: response.status,
      };
      throw error;
    }

    return { data: await response.json() };
  },

  post: async (url: string, data?: any) => {
    const response = await fetch(`${API_BASE_URL}${url}`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const error: any = new Error('API Error');
      error.response = {
        data: await response.json().catch(() => ({ message: response.statusText })),
        status: response.status,
      };
      throw error;
    }

    return { data: await response.json() };
  },

  put: async (url: string, data?: any) => {
    const response = await fetch(`${API_BASE_URL}${url}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const error: any = new Error('API Error');
      error.response = {
        data: await response.json().catch(() => ({ message: response.statusText })),
        status: response.status,
      };
      throw error;
    }

    return { data: await response.json() };
  },

  delete: async (url: string) => {
    const response = await fetch(`${API_BASE_URL}${url}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const error: any = new Error('API Error');
      error.response = {
        data: await response.json().catch(() => ({ message: response.statusText })),
        status: response.status,
      };
      throw error;
    }

    // Handle empty responses (204 No Content) or responses with no body
    const contentLength = response.headers.get('content-length');
    const contentType = response.headers.get('content-type');
    
    if (response.status === 204 || contentLength === '0' || !contentType?.includes('application/json')) {
      return { data: null };
    }

    // Try to parse JSON, but handle empty responses gracefully
    try {
      const text = await response.text();
      return { data: text ? JSON.parse(text) : null };
    } catch (error) {
      // If JSON parsing fails, return null for successful delete operations
      return { data: null };
    }
  },
};
