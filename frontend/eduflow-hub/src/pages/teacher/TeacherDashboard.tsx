import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { 
  Users, 
  BookOpen, 
  FileText, 
  Clock, 
  Award, 
  TrendingUp, 
  Calendar,
  MessageSquare,
  Plus,
  Loader2,
  ArrowRight,
  CheckCircle2,
  AlertCircle
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { useToast } from "@/hooks/use-toast";
import { 
  getTeacherProfile, 
  getClassesByTeacher, 
  getCourses,
  getAssignmentsByClass,
  ClassDTO,
  AssignmentDTO,
  Course,
  UserProfile
} from "@/services/api";

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

// Helper function to check if assignment is overdue
const isOverdue = (dueDate: string) => {
  if (!dueDate) return false;
  try {
    const due = new Date(dueDate);
    const now = new Date();
    return due < now;
  } catch {
    return false;
  }
};

export default function TeacherDashboard() {
  const navigate = useNavigate();
  const { toast } = useToast();

  // State
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [classes, setClasses] = useState<ClassDTO[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [allAssignments, setAllAssignments] = useState<AssignmentDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalStudents: 0,
    activeClasses: 0,
    totalCourses: 0,
    totalAssignments: 0,
    upcomingAssignments: 0,
    overdueAssignments: 0,
  });

  // Fetch all dashboard data
  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);

        // Fetch teacher profile
        const teacherProfile = await getTeacherProfile();
        setProfile(teacherProfile);

        // Fetch classes
        const teacherClasses = await getClassesByTeacher(teacherProfile.id);
        setClasses(teacherClasses);

        // Fetch courses
        const teacherCourses = await getCourses();
        setCourses(teacherCourses);

        // Fetch assignments for all classes
        const assignmentsPromises = teacherClasses.map(async (classItem) => {
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

        // Calculate statistics
        const totalStudents = teacherProfile.totalStudents || 0;
        const activeClasses = teacherClasses.length;
        const totalCourses = teacherCourses.length;
        const totalAssignments = allAssignmentsFlat.length;
        const upcomingAssignments = allAssignmentsFlat.filter(a => 
          a.dueDate && isUpcoming(a.dueDate)
        ).length;
        const overdueAssignments = allAssignmentsFlat.filter(a => 
          a.dueDate && isOverdue(a.dueDate)
        ).length;

        setStats({
          totalStudents,
          activeClasses,
          totalCourses,
          totalAssignments,
          upcomingAssignments,
          overdueAssignments,
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

    fetchDashboardData();
  }, [toast]);

  // Get recent classes (last 5)
  const recentClasses = classes.slice(0, 5);

  // Get upcoming assignments (due within 7 days, sorted by due date)
  const upcomingAssignments = allAssignments
    .filter(a => a.dueDate && isUpcoming(a.dueDate))
    .sort((a, b) => {
      if (!a.dueDate || !b.dueDate) return 0;
      return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime();
    })
    .slice(0, 5);

  // Get overdue assignments
  const overdueAssignments = allAssignments
    .filter(a => a.dueDate && isOverdue(a.dueDate))
    .sort((a, b) => {
      if (!a.dueDate || !b.dueDate) return 0;
      return new Date(b.dueDate).getTime() - new Date(a.dueDate).getTime();
    })
    .slice(0, 5);

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">Teacher Dashboard</h1>
          <p className="text-muted-foreground">
            Welcome back{profile?.name ? `, ${profile.name.split(' ')[0]}` : ''}! Here's your overview
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => navigate("/teacher/classes/create")}>
            <Plus className="mr-2 h-4 w-4" />
            New Class
          </Button>
          <Button onClick={() => navigate("/teacher/create-assignment")}>
            <FileText className="mr-2 h-4 w-4" />
            New Assignment
          </Button>
        </div>
      </div>

      {/* Statistics Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card className="glass-card hover:shadow-lg transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Total Students
            </CardTitle>
            <Users className="h-5 w-5 text-primary" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{stats.totalStudents}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Across all classes
            </p>
          </CardContent>
        </Card>

        <Card className="glass-card hover:shadow-lg transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Active Classes
            </CardTitle>
            <BookOpen className="h-5 w-5 text-accent" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{stats.activeClasses}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Currently teaching
            </p>
          </CardContent>
        </Card>

        <Card className="glass-card hover:shadow-lg transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Total Courses
            </CardTitle>
            <FileText className="h-5 w-5 text-success" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{stats.totalCourses}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Courses created
            </p>
          </CardContent>
        </Card>

        <Card className="glass-card hover:shadow-lg transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Total Assignments
            </CardTitle>
            <FileText className="h-5 w-5 text-destructive" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{stats.totalAssignments}</div>
            <p className="text-xs text-muted-foreground mt-1">
              {stats.upcomingAssignments} upcoming, {stats.overdueAssignments} overdue
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Additional Stats Row */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card className="glass-card">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Years of Experience
            </CardTitle>
            <Award className="h-5 w-5 text-primary" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">
              {profile?.yearsOfExperience || 0}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Teaching experience
            </p>
          </CardContent>
        </Card>

        <Card className="glass-card">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Upcoming Deadlines
            </CardTitle>
            <Calendar className="h-5 w-5 text-warning" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{stats.upcomingAssignments}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Assignments due this week
            </p>
          </CardContent>
        </Card>

        <Card className="glass-card">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Requires Attention
            </CardTitle>
            <AlertCircle className="h-5 w-5 text-destructive" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{stats.overdueAssignments}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Overdue assignments
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Recent Classes */}
        <Card className="glass-card">
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle>Recent Classes</CardTitle>
                <CardDescription>Your most recent classes</CardDescription>
              </div>
              <Button 
                variant="ghost" 
                size="sm"
                onClick={() => navigate("/teacher/classes")}
              >
                View All
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            {recentClasses.length === 0 ? (
              <div className="text-center py-8">
                <BookOpen className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-sm text-muted-foreground mb-4">No classes yet</p>
                <Button onClick={() => navigate("/teacher/classes/create")}>
                  <Plus className="mr-2 h-4 w-4" />
                  Create Your First Class
                </Button>
              </div>
            ) : (
              <div className="space-y-3">
                {recentClasses.map((classItem) => (
                  <div
                    key={classItem.id}
                    className="flex items-center justify-between p-3 rounded-lg border border-border hover:bg-muted/50 cursor-pointer transition-colors"
                    onClick={() => navigate(`/teacher/classes/${classItem.id}`)}
                  >
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                        <BookOpen className="h-5 w-5 text-primary" />
                      </div>
                      <div>
                        <p className="font-medium">{classItem.name}</p>
                        <p className="text-xs text-muted-foreground">
                          Class ID: {classItem.id}
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
                <CardTitle>Upcoming Assignments</CardTitle>
                <CardDescription>Due within the next 7 days</CardDescription>
              </div>
              <Button 
                variant="ghost" 
                size="sm"
                onClick={() => navigate("/teacher/create-assignment")}
              >
                Create
                <Plus className="ml-2 h-4 w-4" />
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            {upcomingAssignments.length === 0 ? (
              <div className="text-center py-8">
                <Calendar className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-sm text-muted-foreground mb-4">No upcoming assignments</p>
                <Button onClick={() => navigate("/teacher/create-assignment")}>
                  <Plus className="mr-2 h-4 w-4" />
                  Create Assignment
                </Button>
              </div>
            ) : (
              <div className="space-y-3">
                {upcomingAssignments.map((assignment) => (
                  <div
                    key={assignment.id}
                    className="flex items-start justify-between p-3 rounded-lg border border-border hover:bg-muted/50 cursor-pointer transition-colors"
                    onClick={() => navigate(`/teacher/classes/${assignment.classId}`)}
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

      {/* Overdue Assignments Alert */}
      {overdueAssignments.length > 0 && (
        <Card className="glass-card border-destructive/50">
          <CardHeader>
            <div className="flex items-center gap-2">
              <AlertCircle className="h-5 w-5 text-destructive" />
              <CardTitle className="text-destructive">Overdue Assignments</CardTitle>
            </div>
            <CardDescription>
              These assignments have passed their due dates
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {overdueAssignments.map((assignment) => (
                <div
                  key={assignment.id}
                  className="flex items-start justify-between p-3 rounded-lg border border-destructive/20 bg-destructive/5 hover:bg-destructive/10 cursor-pointer transition-colors"
                  onClick={() => navigate(`/teacher/classes/${assignment.classId}`)}
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <p className="font-medium text-sm">{assignment.title}</p>
                      <Badge variant="destructive" className="text-xs">
                        Overdue
                      </Badge>
                      <Badge variant="secondary" className="text-xs">
                        {assignment.className}
                      </Badge>
                    </div>
                    <p className="text-xs text-muted-foreground">
                      Due: {formatDate(assignment.dueDate || '')}
                    </p>
                  </div>
                  <ArrowRight className="h-4 w-4 text-destructive ml-2" />
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Quick Actions */}
      <Card className="glass-card">
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
          <CardDescription>Common tasks and shortcuts</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Button
              variant="outline"
              className="h-auto flex-col items-start p-4"
              onClick={() => navigate("/teacher/classes/create")}
            >
              <BookOpen className="h-6 w-6 mb-2 text-primary" />
              <span className="font-semibold">Create Class</span>
              <span className="text-xs text-muted-foreground">Start a new class</span>
            </Button>

            <Button
              variant="outline"
              className="h-auto flex-col items-start p-4"
              onClick={() => navigate("/teacher/create-assignment")}
            >
              <FileText className="h-6 w-6 mb-2 text-accent" />
              <span className="font-semibold">Create Assignment</span>
              <span className="text-xs text-muted-foreground">Add new assignment</span>
            </Button>

            <Button
              variant="outline"
              className="h-auto flex-col items-start p-4"
              onClick={() => navigate("/teacher/submissions")}
            >
              <CheckCircle2 className="h-6 w-6 mb-2 text-success" />
              <span className="font-semibold">Review Submissions</span>
              <span className="text-xs text-muted-foreground">Grade assignments</span>
            </Button>

            <Button
              variant="outline"
              className="h-auto flex-col items-start p-4"
              onClick={() => navigate("/teacher/messages")}
            >
              <MessageSquare className="h-6 w-6 mb-2 text-primary" />
              <span className="font-semibold">Messages</span>
              <span className="text-xs text-muted-foreground">Chat with students</span>
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
