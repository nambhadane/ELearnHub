import { useEffect, useState, useMemo } from "react";
import { Award, TrendingUp, BookOpen, Target, Loader2 } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/hooks/use-toast";
import { getMyAssignments, StudentAssignmentDTO } from "@/services/api";

interface CourseGrade {
  courseName: string;
  assignments: StudentAssignmentDTO[];
  averageGrade: number;
  letterGrade: string;
  totalAssignments: number;
  gradedAssignments: number;
}

const getGradeColor = (percentage: number) => {
  if (percentage >= 90) return "text-success";
  if (percentage >= 80) return "text-primary";
  if (percentage >= 70) return "text-accent";
  return "text-destructive";
};

const getLetterGrade = (percentage: number): string => {
  if (percentage >= 97) return "A+";
  if (percentage >= 93) return "A";
  if (percentage >= 90) return "A-";
  if (percentage >= 87) return "B+";
  if (percentage >= 83) return "B";
  if (percentage >= 80) return "B-";
  if (percentage >= 77) return "C+";
  if (percentage >= 73) return "C";
  if (percentage >= 70) return "C-";
  if (percentage >= 67) return "D+";
  if (percentage >= 63) return "D";
  if (percentage >= 60) return "D-";
  return "F";
};

const getLetterGradeBadge = (grade: string) => {
  if (grade.startsWith("A")) return "default";
  if (grade.startsWith("B")) return "secondary";
  if (grade.startsWith("C")) return "outline";
  return "destructive";
};

const calculateGPA = (courseGrades: CourseGrade[]): number => {
  if (courseGrades.length === 0) return 0;
  
  const gradePoints: { [key: string]: number } = {
    "A+": 4.0, "A": 4.0, "A-": 3.7,
    "B+": 3.3, "B": 3.0, "B-": 2.7,
    "C+": 2.3, "C": 2.0, "C-": 1.7,
    "D+": 1.3, "D": 1.0, "D-": 0.7,
    "F": 0.0
  };

  let totalPoints = 0;
  let totalCourses = 0;

  courseGrades.forEach(course => {
    if (course.gradedAssignments > 0) {
      const points = gradePoints[course.letterGrade] || 0;
      totalPoints += points;
      totalCourses++;
    }
  });

  return totalCourses > 0 ? totalPoints / totalCourses : 0;
};

export default function Grades() {
  const { toast } = useToast();
  const [assignments, setAssignments] = useState<StudentAssignmentDTO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadGrades = async () => {
      try {
        setLoading(true);
        const data = await getMyAssignments();
        setAssignments(data || []);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Failed to load grades";
        toast({
          title: "Error",
          description: message,
          variant: "destructive",
        });
      } finally {
        setLoading(false);
      }
    };

    loadGrades();
  }, [toast]);

  // Group assignments by course/class
  const courseGrades = useMemo(() => {
    const courseMap = new Map<string, StudentAssignmentDTO[]>();

    assignments.forEach(assignment => {
      const courseName = assignment.className || "Unknown Course";
      if (!courseMap.has(courseName)) {
        courseMap.set(courseName, []);
      }
      courseMap.get(courseName)!.push(assignment);
    });

    const courses: CourseGrade[] = [];
    courseMap.forEach((assignments, courseName) => {
      const gradedAssignments = assignments.filter(a => a.grade != null && a.grade !== undefined);
      const totalGrade = gradedAssignments.reduce((sum, a) => {
        const percentage = a.maxGrade > 0 ? (a.grade! / a.maxGrade) * 100 : 0;
        return sum + percentage;
      }, 0);
      const averageGrade = gradedAssignments.length > 0 ? totalGrade / gradedAssignments.length : 0;
      const letterGrade = getLetterGrade(averageGrade);

      courses.push({
        courseName,
        assignments,
        averageGrade,
        letterGrade,
        totalAssignments: assignments.length,
        gradedAssignments: gradedAssignments.length,
      });
    });

    return courses.sort((a, b) => b.averageGrade - a.averageGrade);
  }, [assignments]);

  // Calculate overall stats
  const stats = useMemo(() => {
    const gradedAssignments = assignments.filter(a => a.grade != null && a.grade !== undefined);
    const totalAssignments = assignments.length;
    const completedAssignments = assignments.filter(a => a.status === "graded" || a.status === "submitted").length;

    // Calculate average score
    const totalScore = gradedAssignments.reduce((sum, a) => {
      const percentage = a.maxGrade > 0 ? (a.grade! / a.maxGrade) * 100 : 0;
      return sum + percentage;
    }, 0);
    const avgScore = gradedAssignments.length > 0 ? totalScore / gradedAssignments.length : 0;

    // Calculate GPA
    const gpa = calculateGPA(courseGrades);

    return {
      gpa: gpa.toFixed(2),
      avgScore: avgScore.toFixed(1),
      completed: `${completedAssignments}/${totalAssignments}`,
      targetGPA: "10",
    };
  }, [assignments, courseGrades]);

  // Get graded assignments for the table
  const gradedAssignments = useMemo(() => {
    return assignments
      .filter(a => a.grade != null && a.grade !== undefined && a.status === "graded")
      .sort((a, b) => {
        const dateA = a.submittedAt ? new Date(a.submittedAt).getTime() : 0;
        const dateB = b.submittedAt ? new Date(b.submittedAt).getTime() : 0;
        return dateB - dateA; // Most recent first
      });
  }, [assignments]);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full py-12">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">My Grades</h1>
        <p className="text-muted-foreground">
          Track your academic performance and progress
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card className="glass-card hover:shadow-lg transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Overall GPA
            </CardTitle>
            <Award className="h-5 w-5 text-primary" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{stats.gpa}</div>
          </CardContent>
        </Card>

        <Card className="glass-card hover:shadow-lg transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Avg Score
            </CardTitle>
            <TrendingUp className="h-5 w-5 text-success" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{stats.avgScore}%</div>
          </CardContent>
        </Card>

        <Card className="glass-card hover:shadow-lg transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Completed
            </CardTitle>
            <BookOpen className="h-5 w-5 text-accent" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{stats.completed}</div>
          </CardContent>
        </Card>

        <Card className="glass-card hover:shadow-lg transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Target GPA
            </CardTitle>
            <Target className="h-5 w-5 text-purple-500" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{stats.targetGPA}</div>
          </CardContent>
        </Card>
      </div>

      {/* Course Grades */}
      {courseGrades.length > 0 ? (
        <Card className="glass-card">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BookOpen className="h-5 w-5 text-primary" />
              Course Grades
            </CardTitle>
            <CardDescription>Your performance across all courses</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-6">
              {courseGrades.map((course, index) => (
                <div key={index} className="space-y-2">
                  <div className="flex items-center justify-between">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <h4 className="font-semibold">{course.courseName}</h4>
                        {course.gradedAssignments > 0 && (
                          <Badge variant={getLetterGradeBadge(course.letterGrade)}>
                            {course.letterGrade}
                          </Badge>
                        )}
                      </div>
                      <p className="text-sm text-muted-foreground">
                        {course.gradedAssignments} of {course.totalAssignments} assignments graded
                      </p>
                    </div>
                    <div className="text-right">
                      {course.gradedAssignments > 0 ? (
                        <p className={`text-2xl font-bold ${getGradeColor(course.averageGrade)}`}>
                          {course.averageGrade.toFixed(1)}%
                        </p>
                      ) : (
                        <p className="text-2xl font-bold text-muted-foreground">-</p>
                      )}
                    </div>
                  </div>
                  {course.gradedAssignments > 0 && (
                    <Progress value={course.averageGrade} className="h-2" />
                  )}
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      ) : (
        <Card className="glass-card">
          <CardContent className="py-12 text-center">
            <BookOpen className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
            <p className="text-muted-foreground">No course grades available yet.</p>
          </CardContent>
        </Card>
      )}

      {/* Assignment Grades Table */}
      {gradedAssignments.length > 0 ? (
        <Card className="glass-card">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Award className="h-5 w-5 text-accent" />
              Recent Assignment Grades
            </CardTitle>
            <CardDescription>Detailed breakdown of your assignment scores</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Assignment</TableHead>
                  <TableHead>Course</TableHead>
                  <TableHead>Submitted Date</TableHead>
                  <TableHead className="text-right">Grade</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {gradedAssignments.map((assignment) => {
                  const percentage = assignment.maxGrade > 0 
                    ? (assignment.grade! / assignment.maxGrade) * 100 
                    : 0;
                  const submittedDate = assignment.submittedAt 
                    ? new Date(assignment.submittedAt).toLocaleDateString() 
                    : "N/A";

                  return (
                    <TableRow key={assignment.id}>
                      <TableCell className="font-medium">{assignment.title}</TableCell>
                      <TableCell>{assignment.className || "Unknown"}</TableCell>
                      <TableCell>{submittedDate}</TableCell>
                      <TableCell className="text-right">
                        <span className={`font-semibold ${getGradeColor(percentage)}`}>
                          {assignment.grade}/{assignment.maxGrade}
                        </span>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      ) : (
        <Card className="glass-card">
          <CardContent className="py-12 text-center">
            <Award className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
            <p className="text-muted-foreground">No graded assignments yet.</p>
            <p className="text-sm text-muted-foreground mt-2">
              Your assignment grades will appear here once teachers have graded them.
            </p>
          </CardContent>
        </Card>
      )}

      {/* Grade Trends */}
      {courseGrades.length > 0 && (
        <Card className="glass-card">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <TrendingUp className="h-5 w-5 text-success" />
              Performance Overview
            </CardTitle>
            <CardDescription>Your academic progress summary</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="space-y-2">
                <div className="flex items-center justify-between text-sm">
                  <span className="font-medium">Overall Average</span>
                  <span className="text-muted-foreground">
                    {stats.avgScore}% ({stats.gpa} GPA)
                  </span>
                </div>
                <Progress value={parseFloat(stats.avgScore)} className="h-2" />
              </div>
              <div className="space-y-2">
                <div className="flex items-center justify-between text-sm">
                  <span className="font-medium">Graded Assignments</span>
                  <span className="text-muted-foreground">
                    {gradedAssignments.length} of {assignments.length} assignments
                  </span>
                </div>
                <Progress 
                  value={assignments.length > 0 ? (gradedAssignments.length / assignments.length) * 100 : 0} 
                  className="h-2" 
                />
              </div>
            </div>

            {parseFloat(stats.avgScore) >= 90 && (
              <div className="mt-6 p-4 rounded-lg bg-success/10 border border-success/20">
                <p className="text-sm font-medium text-success flex items-center gap-2">
                  <TrendingUp className="h-4 w-4" />
                  Excellent work! You're maintaining a high average score.
                </p>
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
