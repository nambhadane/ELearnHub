import { useState, FormEvent, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { FileText, Calendar, Clock, Users, Loader2 } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { useToast } from "@/hooks/use-toast";
import { createAssignment, getClassesByTeacher, getTeacherProfile, ClassDTO } from "@/services/api";

export default function CreateAssignment() {
  const navigate = useNavigate();
  const { toast } = useToast();

  // Form state
  const [classId, setClassId] = useState<string>("");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [dueDate, setDueDate] = useState("");
  const [dueTime, setDueTime] = useState("");
  const [points, setPoints] = useState("");
  const [weight, setWeight] = useState("");
  const [allowLateSubmission, setAllowLateSubmission] = useState(false);
  const [latePenalty, setLatePenalty] = useState("");
  const [additionalInstructions, setAdditionalInstructions] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Classes data
  const [classes, setClasses] = useState<ClassDTO[]>([]);
  const [loadingClasses, setLoadingClasses] = useState(true);
  const [selectedClass, setSelectedClass] = useState<ClassDTO | null>(null);

  // Fetch classes
  useEffect(() => {
    const fetchClasses = async () => {
      try {
        setLoadingClasses(true);
        const profile = await getTeacherProfile();
        const teacherClasses = await getClassesByTeacher(profile.id);
        setClasses(teacherClasses);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Failed to load classes";
        toast({
          title: "Error",
          description: message,
          variant: "destructive",
        });
        setClasses([]);
      } finally {
        setLoadingClasses(false);
      }
    };

    fetchClasses();
  }, [toast]);

  // Update selected class when classId changes
  useEffect(() => {
    if (classId) {
      const found = classes.find((c) => c.id.toString() === classId);
      setSelectedClass(found || null);
    } else {
      setSelectedClass(null);
    }
  }, [classId, classes]);

  const handleSubmit = async (e: FormEvent, status: "published" | "draft" = "published") => {
    e.preventDefault();

    // Validation
    if (!classId) {
      toast({
        title: "Validation Error",
        description: "Please select a class",
        variant: "destructive",
      });
      return;
    }

    if (!title.trim()) {
      toast({
        title: "Validation Error",
        description: "Assignment title is required",
        variant: "destructive",
      });
      return;
    }

    if (!description.trim()) {
      toast({
        title: "Validation Error",
        description: "Description is required",
        variant: "destructive",
      });
      return;
    }

    if (!dueDate || !dueTime) {
      toast({
        title: "Validation Error",
        description: "Due date and time are required",
        variant: "destructive",
      });
      return;
    }

    if (!points || parseFloat(points) <= 0) {
      toast({
        title: "Validation Error",
        description: "Total points must be greater than 0",
        variant: "destructive",
      });
      return;
    }

    // Combine date and time into ISO 8601 format
    const dateTimeString = `${dueDate}T${dueTime}:00`;
    const dueDateTime = new Date(dateTimeString).toISOString();

    try {
      setIsSubmitting(true);

      const assignmentData = {
        classId: parseInt(classId),
        title: title.trim(),
        description: description.trim(),
        dueDate: dueDateTime,
        maxGrade: parseFloat(points),
        weight: weight ? parseFloat(weight) : undefined,
        allowLateSubmission: allowLateSubmission,
        latePenalty: allowLateSubmission && latePenalty ? parseFloat(latePenalty) : undefined,
        additionalInstructions: additionalInstructions.trim() || undefined,
        status: status,
      };

      await createAssignment(assignmentData);

      toast({
        title: "Success",
        description: `Assignment ${status === "draft" ? "saved as draft" : "created"} successfully!`,
      });

      // Navigate to classes page or assignments list
      navigate("/teacher/classes");
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to create assignment";
      toast({
        title: "Error",
        description: message,
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  // Format date for display
  const formatDate = (dateString: string) => {
    if (!dateString) return "Not set";
    try {
      return new Date(dateString).toLocaleDateString();
    } catch {
      return "Not set";
    }
  };

  // Format time for display
  const formatTime = (timeString: string) => {
    if (!timeString) return "Not set";
    return timeString;
  };

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">Create Assignment</h1>
        <p className="text-muted-foreground">
          Create a new assignment for your students
        </p>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Assignment Details</CardTitle>
              <CardDescription>Fill in the information about the assignment</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={(e) => handleSubmit(e, "published")} className="space-y-6">
                <div className="space-y-2">
                  <Label htmlFor="class-select">Select Class *</Label>
                  <Select
                    value={classId}
                    onValueChange={setClassId}
                    disabled={loadingClasses}
                  >
                    <SelectTrigger id="class-select">
                      <SelectValue placeholder={loadingClasses ? "Loading classes..." : "Choose a class"} />
                    </SelectTrigger>
                    <SelectContent>
                      {classes.length === 0 && !loadingClasses ? (
                        <SelectItem value="no-classes" disabled>
                          No classes available
                        </SelectItem>
                      ) : (
                        classes.map((classItem) => (
                          <SelectItem key={classItem.id} value={classItem.id.toString()}>
                            {classItem.name}
                          </SelectItem>
                        ))
                      )}
                    </SelectContent>
                  </Select>
                  {classes.length === 0 && !loadingClasses && (
                    <p className="text-sm text-muted-foreground">
                      You need to create a class first.{" "}
                      <Button
                        type="button"
                        variant="link"
                        className="p-0 h-auto"
                        onClick={() => navigate("/teacher/classes/create")}
                      >
                        Create a class
                      </Button>
                    </p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="title">Assignment Title *</Label>
                  <Input
                    id="title"
                    placeholder="e.g., React Advanced Patterns"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="description">Description *</Label>
                  <Textarea
                    id="description"
                    placeholder="Provide detailed instructions for the assignment..."
                    rows={6}
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    required
                  />
                </div>

                <div className="grid gap-4 md:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="due-date">Due Date *</Label>
                    <Input
                      id="due-date"
                      type="date"
                      value={dueDate}
                      onChange={(e) => setDueDate(e.target.value)}
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="due-time">Due Time *</Label>
                    <Input
                      id="due-time"
                      type="time"
                      value={dueTime}
                      onChange={(e) => setDueTime(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <div className="grid gap-4 md:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="points">Total Points *</Label>
                    <Input
                      id="points"
                      type="number"
                      placeholder="100"
                      min="1"
                      value={points}
                      onChange={(e) => setPoints(e.target.value)}
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="weight">Weight (%)</Label>
                    <Input
                      id="weight"
                      type="number"
                      placeholder="20"
                      min="0"
                      max="100"
                      value={weight}
                      onChange={(e) => setWeight(e.target.value)}
                    />
                  </div>
                </div>

                <div className="space-y-4">
                  <div className="flex items-center justify-between rounded-lg border border-border bg-muted p-4">
                    <div className="space-y-1">
                      <Label htmlFor="late-submission">Allow Late Submission</Label>
                      <p className="text-sm text-muted-foreground">
                        Students can submit after the due date
                      </p>
                    </div>
                    <Switch
                      id="late-submission"
                      checked={allowLateSubmission}
                      onCheckedChange={setAllowLateSubmission}
                    />
                  </div>

                  {allowLateSubmission && (
                    <div className="space-y-2">
                      <Label htmlFor="late-penalty">Late Penalty (%)</Label>
                      <Input
                        id="late-penalty"
                        type="number"
                        placeholder="10"
                        min="0"
                        max="100"
                        value={latePenalty}
                        onChange={(e) => setLatePenalty(e.target.value)}
                      />
                    </div>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="instructions">Additional Instructions</Label>
                  <Textarea
                    id="instructions"
                    placeholder="Any additional notes or requirements..."
                    rows={4}
                    value={additionalInstructions}
                    onChange={(e) => setAdditionalInstructions(e.target.value)}
                  />
                </div>

                <div className="flex gap-2 pt-4">
                  <Button
                    type="submit"
                    className="flex-1"
                    disabled={isSubmitting || classes.length === 0}
                  >
                    {isSubmitting ? "Creating..." : "Create Assignment"}
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    className="flex-1"
                    disabled={isSubmitting || classes.length === 0}
                    onClick={(e) => handleSubmit(e, "draft")}
                  >
                    {isSubmitting ? "Saving..." : "Save as Draft"}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </div>

        <div className="space-y-6">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle className="text-base">Assignment Summary</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                  <FileText className="h-5 w-5 text-primary" />
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Type</p>
                  <p className="font-medium">Assignment</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-accent/10">
                  <Calendar className="h-5 w-5 text-accent" />
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Due Date</p>
                  <p className="font-medium">{formatDate(dueDate)}</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-success/10">
                  <Clock className="h-5 w-5 text-success" />
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Time</p>
                  <p className="font-medium">{formatTime(dueTime)}</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-purple-500/10">
                  <Users className="h-5 w-5 text-purple-500" />
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Class</p>
                  <p className="font-medium">{selectedClass?.name || "Select class first"}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="glass-card">
            <CardHeader>
              <CardTitle className="text-base">Tips</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm text-muted-foreground">
              <p>• Be clear and specific in your instructions</p>
              <p>• Set realistic deadlines for students</p>
              <p>• Include grading rubric if applicable</p>
              <p>• Consider allowing late submissions with penalty</p>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
