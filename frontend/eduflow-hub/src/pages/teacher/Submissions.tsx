import { useEffect, useMemo, useState } from "react";
import { FileText, Download, CheckCircle2, Clock, AlertCircle } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useToast } from "@/hooks/use-toast";
import { useSearchParams } from "react-router-dom";
import {
  getAssignmentById,
  getSubmissionsByAssignment,
  gradeSubmission,
  downloadSubmissionFile,
  AssignmentDTO,
  SubmissionDTO,
  getTeacherProfile,
  getClassesByTeacher,
  getAssignmentsByClass,
  ClassDTO,
  UserProfile,
} from "@/services/api";

export default function Submissions() {
  const { toast } = useToast();
  const [searchParams] = useSearchParams();
  const assignmentIdParam = searchParams.get("assignmentId");
  const assignmentId = assignmentIdParam ? Number(assignmentIdParam) : null;

  const [assignment, setAssignment] = useState<AssignmentDTO | null>(null);
  const [submissions, setSubmissions] = useState<(SubmissionDTO & { assignmentTitle?: string; className?: string })[]>([]);
  const [loading, setLoading] = useState(true);
  const [gradingId, setGradingId] = useState<number | null>(null);
  const [gradeValue, setGradeValue] = useState<string>("");
  const [feedbackValue, setFeedbackValue] = useState<string>("");

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);

        if (assignmentId) {
          // View submissions for a specific assignment (when ?assignmentId=... is provided)
          const [assignmentData, submissionsData] = await Promise.all([
            getAssignmentById(assignmentId),
            getSubmissionsByAssignment(assignmentId),
          ]);
          setAssignment(assignmentData);
          setSubmissions(
            submissionsData.map((s) => ({
              ...s,
              assignmentTitle: assignmentData.title,
            }))
          );
        } else {
          // Global view: load all submissions for all assignments of this teacher
          const teacherProfile: UserProfile = await getTeacherProfile();
          const classes: ClassDTO[] = await getClassesByTeacher(teacherProfile.id);

          // Load assignments for each class
          const assignmentsByClass = await Promise.all(
            classes.map(async (cls) => {
              try {
                const assignments = await getAssignmentsByClass(cls.id);
                return assignments.map((a) => ({
                  assignment: a,
                  className: cls.name,
                }));
              } catch (err) {
                console.error(`Failed to fetch assignments for class ${cls.id}`, err);
                return [] as { assignment: AssignmentDTO; className: string }[];
              }
            })
          );

          const allAssignments = assignmentsByClass.flat();

          // Load submissions for each assignment
          const submissionsByAssignment = await Promise.all(
            allAssignments.map(async ({ assignment, className }) => {
              if (!assignment.id) return [] as (SubmissionDTO & { assignmentTitle?: string; className?: string })[];
              try {
                const subs = await getSubmissionsByAssignment(assignment.id);
                return subs.map((s) => ({
                  ...s,
                  assignmentTitle: assignment.title,
                  className,
                }));
              } catch (err) {
                console.error(`Failed to fetch submissions for assignment ${assignment.id}`, err);
                return [] as (SubmissionDTO & { assignmentTitle?: string; className?: string })[];
              }
            })
          );

          setAssignment(null);
          setSubmissions(submissionsByAssignment.flat());
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : "Failed to load submissions";
        toast({
          title: "Error",
          description: message,
          variant: "destructive",
        });
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [assignmentId, toast]);

  const pendingCount = useMemo(
    () => submissions.filter((s) => s.grade == null).length,
    [submissions]
  );
  const gradedCount = useMemo(
    () => submissions.filter((s) => s.grade != null).length,
    [submissions]
  );

  const getStatusBadge = (s: SubmissionDTO) => {
    if (s.grade != null) {
      return <Badge className="bg-success text-success-foreground">Graded</Badge>;
    }
    return <Badge variant="secondary">Pending Review</Badge>;
  };

  const getStatusIcon = (s: SubmissionDTO) => {
    if (s.grade != null) {
      return <CheckCircle2 className="h-5 w-5 text-success" />;
    }
    return <Clock className="h-5 w-5 text-accent" />;
  };

  const handleStartGrading = (submission: SubmissionDTO) => {
    setGradingId(submission.id!);
    setGradeValue(submission.grade != null ? String(submission.grade) : "");
    setFeedbackValue(submission.feedback || "");
  };

  const handleSubmitGrade = async () => {
    if (!gradingId) return;
    const gradeNum = Number(gradeValue);
    if (Number.isNaN(gradeNum) || gradeNum < 0 || gradeNum > 100) {
      toast({
        title: "Invalid grade",
        description: "Please enter a grade between 0 and 100.",
        variant: "destructive",
      });
      return;
    }

    try {
      const updated = await gradeSubmission(gradingId, gradeNum, feedbackValue || undefined);
      setSubmissions((prev) =>
        prev.map((s) => (s.id === updated.id ? { ...s, ...updated } : s))
      );
      setGradingId(null);
      setGradeValue("");
      setFeedbackValue("");
      toast({
        title: "Grade saved",
        description: "The submission has been graded successfully.",
      });
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to grade submission";
      toast({
        title: "Error",
        description: message,
        variant: "destructive",
      });
    }
  };

  const handleDownload = async (submission: SubmissionDTO) => {
    if (!submission.id || (!submission.filePath && !submission.content)) {
      toast({
        title: "No content available",
        description: "This submission does not have any files or text content to download.",
        variant: "destructive",
      });
      return;
    }

    try {
      // For file submissions, extract filename from filePath
      if (submission.filePath) {
        const firstPath = submission.filePath.split(',')[0].trim();
        const filename = firstPath.split(/[\\/]/).pop() || 'submission';
        await downloadSubmissionFile(submission.id, filename);
      } else {
        // For text submissions, use a default filename
        await downloadSubmissionFile(submission.id, 'submission.txt');
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to download file";
      toast({
        title: "Download failed",
        description: message,
        variant: "destructive",
      });
    }
  };

  const selectedSubmission = submissions.find((s) => s.id === gradingId) || null;

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">Student Submissions</h1>
        <p className="text-muted-foreground">
          {assignmentId && assignment
            ? `Review and grade submissions for: ${assignment.title}`
            : "Review and grade student assignment submissions across all your classes"}
        </p>
      </div>

      {/* Stats */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card className="glass-card">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Total Submissions
            </CardTitle>
            <FileText className="h-5 w-5 text-primary" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{submissions.length}</div>
          </CardContent>
        </Card>

        <Card className="glass-card">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Pending Review
            </CardTitle>
            <Clock className="h-5 w-5 text-accent" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{pendingCount}</div>
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

      {/* Submissions Table */}
      <Card className="glass-card">
        <CardHeader>
          <CardTitle>All Submissions</CardTitle>
          <CardDescription>
            {loading
              ? "Loading submissions..."
              : submissions.length === 0
              ? "No submissions yet for this assignment."
              : "Manage student assignment submissions"}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs defaultValue="all">
            <TabsList>
              <TabsTrigger value="all">All</TabsTrigger>
              <TabsTrigger value="pending">Pending</TabsTrigger>
              <TabsTrigger value="graded">Graded</TabsTrigger>
            </TabsList>

            <TabsContent value="all" className="mt-4">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Student</TableHead>
                    <TableHead>Assignment</TableHead>
                    <TableHead>Submitted</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Grade</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {submissions.map((submission) => (
                    <TableRow key={submission.id}>
                      <TableCell className="font-medium">
                        {submission.studentName || submission.studentId}
                      </TableCell>
                      <TableCell>
                        {submission.assignmentTitle ||
                          assignment?.title ||
                          "-"}
                        {submission.className && (
                          <span className="ml-1 text-xs text-muted-foreground">
                            ({submission.className})
                          </span>
                        )}
                      </TableCell>
                      <TableCell>
                        {submission.submittedAt
                          ? new Date(submission.submittedAt).toLocaleString()
                          : "-"}
                      </TableCell>
                      <TableCell>{getStatusBadge(submission)}</TableCell>
                      <TableCell>
                        {submission.grade != null ? (
                          <span className="font-semibold">{submission.grade}/100</span>
                        ) : (
                          <span className="text-muted-foreground">-</span>
                        )}
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-2">
                          <Button 
                            size="sm" 
                            variant="outline" 
                            disabled={!submission.filePath && !submission.content}
                            onClick={() => handleDownload(submission)}
                          >
                            <Download className="mr-2 h-4 w-4" />
                            Download
                          </Button>
                          <Button size="sm" onClick={() => handleStartGrading(submission)}>
                            Grade
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TabsContent>

            <TabsContent value="pending" className="mt-4">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Student</TableHead>
                    <TableHead>Assignment</TableHead>
                    <TableHead>Submitted</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {submissions
                    .filter((s) => s.grade == null)
                    .map((submission) => (
                      <TableRow key={submission.id}>
                        <TableCell className="font-medium">
                          {submission.studentName || submission.studentId}
                        </TableCell>
                        <TableCell>
                          {submission.assignmentTitle ||
                            assignment?.title ||
                            "-"}
                          {submission.className && (
                            <span className="ml-1 text-xs text-muted-foreground">
                              ({submission.className})
                            </span>
                          )}
                        </TableCell>
                        <TableCell>
                          {submission.submittedAt
                            ? new Date(submission.submittedAt).toLocaleString()
                            : "-"}
                        </TableCell>
                        <TableCell>{getStatusBadge(submission)}</TableCell>
                        <TableCell className="text-right">
                          <div className="flex justify-end gap-2">
                            <Button 
                              size="sm" 
                              variant="outline" 
                              disabled={!submission.filePath && !submission.content}
                              onClick={() => handleDownload(submission)}
                            >
                              <Download className="mr-2 h-4 w-4" />
                              Download
                            </Button>
                            <Button size="sm" onClick={() => handleStartGrading(submission)}>
                              Grade
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                </TableBody>
              </Table>
            </TabsContent>

            <TabsContent value="graded" className="mt-4">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Student</TableHead>
                    <TableHead>Assignment</TableHead>
                    <TableHead>Submitted</TableHead>
                    <TableHead>Grade</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {submissions
                    .filter((s) => s.grade != null)
                    .map((submission) => (
                      <TableRow key={submission.id}>
                        <TableCell className="font-medium">
                          {submission.studentName || submission.studentId}
                        </TableCell>
                        <TableCell>
                          {submission.assignmentTitle ||
                            assignment?.title ||
                            "-"}
                          {submission.className && (
                            <span className="ml-1 text-xs text-muted-foreground">
                              ({submission.className})
                            </span>
                          )}
                        </TableCell>
                        <TableCell>
                          {submission.submittedAt
                            ? new Date(submission.submittedAt).toLocaleString()
                            : "-"}
                        </TableCell>
                        <TableCell>
                          <span className="font-semibold text-success">
                            {submission.grade}/100
                          </span>
                        </TableCell>
                        <TableCell className="text-right">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleStartGrading(submission)}
                          >
                            View / Edit Grade
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                </TableBody>
              </Table>
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>

      {/* Grading Form */}
      {selectedSubmission && (
        <Card className="glass-card">
          <CardHeader>
            <CardTitle>Grade Submission</CardTitle>
            <CardDescription>
              {selectedSubmission.studentName || selectedSubmission.studentId} -{" "}
              {assignment?.title ?? "Assignment"}
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="grade">Grade (out of 100)</Label>
                <Input
                  id="grade"
                  type="number"
                  placeholder="95"
                  value={gradeValue}
                  onChange={(e) => setGradeValue(e.target.value)}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="status">Status</Label>
                <Input
                  id="status"
                  value={selectedSubmission.grade != null ? "Graded" : "Pending"}
                  disabled
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="feedback">Feedback</Label>
              <Textarea
                id="feedback"
                placeholder="Provide detailed feedback for the student..."
                rows={6}
                value={feedbackValue}
                onChange={(e) => setFeedbackValue(e.target.value)}
              />
            </div>

            <div className="flex gap-2">
              <Button className="flex-1" onClick={handleSubmitGrade}>
                Submit Grade
              </Button>
              <Button
                variant="outline"
                className="flex-1"
                onClick={() => {
                  setGradingId(null);
                  setGradeValue("");
                  setFeedbackValue("");
                }}
              >
                Cancel
              </Button>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
