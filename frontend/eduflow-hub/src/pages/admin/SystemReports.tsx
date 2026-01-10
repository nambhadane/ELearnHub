import { useEffect, useState } from "react";
import { TrendingUp, Users, BookOpen, FileText, Download } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { useToast } from "@/hooks/use-toast";
import { api } from "@/services/api";

interface ReportData {
  userAnalytics: any;
  courseAnalytics: any;
  activityAnalytics: any;
  topCourses: any[];
  recentActivities: any[];
}

export default function SystemReports() {
  const { toast } = useToast();
  const [reportData, setReportData] = useState<ReportData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchReportData();
  }, []);

  const fetchReportData = async () => {
    try {
      setLoading(true);
      const [userRes, courseRes, activityRes, topCoursesRes, activitiesRes] = await Promise.all([
        api.get("/admin/analytics/users"),
        api.get("/admin/analytics/courses"),
        api.get("/admin/analytics/activity"),
        api.get("/admin/reports/top-courses"),
        api.get("/admin/reports/recent-activities")
      ]);

      setReportData({
        userAnalytics: userRes.data,
        courseAnalytics: courseRes.data,
        activityAnalytics: activityRes.data,
        topCourses: topCoursesRes.data,
        recentActivities: activitiesRes.data
      });
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to load report data",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (!reportData) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">Failed to load report data</p>
        <Button onClick={fetchReportData} className="mt-4">
          Retry
        </Button>
      </div>
    );
  }

  const reportStats = [
    {
      label: "Total Active Users",
      value: reportData.userAnalytics.totalUsers?.toString() || "0",
      change: "+12%", // Could be calculated from historical data
      trend: "up",
      icon: Users,
    },
    {
      label: "Classes This Semester",
      value: reportData.courseAnalytics.totalClasses?.toString() || "0",
      change: "+5",
      trend: "up",
      icon: BookOpen,
    },
    {
      label: "Total Assignments",
      value: reportData.courseAnalytics.totalAssignments?.toString() || "0",
      change: "+18%",
      trend: "up",
      icon: FileText,
    },
    {
      label: "Platform Usage",
      value: `${reportData.activityAnalytics.platformUsage || 0}%`,
      change: "+3%",
      trend: "up",
      icon: TrendingUp,
    },
  ];

  const activityData = [
    { label: "Student Engagement", value: reportData.activityAnalytics.studentEngagement || 0 },
    { label: "Teacher Engagement", value: reportData.activityAnalytics.teacherEngagement || 0 },
    { label: "Assignment Submission Rate", value: reportData.activityAnalytics.assignmentSubmissionRate || 0 },
    { label: "Class Attendance", value: reportData.activityAnalytics.classAttendance || 0 },
    { label: "Platform Usage", value: reportData.activityAnalytics.platformUsage || 0 },
  ];
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">System Reports</h1>
          <p className="text-muted-foreground">
            Analytics and insights about system usage
          </p>
        </div>
        <Button>
          <Download className="mr-2 h-4 w-4" />
          Export Report
        </Button>
      </div>

      {/* Stats Grid */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {reportStats.map((stat, index) => (
          <Card key={index} className="glass-card hover:shadow-lg transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {stat.label}
              </CardTitle>
              <stat.icon className="h-5 w-5 text-primary" />
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold">{stat.value}</div>
              <p className="text-xs text-success mt-1">
                {stat.change} from last month
              </p>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Activity Metrics */}
      <Card className="glass-card">
        <CardHeader>
          <CardTitle>Activity Metrics</CardTitle>
          <CardDescription>Key performance indicators for the platform</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-6">
            {activityData.map((item, index) => (
              <div key={index} className="space-y-2">
                <div className="flex items-center justify-between text-sm">
                  <span className="font-medium">{item.label}</span>
                  <span className="text-muted-foreground">{item.value}%</span>
                </div>
                <Progress value={item.value} className="h-2" />
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-6 md:grid-cols-2">
        {/* Top Courses */}
        <Card className="glass-card">
          <CardHeader>
            <CardTitle>Top Performing Courses</CardTitle>
            <CardDescription>Courses with highest engagement</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {reportData.topCourses.map((course, index) => (
                <div
                  key={index}
                  className="flex items-center justify-between rounded-lg border border-border bg-muted p-3"
                >
                  <div>
                    <p className="font-medium">{course.name}</p>
                    <p className="text-sm text-muted-foreground">
                      {course.studentsCount || 0} students
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold text-accent">{course.rating?.toFixed(1) || "N/A"}</p>
                    <p className="text-xs text-muted-foreground">rating</p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Recent Activities */}
        <Card className="glass-card">
          <CardHeader>
            <CardTitle>Recent Activities</CardTitle>
            <CardDescription>Latest system activities</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {reportData.recentActivities.length > 0 ? (
                reportData.recentActivities.map((activity, index) => (
                  <div
                    key={index}
                    className="flex items-center justify-between rounded-lg border border-border bg-muted p-3"
                  >
                    <div>
                      <p className="font-medium">{activity.message}</p>
                      <p className="text-sm text-muted-foreground">
                        by {activity.user}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="text-xs text-muted-foreground">
                        {new Date(activity.timestamp).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                ))
              ) : (
                <div className="text-center py-4">
                  <p className="text-muted-foreground">No recent activities</p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
