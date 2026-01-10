import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FileText, Clock, CheckCircle2, AlertCircle, Loader2 } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useToast } from "@/hooks/use-toast";
import { getMyAssignments, StudentAssignmentDTO } from "@/services/api";

export default function Assignments() {
  const navigate = useNavigate();
  const { toast } = useToast();
  const [assignments, setAssignments] = useState<StudentAssignmentDTO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadAssignments = async () => {
      try {
        setLoading(true);
        console.log("Loading assignments...");
        const data = await getMyAssignments();
        console.log("Assignments loaded:", data);
        setAssignments(data || []);
      } catch (err) {
        console.error("Error loading assignments:", err);
        const message = err instanceof Error ? err.message : "Failed to load assignments";
        toast({
          title: "Error",
          description: message,
          variant: "destructive",
        });
        setAssignments([]);
      } finally {
        setLoading(false);
      }
    };

    loadAssignments();
  }, [toast]);

  const pendingCount = assignments.filter((a) => a.status === "pending").length;
  const submittedCount = assignments.filter((a) => a.status === "submitted").length;
  const gradedCount = assignments.filter((a) => a.status === "graded").length;

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "pending":
        return <Badge variant="secondary">Pending</Badge>;
      case "submitted":
        return <Badge className="bg-primary text-primary-foreground">Submitted</Badge>;
      case "graded":
        return <Badge className="bg-success text-success-foreground">Graded</Badge>;
      default:
        return null;
    }
  };

  const getPriorityBadge = (assignment: StudentAssignmentDTO) => {
    if (!assignment.dueDate) return null;
    
    const due = new Date(assignment.dueDate);
    const now = new Date();
    const diffTime = due.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays < 0) {
      return <Badge variant="destructive">Overdue</Badge>;
    } else if (diffDays <= 3) {
      return <Badge variant="default">Due Soon</Badge>;
    } else {
      return <Badge variant="outline">Upcoming</Badge>;
    }
  };

  const renderAssignmentCard = (assignment: StudentAssignmentDTO) => (
    <Card key={assignment.id} className="glass-card hover:shadow-lg transition-all">
      <CardHeader>
        <div className="flex items-start justify-between">
          <div className="space-y-1 flex-1">
            <CardTitle className="text-lg">{assignment.title}</CardTitle>
            <CardDescription>{assignment.className}</CardDescription>
          </div>
          <div className="flex gap-2">
            {getStatusBadge(assignment.status)}
            {getPriorityBadge(assignment)}
          </div>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        <p className="text-sm text-muted-foreground">{assignment.description}</p>

        <div className="flex items-center justify-between text-sm">
          <div className="flex items-center gap-4">
            <span className="flex items-center gap-1 text-muted-foreground">
              <Clock className="h-4 w-4" />
              Due: {assignment.dueDate ? new Date(assignment.dueDate).toLocaleDateString() : "N/A"}
            </span>
            <span className="flex items-center gap-1 text-muted-foreground">
              <FileText className="h-4 w-4" />
              {assignment.maxGrade} points
            </span>
          </div>
          {assignment.grade !== undefined && assignment.grade !== null && (
            <span className="font-semibold text-success">
              Grade: {assignment.grade}/{assignment.maxGrade}
            </span>
          )}
        </div>

        {assignment.feedback && (
          <div className="rounded-md bg-muted p-3 text-sm">
            <div className="font-semibold mb-1">Teacher Feedback:</div>
            <p className="whitespace-pre-line">{assignment.feedback}</p>
          </div>
        )}

        <div className="flex gap-2 pt-2">
          {assignment.status === "pending" ? (
            <Button 
              className="w-full" 
              onClick={() => navigate(`/student/assignments/${assignment.id}`)}
            >
              Submit Assignment
            </Button>
          ) : assignment.status === "submitted" ? (
            <Button 
              variant="outline" 
              className="w-full" 
              onClick={() => navigate(`/student/assignments/${assignment.id}`)}
            >
              <CheckCircle2 className="mr-2 h-4 w-4" />
              View Submission
            </Button>
          ) : (
            <Button 
              variant="outline" 
              className="w-full"
              onClick={() => navigate(`/student/assignments/${assignment.id}`)}
            >
              View Feedback
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  );

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
        <h1 className="text-3xl font-bold tracking-tight">Assignments</h1>
        <p className="text-muted-foreground">
          Manage and submit your course assignments
        </p>
      </div>

      {/* Stats Overview */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card className="glass-card">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Pending
            </CardTitle>
            <AlertCircle className="h-5 w-5 text-destructive" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{pendingCount}</div>
          </CardContent>
        </Card>
        <Card className="glass-card">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Submitted
            </CardTitle>
            <FileText className="h-5 w-5 text-primary" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{submittedCount}</div>
          </CardContent>
        </Card>
        <Card className="glass-card">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Graded
            </CardTitle>
            <CheckCircle2 className="h-5 w-5 text-success" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{gradedCount}</div>
          </CardContent>
        </Card>
      </div>

      {/* Assignments Tabs */}
      {assignments.length === 0 && !loading ? (
        <Card className="glass-card">
          <CardContent className="py-12 text-center">
            <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
            <p className="text-muted-foreground mb-2">No assignments available yet.</p>
            <p className="text-sm text-muted-foreground">
              You may not be enrolled in any classes yet, or your classes don't have assignments.
            </p>
          </CardContent>
        </Card>
      ) : assignments.length > 0 ? (
        <Tabs defaultValue="all" className="space-y-4">
          <TabsList>
            <TabsTrigger value="all">All Assignments</TabsTrigger>
            <TabsTrigger value="pending">Pending</TabsTrigger>
            <TabsTrigger value="submitted">Submitted</TabsTrigger>
            <TabsTrigger value="graded">Graded</TabsTrigger>
          </TabsList>

          <TabsContent value="all" className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              {assignments.map(renderAssignmentCard)}
            </div>
          </TabsContent>

          <TabsContent value="pending" className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              {assignments
                .filter((a) => a.status === "pending")
                .map(renderAssignmentCard)}
            </div>
          </TabsContent>

          <TabsContent value="submitted" className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              {assignments
                .filter((a) => a.status === "submitted")
                .map(renderAssignmentCard)}
            </div>
          </TabsContent>

          <TabsContent value="graded" className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              {assignments
                .filter((a) => a.status === "graded")
                .map(renderAssignmentCard)}
            </div>
          </TabsContent>
        </Tabs>
      ) : null}
    </div>
  );
}
