import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, FileText, Calendar, User, BookOpen, Clock, CheckCircle, XCircle } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/hooks/use-toast";
import { api } from "@/services/api";

interface AssignmentDetail {
  id: number;
  title: string;
  description: string;
  dueDate: string;
  maxGrade: number;
  courseId: number;
  courseName?: string;
  teacherId?: number;
  teacherName?: string;
  status: string;
  weight?: number;
  allowLateSubmission: boolean;
  latePenalty?: number;
  additionalInstructions?: string;
  createdAt: string;
  updatedAt?: string;
  type: string;
}

export default function AssignmentDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [assignment, setAssignment] = useState<AssignmentDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      fetchAssignmentDetail();
    }
  }, [id]);

  const fetchAssignmentDetail = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/admin/assignments/${id}`);
      setAssignment(response.data);
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to load assignment details",
        variant: "destructive",
      });
      navigate("/admin/assignments");
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return "N/A";
    try {
      return new Date(dateString).toLocaleDateString("en-US", {
        year: "numeric",
        month: "long",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
    } catch {
      return "Invalid Date";
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status?.toLowerCase()) {
      case "published":
        return <Badge className="bg-success">Published</Badge>;
      case "draft":
        return <Badge variant="secondary">Draft</Badge>;
      default:
        return <Badge variant="outline">{status}</Badge>;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (!assignment) {
    return (
      <div className="text-center py-12">
        <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
        <p className="text-muted-foreground">Assignment not found</p>
        <Button onClick={() => navigate("/admin/assignments")} className="mt-4">
          Back to Assignments
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button
          variant="outline"
          size="sm"
          onClick={() => navigate("/admin/assignments")}
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Assignments
        </Button>
        <div className="space-y-1">
          <h1 className="text-3xl font-bold tracking-tight">{assignment.title}</h1>
          <p className="text-muted-foreground">Assignment Details</p>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <FileText className="h-5 w-5" />
                Assignment Information
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              <div>
                <h3 className="font-semibold mb-3 text-lg">Description</h3>
                <div className="bg-muted/50 rounded-lg p-4">
                  <p className="text-foreground whitespace-pre-wrap leading-relaxed">
                    {assignment.description}
                  </p>
                </div>
              </div>

              {assignment.additionalInstructions && (
                <div>
                  <h3 className="font-semibold mb-3 text-lg">Additional Instructions</h3>
                  <div className="bg-muted/50 rounded-lg p-4">
                    <p className="text-foreground whitespace-pre-wrap leading-relaxed">
                      {assignment.additionalInstructions}
                    </p>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Assignment Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium">Status</span>
                {getStatusBadge(assignment.status)}
              </div>

              <div className="flex items-center justify-between">
                <span className="text-sm font-medium">Max Grade</span>
                <span className="font-semibold">{assignment.maxGrade} points</span>
              </div>

              {assignment.weight && (
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">Weight</span>
                  <span className="font-semibold">{assignment.weight}%</span>
                </div>
              )}

              <div className="flex items-center justify-between">
                <span className="text-sm font-medium">Late Submission</span>
                {assignment.allowLateSubmission ? (
                  <CheckCircle className="h-5 w-5 text-success" />
                ) : (
                  <XCircle className="h-5 w-5 text-destructive" />
                )}
              </div>

              {assignment.latePenalty && (
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">Late Penalty</span>
                  <span className="font-semibold">{assignment.latePenalty}%</span>
                </div>
              )}
            </CardContent>
          </Card>

          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Course Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center gap-2">
                <BookOpen className="h-4 w-4 text-muted-foreground" />
                <span className="font-medium">{assignment.courseName || "Unknown Course"}</span>
              </div>

              <div className="flex items-center gap-2">
                <User className="h-4 w-4 text-muted-foreground" />
                <span className="font-medium">{assignment.teacherName || "Unknown Teacher"}</span>
              </div>
            </CardContent>
          </Card>

          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Timeline</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center gap-2">
                <Calendar className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">Due Date</p>
                  <p className="text-sm text-muted-foreground">
                    {formatDate(assignment.dueDate)}
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <Clock className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">Created</p>
                  <p className="text-sm text-muted-foreground">
                    {formatDate(assignment.createdAt)}
                  </p>
                </div>
              </div>

              {assignment.updatedAt && (
                <div className="flex items-center gap-2">
                  <Clock className="h-4 w-4 text-muted-foreground" />
                  <div>
                    <p className="text-sm font-medium">Last Updated</p>
                    <p className="text-sm text-muted-foreground">
                      {formatDate(assignment.updatedAt)}
                    </p>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}