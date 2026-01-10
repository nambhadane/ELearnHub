import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { 
  BookOpen, 
  Clock, 
  Users, 
  FileText, 
  Play, 
  Download,
  Loader2,
  ArrowRight,
  User,
  GraduationCap
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Progress } from "@/components/ui/progress";
import { useToast } from "@/hooks/use-toast";
import { 
  getMyClasses, 
  getStudentProfile, 
  getClassById,
  getCourseById,
  getAssignmentsByClass,
  ClassDTO,
  Course,
  AssignmentDTO,
  UserProfile
} from "@/services/api";

// Extended interface for class with additional info
interface StudentClassInfo extends ClassDTO {
  courseName?: string;
  teacherName?: string;
  studentCount?: number;
  assignments?: AssignmentDTO[];
}

export default function Classes() {
  const navigate = useNavigate();
  const { toast } = useToast();

  const [classes, setClasses] = useState<StudentClassInfo[]>([]);
  const [selectedClass, setSelectedClass] = useState<StudentClassInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [profile, setProfile] = useState<UserProfile | null>(null);

  // Fetch student classes
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);

        // Get student profile
        const studentProfile = await getStudentProfile();
        setProfile(studentProfile);

        // Fetch classes - try /student/classes first, fallback to /classes/student/{id}
        let studentClasses: ClassDTO[] = [];
        try {
          studentClasses = await getMyClasses();
        } catch (err) {
          // Fallback to using student ID
          console.log("Trying fallback endpoint...");
          const { getClassesByStudent } = await import("@/services/api");
          studentClasses = await getClassesByStudent(studentProfile.id);
        }

        // Enrich classes with additional data
        const enrichedClasses = await Promise.all(
          studentClasses.map(async (classItem) => {
            try {
              // Fetch course details
              let course: Course | null = null;
              try {
                course = await getCourseById(classItem.courseId);
              } catch (err) {
                console.error(`Failed to fetch course ${classItem.courseId}:`, err);
              }

              // Fetch assignments for this class
              let assignments: AssignmentDTO[] = [];
              try {
                assignments = await getAssignmentsByClass(classItem.id);
              } catch (err) {
                console.error(`Failed to fetch assignments for class ${classItem.id}:`, err);
              }

              return {
                ...classItem,
                courseName: course?.name || "Unknown Course",
                teacherName: "Teacher", // You can fetch teacher name if needed
                studentCount: 0, // You can fetch this if backend provides it
                assignments: assignments,
              } as StudentClassInfo;
            } catch (err) {
              console.error(`Error enriching class ${classItem.id}:`, err);
              return {
                ...classItem,
                courseName: "Unknown Course",
                assignments: [],
              } as StudentClassInfo;
            }
          })
        );

        setClasses(enrichedClasses);

        // Set first class as selected by default
        if (enrichedClasses.length > 0) {
          setSelectedClass(enrichedClasses[0]);
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : "Failed to load classes";
        toast({
          title: "Error Loading Classes",
          description: message,
          variant: "destructive",
        });
        setClasses([]);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [toast]);

  // Calculate progress (mock - you can implement real progress calculation)
  const calculateProgress = (classItem: StudentClassInfo): number => {
    // Mock progress - you can calculate based on completed assignments, etc.
    if (!classItem.assignments || classItem.assignments.length === 0) return 0;
    // For now, return a random progress (replace with real calculation)
    return Math.floor(Math.random() * 50) + 30; // 30-80% for demo
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">My Classes</h1>
          <p className="text-muted-foreground">
            View your enrolled classes and access course materials
          </p>
        </div>
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">My Classes</h1>
        <p className="text-muted-foreground">
          View your enrolled classes and access course materials
        </p>
      </div>

      {classes.length === 0 ? (
        <Card className="glass-card">
          <CardContent className="flex flex-col items-center justify-center py-12">
            <BookOpen className="h-16 w-16 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">No Classes Yet</h3>
            <p className="text-sm text-muted-foreground text-center mb-4">
              You haven't been enrolled in any classes yet. Your teacher will add you to classes.
            </p>
          </CardContent>
        </Card>
      ) : (
        <>
          {/* Enrolled Classes Grid */}
          <div className="grid gap-4 md:grid-cols-1 lg:grid-cols-2">
            {classes.map((classItem) => {
              const progress = calculateProgress(classItem);
              const colorClasses = [
                "bg-primary",
                "bg-accent",
                "bg-success",
                "bg-purple-500",
                "bg-orange-500",
              ];
              const colorClass = colorClasses[classItem.id % colorClasses.length] || "bg-primary";
              const isExpanded = selectedClass?.id === classItem.id;

              return (
                <Card
                  key={classItem.id}
                  className={`glass-card hover:shadow-lg transition-all group ${
                    isExpanded ? "ring-2 ring-primary" : ""
                  }`}
                >
                  <CardHeader>
                    <div className="flex items-start justify-between">
                      <div className="space-y-1">
                        <CardTitle className="flex items-center gap-2">
                          <div className={`h-3 w-3 rounded-full ${colorClass}`} />
                          {classItem.name}
                        </CardTitle>
                        <CardDescription>{classItem.courseName}</CardDescription>
                        {classItem.teacherName && (
                          <div className="flex items-center gap-1 text-xs text-muted-foreground">
                            <User className="h-3 w-3" />
                            {classItem.teacherName}
                          </div>
                        )}
                      </div>
                      {classItem.studentCount !== undefined && classItem.studentCount > 0 && (
                        <Badge variant="secondary">
                          <Users className="mr-1 h-3 w-3" />
                          {classItem.studentCount}
                        </Badge>
                      )}
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Clock className="h-4 w-4" />
                      <span>Class ID: {classItem.id}</span>
                    </div>

                    <div className="space-y-2">
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-muted-foreground">Course Progress</span>
                        <span className="font-medium">{progress}%</span>
                      </div>
                      <Progress value={progress} className="h-2" />
                    </div>

                    {classItem.assignments && classItem.assignments.length > 0 && (
                      <div className="text-sm text-muted-foreground">
                        <FileText className="h-4 w-4 inline mr-1" />
                        {classItem.assignments.length} assignment{classItem.assignments.length !== 1 ? 's' : ''}
                      </div>
                    )}

                    {/* Course Details Section - Inside Card */}
                    {isExpanded && (
                      <div className="border-t pt-4 mt-4 space-y-4">
                        <h4 className="font-semibold text-sm">Course Details</h4>
                        <Tabs defaultValue="overview" className="space-y-4">
                          <TabsList className="grid w-full grid-cols-3">
                            <TabsTrigger value="overview">Overview</TabsTrigger>
                            <TabsTrigger value="assignments">
                              Assignments ({classItem.assignments?.length || 0})
                            </TabsTrigger>
                            <TabsTrigger value="materials">Materials</TabsTrigger>
                          </TabsList>

                          <TabsContent value="overview" className="space-y-3">
                            <div className="grid gap-2 text-sm">
                              <div className="flex items-center gap-2">
                                <BookOpen className="h-4 w-4 text-muted-foreground" />
                                <span><strong>Class:</strong> {classItem.name}</span>
                              </div>
                              <div className="flex items-center gap-2">
                                <GraduationCap className="h-4 w-4 text-muted-foreground" />
                                <span><strong>Course:</strong> {classItem.courseName}</span>
                              </div>
                              {classItem.teacherName && (
                                <div className="flex items-center gap-2">
                                  <User className="h-4 w-4 text-muted-foreground" />
                                  <span><strong>Teacher:</strong> {classItem.teacherName}</span>
                                </div>
                              )}
                              {classItem.assignments && (
                                <div className="flex items-center gap-2">
                                  <FileText className="h-4 w-4 text-muted-foreground" />
                                  <span><strong>Assignments:</strong> {classItem.assignments.length}</span>
                                </div>
                              )}
                            </div>
                          </TabsContent>

                          <TabsContent value="assignments" className="space-y-3">
                            {classItem.assignments && classItem.assignments.length > 0 ? (
                              <div className="space-y-2 max-h-64 overflow-y-auto">
                                {classItem.assignments.map((assignment) => (
                                  <div
                                    key={assignment.id}
                                    className="flex items-center justify-between rounded-lg border p-3 hover:bg-accent/50 cursor-pointer text-sm"
                                    onClick={() => navigate(`/student/assignments/${assignment.id}`)}
                                  >
                                    <div className="flex-1">
                                      <p className="font-medium">{assignment.title}</p>
                                      {assignment.dueDate && (
                                        <p className="text-xs text-muted-foreground flex items-center gap-1 mt-1">
                                          <Clock className="h-3 w-3" />
                                          Due: {new Date(assignment.dueDate).toLocaleDateString()}
                                        </p>
                                      )}
                                    </div>
                                    <ArrowRight className="h-4 w-4 text-muted-foreground" />
                                  </div>
                                ))}
                              </div>
                            ) : (
                              <p className="text-sm text-muted-foreground text-center py-4">No assignments yet</p>
                            )}
                          </TabsContent>

                          <TabsContent value="materials" className="space-y-3">
                            <p className="text-sm text-muted-foreground text-center py-4">
                              Click "View Details" to see course materials
                            </p>
                          </TabsContent>
                        </Tabs>
                      </div>
                    )}

                    <div className="flex items-center gap-2 pt-2">
                      <Button
                        size="sm"
                        variant={isExpanded ? "default" : "outline"}
                        className="flex-1"
                        onClick={() => setSelectedClass(isExpanded ? null : classItem)}
                      >
                        {isExpanded ? "Hide Details" : "Show Details"}
                      </Button>
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => navigate(`/student/classes/${classItem.id}`)}
                      >
                        View Full Page
                        <ArrowRight className="ml-2 h-4 w-4" />
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>


        </>
      )}
    </div>
  );
}
