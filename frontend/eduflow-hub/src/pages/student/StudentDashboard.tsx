import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { BookOpen, FileText, Clock, TrendingUp, Calendar, CheckCircle2, Loader2, AlertCircle, ArrowRight } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { 
  getStudentProfile, 
  getClassesByStudent, 
  getAssignmentsByClass,
  getMySubmission,
  ClassDTO,
  AssignmentDTO,
  SubmissionDTO,
  UserProfile
} from "@/services/api";
import { useToast } from "@/hooks/use-toast";

// Helper function to format date
const formatDate = (dateString: string) => {
  if (!dateString) return "N/A";
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      month: 'short', 
      day: 'numeric',
      year: 'numeric'
    });
  } catch {
    return dateString;
  }
};

// Helper function to check if assignment is upcoming (due within 7 days)
const isUpcoming = (dueDate: string) => {
  if (!dueDate) return false;
  try {
    const due = new Date(dueDate);
    const now = new Date();
    const diffTime = due.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays >= 0 && diffDays <= 7;
  } catch {
    return false;
  }
};

export default function StudentDashboard() {
  const { toast } = useToast();
  const navigate = useNavigate();
  
  // State
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [classes, setClasses] = useState<ClassDTO[]>([]);
  const [allAssignments, setAllAssignments] = useState<(AssignmentDTO & { className?: string; classId?: number })[]>([]);
  const [submissions, setSubmissions] = useState<SubmissionDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    enrolledClasses: 0,
    totalAssignments: 0,
    completedAssignments: 0,
    pendingAssignments: 0,
    averageGrade: 0,
  });

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        setLoading(true);

        // Fetch student profile
        const studentProfile = await getStudentProfile();
        setProfile(studentProfile);

        // Fetch enrolled classes
        const studentClasses = await getClassesByStudent(studentProfile.id);
        setClasses(studentClasses);

        // Fetch assignments and submissions for all classes
        const assignmentsPromises = studentClasses.map(async (classItem) => {
          try {
            const assignments = await getAssignmentsByClass(classItem.id);
            return assignments.map(assignment => ({
              ...assignment,
              classId: classItem.id,
              className: classItem.name,
            }));
          } catch (err) {
            console.error(`Failed to fetch assignments for class ${classItem.id}:`, err);
            return [];
          }
        });

        const assignmentsArrays = await Promise.all(assignmentsPromises);
        const allAssignmentsFlat = assignmentsArrays.flat();
        setAllAssignments(allAssignmentsFlat);

        // Fetch submissions for each assignment (404 is normal if not submitted)
        const submissionsPromises = allAssignmentsFlat.map(async (assignment) => {
          try {
            const submission = await getMySubmission(assignment.id);
            return submission;
          } catch (err) {
            // 404 is expected if student hasn't submitted this assignment yet
            return null;
          }
        });

        const submissionsResults = await Promise.all(submissionsPromises);
        const studentSubmissions = submissionsResults.filter((s): s is SubmissionDTO => s !== null);
        setSubmissions(studentSubmissions);

        // Calculate statistics
        const enrolledClasses = studentClasses.length;
        const totalAssignments = allAssignmentsFlat.length;
        const completedAssignments = studentSubmissions.length;
        const pendingAssignments = totalAssignments - completedAssignments;
        
        // Calculate average grade
        const gradedSubmissions = studentSubmissions.filter(s => s.grade !== null && s.grade !== undefined);
        const averageGrade = gradedSubmissions.length > 0
          ? gradedSubmissions.reduce((sum, s) => sum + (s.grade || 0), 0) / gradedSubmissions.length
          : 0;

        setStats({
          enrolledClasses,
          totalAssignments,
          completedAssignments,
          pendingAssignments,
          averageGrade,
        });

      } catch (err) {
        const message = err instanceof Error ? err.message : "Failed to load dashboard data";
        toast({
          title: "Error",
          description: message,
          variant: "destructive",
        });
      } finally {
        setLoading(false);
      }
    };

    fetchDashboard();
  }, [toast]);

  // Get upcoming assignments (due within 7 days, not submitted)
  const upcomingAssignments = allAssignments
    .filter(a => {
      const hasSubmission = submissions.some(s => s.assignmentId === a.id);
      return !hasSubmission && a.dueDate && isUpcoming(a.dueDate);
    })
    .sort((a, b) => {
      if (!a.dueDate || !b.dueDate) return 0;
      return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime();
    })
    .slice(0, 5);

  // Calculate class progress
  const classProgress = classes.map(classItem => {
    const classAssignments = allAssignments.filter(a => a.classId === classItem.id);
    const classSubmissions = submissions.filter(s => 
      classAssignments.some(a => a.id === s.assignmentId)
    );
    const gradedSubmissions = classSubmissions.filter(s => s.grade !== null && s.grade !== undefined);
    const avgGrade = gradedSubmissions.length > 0
      ? gradedSubmissions.reduce((sum, s) => sum + (s.grade || 0), 0) / gradedSubmissions.length
      : 0;
    
    return {
      classId: classItem.id,
      className: classItem.name,
      totalAssignments: classAssignments.length,
      completedAssignments: classSubmissions.length,
      averageGrade: avgGrade,
      progressPercentage: classAssignments.length > 0 
        ? Math.round((classSubmissions.length / classAssignments.length) * 100)
        : 0,
    };
  });

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  const statsCards = [
    { 
      label: "Enrolled Classes", 
      value: stats.enrolledClasses.toString(), 
      icon: BookOpen, 
      color: "text-primary" 
    },
    { 
      label: "Pending Assignments", 
      value: stats.pendingAssignments.toString(), 
      icon: FileText, 
      color: "text-accent" 
    },
    { 
      label: "Completed", 
      value: stats.completedAssignments.toString(), 
      icon: CheckCircle2, 
      color: "text-success" 
    },
    { 
      label: "Average Grade", 
      value: stats.averageGrade > 0 ? `${stats.averageGrade.toFixed(1)}%` : "N/A", 
      icon: TrendingUp, 
      color: "text-purple-500" 
    },
  ];
  return (
    <div className="space-y-6">
      {/* Welcome Header */}
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">
          Welcome back{profile?.name ? `, ${profile.name.split(' ')[0]}` : ''}! 👋
        </h1>
        <p className="text-muted-foreground">
          Here's what's happening with your courses today.
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {statsCards.map((stat, index) => (
          <Card key={index} className="glass-card hover:shadow-lg transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {stat.label}
              </CardTitle>
              <stat.icon className={`h-5 w-5 ${stat.color}`} />
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold">{stat.value}</div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Enrolled Classes */}
        <Card className="glass-card">
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="flex items-center gap-2">
                  <BookOpen className="h-5 w-5 text-primary" />
                  My Classes
                </CardTitle>
                <CardDescription>Your enrolled classes</CardDescription>
              </div>
              <Button 
                variant="ghost" 
                size="sm"
                onClick={() => navigate("/student/classes")}
              >
                View All
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            {classes.length === 0 ? (
              <div className="text-center py-8">
                <BookOpen className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-sm text-muted-foreground">No classes enrolled yet</p>
              </div>
            ) : (
              <div className="space-y-3">
                {classes.slice(0, 5).map((classItem) => (
                  <div
                    key={classItem.id}
                    className="flex items-center justify-between p-3 rounded-lg border border-border hover:bg-muted/50 cursor-pointer transition-colors"
                    onClick={() => navigate(`/student/classes/${classItem.id}`)}
                  >
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                        <BookOpen className="h-5 w-5 text-primary" />
                      </div>
                      <div>
                        <p className="font-medium">{classItem.name}</p>
                        <p className="text-xs text-muted-foreground">
                          {classItem.description || `Class ID: ${classItem.id}`}
                        </p>
                      </div>
                    </div>
                    <ArrowRight className="h-4 w-4 text-muted-foreground" />
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Upcoming Assignments */}
        <Card className="glass-card">
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="flex items-center gap-2">
                  <Calendar className="h-5 w-5 text-accent" />
                  Upcoming Assignments
                </CardTitle>
                <CardDescription>Due within the next 7 days</CardDescription>
              </div>
              <Button 
                variant="ghost" 
                size="sm"
                onClick={() => navigate("/student/classes")}
              >
                View All
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            {upcomingAssignments.length === 0 ? (
              <div className="text-center py-8">
                <Calendar className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-sm text-muted-foreground">No upcoming assignments</p>
              </div>
            ) : (
              <div className="space-y-3">
                {upcomingAssignments.map((assignment) => (
                  <div
                    key={assignment.id}
                    className="flex items-start justify-between p-3 rounded-lg border border-border hover:bg-muted/50 cursor-pointer transition-colors"
                    onClick={() => navigate(`/student/classes/${assignment.classId}`)}
                  >
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <p className="font-medium text-sm">{assignment.title}</p>
                        <Badge variant="secondary" className="text-xs">
                          {assignment.className}
                        </Badge>
                      </div>
                      <p className="text-xs text-muted-foreground">
                        Due: {formatDate(assignment.dueDate || '')}
                      </p>
                      {assignment.maxGrade && (
                        <p className="text-xs text-muted-foreground">
                          Max Grade: {assignment.maxGrade} points
                        </p>
                      )}
                    </div>
                    <Clock className="h-4 w-4 text-warning ml-2" />
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Progress Overview */}
      <Card className="glass-card">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <TrendingUp className="h-5 w-5 text-primary" />
            Overall Progress
          </CardTitle>
          <CardDescription>Your performance across all courses</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {classProgress.length === 0 ? (
            <div className="text-center py-8">
              <TrendingUp className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-sm text-muted-foreground">No progress data available</p>
            </div>
          ) : (
            <div className="space-y-4">
              {classProgress.map((progress) => (
                <div key={progress.classId} className="space-y-2">
                  <div className="flex items-center justify-between text-sm">
                    <span className="font-medium">{progress.className}</span>
                    <div className="flex items-center gap-2">
                      {progress.averageGrade > 0 && (
                        <span className="text-xs text-muted-foreground">
                          Avg: {progress.averageGrade.toFixed(1)}%
                        </span>
                      )}
                      <span className="text-muted-foreground">
                        {progress.completedAssignments}/{progress.totalAssignments}
                      </span>
                    </div>
                  </div>
                  <Progress value={progress.progressPercentage} className="h-2" />
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
