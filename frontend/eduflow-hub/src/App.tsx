import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { ThemeProvider } from "@/components/ThemeProvider";
import { DashboardLayout } from "@/components/layouts/DashboardLayout";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import RoleSelection from "./pages/RoleSelection";
import StudentDashboard from "./pages/student/StudentDashboard";
import Classes from "./pages/student/Classes";
import StudentClassDetail from "./pages/student/ClassDetail";
import Assignments from "./pages/student/Assignments";
import StudentAssignmentDetail from "./pages/student/AssignmentDetail";
import QuizAttempt from "./pages/student/QuizAttempt";
import QuizResults from "./pages/student/QuizResults";
import Messages from "./pages/student/Messages";
import Grades from "./pages/student/Grades";
import Profile from "./pages/student/Profile";
import StudentSettings from "./pages/student/Settings";
import Timetable from "./pages/student/Timetable";
import TeacherDashboard from "./pages/teacher/TeacherDashboard";
import MyClasses from "./pages/teacher/MyClasses";
import CreateClass from "./pages/teacher/CreateClass";
import CreateCourse from "./pages/teacher/CreateCourse";
import ClassDetail from "./pages/teacher/ClassDetail";
import ClassMaterials from "./pages/teacher/ClassMaterials";
import CreateAssignment from "./pages/teacher/CreateAssignment";
import Submissions from "./pages/teacher/Submissions";
import TeacherMessages from "./pages/teacher/Messages";
import TeacherSettings from "./pages/teacher/Settings";
import Quizzes from "./pages/teacher/Quizzes";
import QuizAttempts from "./pages/teacher/QuizAttempts";
import LiveClassRoom from "./pages/LiveClassRoom";
import AdminDashboard from "./pages/admin/AdminDashboard";
import Users from "./pages/admin/Users";
import SystemReports from "./pages/admin/SystemReports";
import Courses from "./pages/admin/Courses";
import AdminClasses from "./pages/admin/Classes";
import AdminAssignments from "./pages/admin/Assignments";
import AdminQuizzes from "./pages/admin/Quizzes";
import AdminAssignmentDetail from "./pages/admin/AssignmentDetail";
import QuizDetail from "./pages/admin/QuizDetail";
import AdminSettings from "./pages/admin/Settings";
import EmailVerification from "./pages/EmailVerification";
import NotFound from "./pages/NotFound";
import Login from "./pages/Login";

const queryClient = new QueryClient();

const App = () => {
  console.log("App component rendering");
  
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider defaultTheme="light">
        <TooltipProvider>
          <Toaster />
          <Sonner />
          <BrowserRouter>
            <Routes>
            <Route path="/" element={<RoleSelection />} />
            <Route path="/login" element={<Login />} />
            <Route path="/verify-email" element={<EmailVerification />} />
            
            {/* Student Routes */}
            <Route
              path="/student"
              element={
                <ProtectedRoute>
                  <DashboardLayout role="student" />
                </ProtectedRoute>
              }
            >
              <Route index element={<StudentDashboard />} />
              <Route path="classes" element={<Classes />} />
              <Route path="classes/:classId" element={<StudentClassDetail />} />
              <Route path="assignments" element={<Assignments />} />
              <Route path="assignments/:assignmentId" element={<StudentAssignmentDetail />} />
              <Route path="quiz/:quizId" element={<QuizAttempt />} />
              <Route path="quiz/:quizId/results" element={<QuizResults />} />
              <Route path="live-class/:id" element={<LiveClassRoom />} />
              <Route path="messages" element={<Messages />} />
              <Route path="grades" element={<Grades />} />
              <Route path="timetable" element={<Timetable />} />
              <Route path="profile" element={<Profile />} />
              <Route path="settings" element={<StudentSettings />} />
            </Route>

            {/* Teacher Routes */}
            <Route
              path="/teacher"
              element={
                <ProtectedRoute>
                  <DashboardLayout role="teacher" />
                </ProtectedRoute>
              }
            >
              <Route index element={<TeacherDashboard />} />
              <Route path="classes" element={<MyClasses />} />
              <Route path="classes/create" element={<CreateClass />} />
              <Route path="classes/:classId" element={<ClassDetail />} />
              <Route path="classes/:classId/materials" element={<ClassMaterials />} />
              <Route path="courses/create" element={<CreateCourse />} />
              <Route path="create-assignment" element={<CreateAssignment />} />
              <Route path="quizzes" element={<Quizzes />} />
              <Route path="quiz/:quizId/attempts" element={<QuizAttempts />} />
              <Route path="live-class/:id" element={<LiveClassRoom />} />
              <Route path="submissions" element={<Submissions />} />
              <Route path="messages" element={<TeacherMessages />} />
              <Route path="profile" element={<Profile />} />
              <Route path="settings" element={<TeacherSettings />} />
            </Route>

            {/* Admin Routes */}
            <Route
              path="/admin"
              element={
                <ProtectedRoute>
                  <DashboardLayout role="admin" />
                </ProtectedRoute>
              }
            >
              <Route index element={<AdminDashboard />} />
              <Route path="users" element={<Users />} />
              <Route path="courses" element={<Courses />} />
              <Route path="classes" element={<AdminClasses />} />
              <Route path="assignments" element={<AdminAssignments />} />
              <Route path="assignments/:id" element={<AdminAssignmentDetail />} />
              <Route path="quizzes" element={<AdminQuizzes />} />
              <Route path="quizzes/:id" element={<QuizDetail />} />
              <Route path="reports" element={<SystemReports />} />
              <Route path="settings" element={<AdminSettings />} />
              <Route path="profile" element={<Profile />} />
            </Route>

            {/* Catch-all */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </TooltipProvider>
    </ThemeProvider>
  </QueryClientProvider>
  );
};

export default App;
