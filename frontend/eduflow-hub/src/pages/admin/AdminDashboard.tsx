import { useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Users, BookOpen, GraduationCap, FileText, ClipboardList, Video, TrendingUp, Activity } from "lucide-react";
import { api } from "@/services/api";
import { useToast } from "@/hooks/use-toast";
import { Progress } from "@/components/ui/progress";

interface AdminStats {
  totalUsers: number;
  totalStudents: number;
  totalTeachers: number;
  totalAdmins: number;
  totalCourses: number;
  totalClasses: number;
  activeClasses: number;
  totalAssignments: number;
  totalQuizzes: number;
  totalMaterials: number;
  averageAttendance: number;
  activeUsersToday: number;
}

interface TopCourse {
  id: number;
  name: string;
  studentsCount: number;
  rating: number;
  teacherName: string;
}

export default function AdminDashboard() {
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [topCourses, setTopCourses] = useState<TopCourse[]>([]);
  const [loading, setLoading] = useState(true);
  const { toast } = useToast();

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const [statsRes, coursesRes] = await Promise.all([
        api.get("/admin/stats"),
        api.get("/admin/reports/top-courses")
      ]);
      
      setStats(statsRes.data);
      setTopCourses(coursesRes.data);
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to load dashboard data",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  if (loading || !stats) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>
    );
  }

  const mainStats = [
    { label: "Total Users", value: stats.totalUsers.toLocaleString(), icon: Users, color: "text-primary" },
    { label: "Active Classes", value: stats.activeClasses.toString(), icon: BookOpen, color: "text-accent" },
    { label: "Total Courses", value: stats.totalCourses.toString(), icon: Video, color: "text-success" },
    { label: "Assignments", value: stats.totalAssignments.toString(), icon: FileText, color: "text-purple-500" },
  ];

  const userStats = [
    { label: "Students", value: stats.totalStudents, icon: GraduationCap, color: "text-blue-500" },
    { label: "Teachers", value: stats.totalTeachers, icon: BookOpen, color: "text-green-500" },
    { label: "Admins", value: stats.totalAdmins, icon: Users, color: "text-purple-500" },
  ];

  const contentStats = [
    { label: "Quizzes", value: stats.totalQuizzes, icon: ClipboardList },
    { label: "Materials", value: stats.totalMaterials, icon: FileText },
  ];

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">Admin Dashboard</h1>
        <p className="text-muted-foreground">System overview and management</p>
      </div>

      {/* Main Stats */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {mainStats.map((stat, index) => (
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

      <div className="grid gap-6 md:grid-cols-2">
        {/* User Distribution */}
        <Card className="glass-card">
          <CardHeader>
            <CardTitle>User Distribution</CardTitle>
            <CardDescription>Breakdown by role</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {userStats.map((stat, index) => (
                <div key={index} className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className={`p-2 rounded-lg bg-muted`}>
                      <stat.icon className={`h-4 w-4 ${stat.color}`} />
                    </div>
                    <div>
                      <p className="font-medium">{stat.label}</p>
                      <p className="text-sm text-muted-foreground">
                        {((stat.value / stats.totalUsers) * 100).toFixed(1)}% of total
                      </p>
                    </div>
                  </div>
                  <p className="text-2xl font-bold">{stat.value}</p>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Content Overview */}
        <Card className="glass-card">
          <CardHeader>
            <CardTitle>Content Overview</CardTitle>
            <CardDescription>Platform resources</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-6">
              {contentStats.map((stat, index) => (
                <div key={index} className="space-y-2">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <stat.icon className="h-4 w-4 text-muted-foreground" />
                      <span className="font-medium">{stat.label}</span>
                    </div>
                    <span className="text-2xl font-bold">{stat.value}</span>
                  </div>
                  <Progress value={(stat.value / 100) * 100} className="h-2" />
                </div>
              ))}
              
              <div className="pt-4 border-t">
                <div className="flex items-center justify-between">
                  <span className="text-sm text-muted-foreground">Total Classes</span>
                  <span className="text-xl font-bold">{stats.totalClasses}</span>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Top Courses */}
      <Card className="glass-card">
        <CardHeader>
          <CardTitle>Top Performing Courses</CardTitle>
          <CardDescription>Courses with highest engagement</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {topCourses.map((course) => (
              <div
                key={course.id}
                className="flex items-center justify-between rounded-lg border border-border bg-muted p-4 hover:bg-muted/80 transition-colors"
              >
                <div className="flex-1">
                  <p className="font-medium">{course.name}</p>
                  <p className="text-sm text-muted-foreground">
                    {course.teacherName} • {course.studentsCount} students
                  </p>
                </div>
                <div className="text-right">
                  <p className="text-xl font-semibold text-accent">{course.rating.toFixed(1)}</p>
                  <p className="text-xs text-muted-foreground">rating</p>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
