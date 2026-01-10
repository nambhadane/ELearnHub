import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, Users, FileText, Loader2, Plus, Clock } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useToast } from "@/hooks/use-toast";
import { ClassDTO, getClassById, getAssignmentsByClass, AssignmentDTO, getClassStudents, ParticipantDTO } from "@/services/api";
import { ScheduleManager } from "@/components/ScheduleManager";
import { QuizManager } from "@/components/QuizManager";
import { LiveClassManager } from "@/components/LiveClassManager";
import { AttendanceManager } from "@/components/AttendanceManager";

export default function ClassDetail() {
  const { classId } = useParams<{ classId: string }>();
  const navigate = useNavigate();
  const { toast } = useToast();
  
  const [classData, setClassData] = useState<ClassDTO | null>(null);
  const [assignments, setAssignments] = useState<AssignmentDTO[]>([]);
  const [loadingAssignments, setLoadingAssignments] = useState(false);
  const [loading, setLoading] = useState(true);
  const [students, setStudents] = useState<ParticipantDTO[]>([]);
  const [loadingStudents, setLoadingStudents] = useState(false);

  useEffect(() => {
    const fetchClassData = async () => {
      if (!classId) {
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        const classIdNum = parseInt(classId);
        
        // Try to fetch class details (might not exist in backend)
        try {
          const classDetails = await getClassById(classIdNum);
          setClassData(classDetails);
        } catch (err) {
          // If getClassById fails (404), we can still work with just the classId
          console.warn('Could not fetch class details, using classId only:', err);
          // Set minimal class data
          setClassData({
            id: classIdNum,
            name: `Class ${classId}`,
            teacherId: 0,
            courseId: 0,
          });
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : "Failed to load class details";
        toast({
          title: "Error",
          description: message,
          variant: "destructive",
        });
      } finally {
        setLoading(false);
      }
    };

    fetchClassData();
  }, [classId, toast]);

  // Fetch assignments when classId is available
  useEffect(() => {
    const fetchAssignments = async () => {
      if (!classId) return;

      try {
        setLoadingAssignments(true);
        const classIdNum = parseInt(classId);
        const assignmentsList = await getAssignmentsByClass(classIdNum);
        setAssignments(assignmentsList);
      } catch (err) {
        // Silently handle assignment fetch errors - endpoint may not be implemented yet
        console.log('Assignments endpoint not available yet, showing empty list');
        setAssignments([]);
      } finally {
        setLoadingAssignments(false);
      }
    };

    fetchAssignments();
  }, [classId]);

  // Fetch students when classId is available
  useEffect(() => {
    const fetchStudents = async () => {
      if (!classId) return;

      try {
        setLoadingStudents(true);
        const classIdNum = parseInt(classId);
        const studentsList = await getClassStudents(classIdNum);
        setStudents(studentsList);
      } catch (err) {
        console.error('Error fetching students:', err);
        toast({
          title: "Error",
          description: "Failed to load students",
          variant: "destructive",
        });
        setStudents([]);
      } finally {
        setLoadingStudents(false);
      }
    };

    fetchStudents();
  }, [classId, toast]);

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
      <div className="flex items-center gap-4">
        <Button
          variant="ghost"
          size="icon"
          onClick={() => navigate("/teacher/classes")}
        >
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div className="space-y-2 flex-1">
          <h1 className="text-3xl font-bold tracking-tight">
            {classData?.name || `Class ${classId}`}
          </h1>
          <p className="text-muted-foreground">
            Manage class details, students, and materials
          </p>
        </div>
      </div>

      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="students">Students</TabsTrigger>
          <TabsTrigger value="schedule">Schedule</TabsTrigger>
          <TabsTrigger value="materials">Materials</TabsTrigger>
          <TabsTrigger value="assignments">Assignments</TabsTrigger>
          <TabsTrigger value="quizzes">Quizzes</TabsTrigger>
          <TabsTrigger value="live-classes">Live Classes</TabsTrigger>
          <TabsTrigger value="attendance">Attendance</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Class Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 md:grid-cols-2">
                <div>
                  <p className="text-sm text-muted-foreground">Class Name</p>
                  <p className="font-medium">{classData?.name || "Loading..."}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Class ID</p>
                  <p className="font-medium">{classId || "N/A"}</p>
                </div>

                <div>
                  <p className="text-sm text-muted-foreground">Teacher ID</p>
                  <p className="font-medium">{classData?.teacherId || "Loading..."}</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="students" className="space-y-4">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Enrolled Students</CardTitle>
              <CardDescription>Students enrolled in this class</CardDescription>
            </CardHeader>
            <CardContent>
              {loadingStudents ? (
                <div className="flex items-center justify-center py-8">
                  <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
                </div>
              ) : students.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-12">
                  <Users className="h-12 w-12 text-muted-foreground mb-4" />
                  <h3 className="text-lg font-semibold mb-2">No students enrolled</h3>
                  <p className="text-muted-foreground text-center mb-4">
                    Add students to this class from the "My Classes" page
                  </p>
                  <Button 
                    variant="outline"
                    onClick={() => navigate("/teacher/classes")}
                  >
                    <ArrowLeft className="mr-2 h-4 w-4" />
                    Go to My Classes
                  </Button>
                </div>
              ) : (
                <div className="space-y-3">
                  <div className="flex items-center justify-between mb-4">
                    <p className="text-sm text-muted-foreground">
                      {students.length} student{students.length !== 1 ? 's' : ''} enrolled
                    </p>
                  </div>
                  <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-3">
                    {students.map((student) => (
                      <Card key={student.id} className="hover:shadow-md transition-shadow">
                        <CardContent className="p-4">
                          <div className="flex items-center gap-3">
                            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary/10">
                              <Users className="h-6 w-6 text-primary" />
                            </div>
                            <div className="flex-1 min-w-0">
                              <p className="font-medium text-sm truncate">
                                {student.name || student.username}
                              </p>
                              <p className="text-xs text-muted-foreground truncate">
                                @{student.username}
                              </p>
                              {student.role && (
                                <Badge variant="secondary" className="mt-1 text-xs">
                                  {student.role}
                                </Badge>
                              )}
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    ))}
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="schedule" className="space-y-4">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Class Schedule</CardTitle>
              <CardDescription>Manage class schedule and timetable</CardDescription>
            </CardHeader>
            <CardContent>
              {classId && <ScheduleManager classId={parseInt(classId)} />}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="materials" className="space-y-4">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Course Materials</CardTitle>
              <CardDescription>Uploaded materials for this class</CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground">Materials will be displayed here</p>
              {/* TODO: Add materials list when backend endpoint is available */}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="assignments" className="space-y-4">
          <Card className="glass-card">
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>Assignments</CardTitle>
                  <CardDescription>Assignments for this class</CardDescription>
                </div>
                <Button onClick={() => navigate("/teacher/create-assignment")}>
                  <Plus className="mr-2 h-4 w-4" />
                  Create Assignment
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              {loadingAssignments ? (
                <div className="flex items-center justify-center py-8">
                  <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
                </div>
              ) : assignments.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-12">
                  <FileText className="h-12 w-12 text-muted-foreground mb-4" />
                  <h3 className="text-lg font-semibold mb-2">No assignments yet</h3>
                  <p className="text-muted-foreground text-center mb-4">
                    Create your first assignment for this class
                  </p>
                  <Button onClick={() => navigate("/teacher/create-assignment")}>
                    <Plus className="mr-2 h-4 w-4" />
                    Create Assignment
                  </Button>
                </div>
              ) : (
                <div className="space-y-4">
                  {assignments.map((assignment) => {
                    const dueDate = assignment.dueDate 
                      ? new Date(assignment.dueDate)
                      : null;
                    const isOverdue = dueDate && dueDate < new Date();
                    
                    return (
                      <Card key={assignment.id} className="hover:shadow-md transition-shadow">
                        <CardHeader>
                          <div className="flex items-start justify-between">
                            <div className="space-y-1 flex-1">
                              <CardTitle className="text-lg">{assignment.title}</CardTitle>
                              <CardDescription className="line-clamp-2">
                                {assignment.description}
                              </CardDescription>
                            </div>
                            <div className="flex gap-2 ml-4">
                              {assignment.status === "draft" && (
                                <Badge variant="secondary">Draft</Badge>
                              )}
                              {isOverdue && (
                                <Badge variant="destructive">Overdue</Badge>
                              )}
                            </div>
                          </div>
                        </CardHeader>
                        <CardContent>
                          <div className="flex items-center gap-6 text-sm text-muted-foreground">
                            {dueDate && (
                              <div className="flex items-center gap-2">
                                <Clock className="h-4 w-4" />
                                <span>
                                  Due: {dueDate.toLocaleDateString()} at {dueDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                </span>
                              </div>
                            )}
                            <div className="flex items-center gap-2">
                              <FileText className="h-4 w-4" />
                              <span>{assignment.maxGrade} points</span>
                            </div>
                            {assignment.weight && (
                              <div>
                                Weight: {assignment.weight}%
                              </div>
                            )}
                            {assignment.allowLateSubmission && (
                              <Badge variant="outline" className="text-xs">
                                Late submission allowed
                                {assignment.latePenalty && ` (${assignment.latePenalty}% penalty)`}
                              </Badge>
                            )}
                          </div>
                          {assignment.additionalInstructions && (
                            <div className="mt-4 pt-4 border-t">
                              <p className="text-sm font-medium mb-1">Additional Instructions:</p>
                              <p className="text-sm text-muted-foreground">
                                {assignment.additionalInstructions}
                              </p>
                            </div>
                          )}
                        </CardContent>
                      </Card>
                    );
                  })}
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="quizzes" className="space-y-4">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Quizzes</CardTitle>
              <CardDescription>Create and manage quizzes for this class</CardDescription>
            </CardHeader>
            <CardContent>
              {classId && <QuizManager classId={parseInt(classId)} />}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="live-classes" className="space-y-4">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Live Classes</CardTitle>
              <CardDescription>Schedule and conduct live video classes</CardDescription>
            </CardHeader>
            <CardContent>
              {classId && <LiveClassManager classId={parseInt(classId)} />}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="attendance" className="space-y-4">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Attendance</CardTitle>
              <CardDescription>Track and manage student attendance</CardDescription>
            </CardHeader>
            <CardContent>
              {classId && (
                <AttendanceManager 
                  classId={parseInt(classId)} 
                  students={students.map(s => ({ id: s.id, name: s.name || 'Unknown' }))}
                />
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}

