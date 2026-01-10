import { useState, FormEvent, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { BookOpen, ArrowLeft, Plus, RefreshCw } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useToast } from "@/hooks/use-toast";
import { createClass, getCourses, Course } from "@/services/api";

export default function CreateClass() {
  const navigate = useNavigate();
  const { toast } = useToast();
  
  const [className, setClassName] = useState("");
  const [courseId, setCourseId] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [courses, setCourses] = useState<Course[]>([]);
  const [loadingCourses, setLoadingCourses] = useState(true);

  const fetchCourses = async () => {
    try {
      setLoadingCourses(true);
      const fetchedCourses = await getCourses();
      setCourses(fetchedCourses);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to load courses";
      toast({
        title: "Error",
        description: message,
        variant: "destructive",
      });
    } finally {
      setLoadingCourses(false);
    }
  };

  useEffect(() => {
    fetchCourses();
  }, []);

  // Refresh courses when page becomes visible (e.g., returning from CreateCourse)
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        fetchCourses();
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => document.removeEventListener('visibilitychange', handleVisibilityChange);
  }, []);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    if (!className.trim()) {
      toast({
        title: "Validation Error",
        description: "Class name is required",
        variant: "destructive",
      });
      return;
    }

    if (!courseId) {
      toast({
        title: "Validation Error",
        description: "Please select a course",
        variant: "destructive",
      });
      return;
    }

    try {
      setIsSubmitting(true);

      const newClass = await createClass({
        courseId: parseInt(courseId),
        name: className.trim(),
      });

      toast({
        title: "Class Created",
        description: `${newClass.name} has been created successfully.`,
      });

      navigate("/teacher/classes");
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to create class";
      toast({
        title: "Error",
        description: message,
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate("/teacher/classes")}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">Create New Class</h1>
          <p className="text-muted-foreground">Create a new class for your course</p>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Class Information</CardTitle>
              <CardDescription>Fill in the details to create a new class</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-6">
                
                {/* --- FIXED SECTION STARTS HERE --- */}
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <Label htmlFor="course">Select Course *</Label>
                    <div className="flex gap-2">
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        onClick={fetchCourses}
                        disabled={loadingCourses}
                        className="h-8"
                      >
                        <RefreshCw className={`h-4 w-4 mr-2 ${loadingCourses ? "animate-spin" : ""}`} />
                        Refresh
                      </Button>
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => navigate("/teacher/courses/create")}
                        className="h-8"
                      >
                        <Plus className="h-4 w-4 mr-2" />
                        New Course
                      </Button>
                    </div>
                  </div>

                  <Select value={courseId} onValueChange={setCourseId} disabled={loadingCourses}>
                    <SelectTrigger id="course">
                      <SelectValue placeholder={loadingCourses ? "Loading courses..." : "Choose a course"} />
                    </SelectTrigger>

                    <SelectContent>
                      {courses.length === 0 && !loadingCourses ? (
                        <SelectItem value="no-courses" disabled>No courses available</SelectItem>
                      ) : (
                        courses.map((course) => (
                          <SelectItem key={course.id} value={course.id.toString()}>
                            {course.name} {course.description && ` - ${course.description}`}
                          </SelectItem>
                        ))
                      )}
                    </SelectContent>
                  </Select>

                  {courses.length > 0 && (
                    <p className="text-xs text-muted-foreground">
                      {courses.length} course{courses.length !== 1 ? "s" : ""} available
                    </p>
                  )}

                  {courses.length === 0 && !loadingCourses && (
                    <div className="space-y-2">
                      <p className="text-sm text-muted-foreground">
                        You need to create a course first before creating classes.
                      </p>
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => navigate("/teacher/courses/create")}
                        className="w-full"
                      >
                        <Plus className="mr-2 h-4 w-4" />
                        Create Your First Course
                      </Button>
                    </div>
                  )}
                </div>
                {/* --- FIXED SECTION ENDS HERE --- */}

                <div className="space-y-2">
                  <Label htmlFor="className">Class Name *</Label>
                  <Input
                    id="className"
                    placeholder="e.g., Web Development - Section A"
                    value={className}
                    onChange={(e) => setClassName(e.target.value)}
                  />
                </div>

                <div className="flex gap-2 pt-4">
                  <Button type="submit" className="flex-1" disabled={isSubmitting || courses.length === 0}>
                    {isSubmitting ? "Creating..." : "Create Class"}
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    className="flex-1"
                    onClick={() => navigate("/teacher/classes")}
                    disabled={isSubmitting}
                  >
                    Cancel
                  </Button>
                </div>

              </form>
            </CardContent>
          </Card>
        </div>

        <div className="space-y-6">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle className="text-base">Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                  <BookOpen className="h-5 w-5 text-primary" />
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">What is a Class?</p>
                  <p className="text-sm font-medium">
                    A class is a specific section of a course. You can create multiple classes for the same course.
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="glass-card">
            <CardHeader>
              <CardTitle className="text-base">Tips</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm text-muted-foreground">
              <p>• Use descriptive names that clearly identify the class section</p>
              <p>• You can add students to the class after creation</p>
              <p>• Each class can have its own assignments and schedule</p>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
