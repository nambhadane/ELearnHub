import { useState, FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { BookOpen, ArrowLeft } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useToast } from "@/hooks/use-toast";
import { createCourse } from "@/services/api";

export default function CreateCourse() {
  const navigate = useNavigate();
  const { toast } = useToast();
  
  const [courseName, setCourseName] = useState("");
  const [subject, setSubject] = useState("");
  const [description, setDescription] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    if (!courseName || !courseName.trim()) {
      toast({
        title: "Validation Error",
        description: "Course name is required",
        variant: "destructive",
      });
      return;
    }

    try {
      setIsSubmitting(true);

      const result = await createCourse({
        name: courseName.trim(),
        subject: subject.trim() || undefined,
        description: description.trim() || undefined,
      });

      toast({
        title: "Course Created",
        description: `${result.course.name} has been created successfully.`,
      });

      // Navigate to create class page so teacher can create a class for this course
      navigate("/teacher/classes/create");
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to create course";
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
        <Button
          variant="ghost"
          size="icon"
          onClick={() => navigate("/teacher/classes/create")}
        >
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">Create New Course</h1>
          <p className="text-muted-foreground">
            Create a new course that you can use to create classes
          </p>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Course Information</CardTitle>
              <CardDescription>
                Fill in the details to create a new course
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-6">
                <div className="space-y-2">
                  <Label htmlFor="courseName">Course Name *</Label>
                  <Input
                    id="courseName"
                    placeholder="e.g., Web Development, Database Systems"
                    value={courseName}
                    onChange={(e) => setCourseName(e.target.value)}
                    required
                  />
                  <p className="text-sm text-muted-foreground">
                    Enter the name of your course
                  </p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="subject">Subject (Optional)</Label>
                  <Input
                    id="subject"
                    placeholder="e.g., Computer Science, Mathematics"
                    value={subject}
                    onChange={(e) => setSubject(e.target.value)}
                  />
                  <p className="text-sm text-muted-foreground">
                    The subject or category this course belongs to
                  </p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="description">Description (Optional)</Label>
                  <Textarea
                    id="description"
                    placeholder="Describe what students will learn in this course..."
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    rows={4}
                  />
                  <p className="text-sm text-muted-foreground">
                    Provide a brief description of the course content and objectives
                  </p>
                </div>

                <div className="flex gap-2 pt-4">
                  <Button
                    type="submit"
                    className="flex-1"
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? "Creating..." : "Create Course"}
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    className="flex-1"
                    onClick={() => navigate("/teacher/classes/create")}
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
                  <p className="text-sm text-muted-foreground">What is a Course?</p>
                  <p className="text-sm font-medium">
                    A course is a subject you teach. You can create multiple classes (sections) for the same course.
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
              <p>• Use clear, descriptive course names</p>
              <p>• Add a description to help students understand the course</p>
              <p>• After creating a course, you can create multiple class sections for it</p>
              <p>• You can add students to courses and classes later</p>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}

