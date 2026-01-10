import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { FileText, ArrowLeft, Clock, Loader2, RefreshCw } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useToast } from "@/hooks/use-toast";
import {
  AssignmentDTO,
  getAssignmentById,
  getMySubmission,
  submitAssignment,
  SubmissionDTO,
} from "@/services/api";

export default function AssignmentDetail() {
  const { assignmentId } = useParams<{ assignmentId: string }>();
  const navigate = useNavigate();
  const { toast } = useToast();

  const [assignment, setAssignment] = useState<AssignmentDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [submission, setSubmission] = useState<SubmissionDTO | null>(null);
  const [submissionLoading, setSubmissionLoading] = useState(true);
  const [content, setContent] = useState("");
  const [files, setFiles] = useState<FileList | null>(null);
  const [saving, setSaving] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  const loadSubmission = async () => {
    if (!assignmentId) return;
    try {
      setSubmissionLoading(true);
      const id = Number(assignmentId);
      const submissionData = await getMySubmission(id);
      setSubmission(submissionData);
      if (submissionData?.content) {
        setContent(submissionData.content);
      }
    } catch (err) {
      // If 404, no submission yet - that's fine
      if (err instanceof Error && err.message.includes('404')) {
        setSubmission(null);
      } else {
        console.error('Failed to load submission:', err);
      }
    } finally {
      setSubmissionLoading(false);
    }
  };

  useEffect(() => {
    const load = async () => {
      if (!assignmentId) {
        setLoading(false);
        setSubmissionLoading(false);
        return;
      }
      try {
        setLoading(true);
        setSubmissionLoading(true);

        const id = Number(assignmentId);
        const data = await getAssignmentById(id);
        setAssignment(data);

        // Load current student's submission (if any)
        await loadSubmission();
      } catch (err) {
        const message = err instanceof Error ? err.message : "Failed to load assignment";
        toast({
          title: "Error loading assignment",
          description: message,
          variant: "destructive",
        });
      } finally {
        setLoading(false);
        setSubmissionLoading(false);
      }
    };

    load();
  }, [assignmentId, toast]);

  const handleRefresh = async () => {
    setRefreshing(true);
    await loadSubmission();
    setRefreshing(false);
    toast({
      title: "Refreshed",
      description: "Submission status updated.",
    });
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full py-12">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (!assignment) {
    return (
      <div className="space-y-4">
        <Button variant="ghost" size="sm" onClick={() => navigate(-1)}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back
        </Button>
        <Card className="glass-card">
          <CardContent className="py-12 text-center text-muted-foreground">
            Assignment not found.
          </CardContent>
        </Card>
      </div>
    );
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!assignmentId) return;

    try {
      setSaving(true);
      const selectedFiles = files ? Array.from(files) : [];
      const saved = await submitAssignment(Number(assignmentId), content, selectedFiles);
      setSubmission(saved);

      toast({
        title: "Submission saved",
        description: "Your assignment submission has been uploaded successfully.",
      });
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to submit assignment";
      toast({
        title: "Submission failed",
        description: message,
        variant: "destructive",
      });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-4">
      <Button variant="ghost" size="sm" onClick={() => navigate(-1)}>
        <ArrowLeft className="mr-2 h-4 w-4" />
        Back to Assignments
      </Button>

      <Card className="glass-card">
        <CardHeader>
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
              <FileText className="h-5 w-5 text-primary" />
            </div>
            <div>
              <CardTitle>{assignment.title}</CardTitle>
              <CardDescription>
                {assignment.dueDate && (
                  <span className="inline-flex items-center gap-1 text-sm">
                    <Clock className="h-4 w-4" />
                    Due: {new Date(assignment.dueDate).toLocaleString()}
                  </span>
                )}
              </CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-sm text-muted-foreground whitespace-pre-line">
            {assignment.description}
          </p>

          {assignment.maxGrade && (
            <p className="text-sm">
              <strong>Maximum grade:</strong> {assignment.maxGrade}
            </p>
          )}
        </CardContent>
      </Card>

      <Card className="glass-card">
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Your Submission</CardTitle>
              <CardDescription>
                {submissionLoading
                  ? "Loading submission status..."
                  : submission
                  ? submission.grade != null
                    ? `Status: graded • Score: ${submission.grade}/100${
                        submission.feedback ? " • Feedback available" : ""
                      }`
                    : "Status: submitted • Waiting for grading"
                  : "Status: not submitted yet"}
              </CardDescription>
            </div>
            {submission && (
              <Button
                variant="ghost"
                size="sm"
                onClick={handleRefresh}
                disabled={refreshing}
              >
                <RefreshCw className={`h-4 w-4 mr-2 ${refreshing ? 'animate-spin' : ''}`} />
                Refresh
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent>
          {submission?.feedback && (
            <div className="mb-4 rounded-md bg-muted p-3 text-sm">
              <div className="font-semibold mb-1">Teacher feedback</div>
              <p className="whitespace-pre-line">{submission.feedback}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1">
              <label className="text-sm font-medium" htmlFor="content">
                Your answer (optional)
              </label>
              <textarea
                id="content"
                className="w-full min-h-[120px] rounded-md border bg-background px-3 py-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                placeholder="Write your answer here or attach files below..."
                value={content}
                onChange={(e) => setContent(e.target.value)}
              />
            </div>

            <div className="space-y-1">
              <label className="text-sm font-medium" htmlFor="files">
                Attach files (optional)
              </label>
              <input
                id="files"
                type="file"
                multiple
                className="block w-full text-sm text-muted-foreground file:mr-3 file:rounded-md file:border file:bg-background file:px-3 file:py-1.5 file:text-sm file:font-medium hover:file:bg-accent"
                onChange={(e) => setFiles(e.target.files)}
              />
              {submission?.filePath && (
                <p className="mt-1 text-xs text-muted-foreground">
                  Previously uploaded files: {submission.filePath}
                </p>
              )}
            </div>

            <Button type="submit" disabled={saving}>
              {saving && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              {submission ? "Update submission" : "Submit assignment"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
