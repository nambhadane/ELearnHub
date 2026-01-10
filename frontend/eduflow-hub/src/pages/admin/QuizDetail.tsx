import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, HelpCircle, Calendar, User, BookOpen, Clock, CheckCircle, XCircle, RotateCcw } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/hooks/use-toast";
import { api } from "@/services/api";

interface QuizDetail {
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
  timeLimit?: number;
  allowRetakes: boolean;
  maxAttempts?: number;
  showResults?: boolean;
  shuffleQuestions?: boolean;
  createdAt: string;
  updatedAt?: string;
  type: string;
}

export default function QuizDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [quiz, setQuiz] = useState<QuizDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      fetchQuizDetail();
    }
  }, [id]);

  const fetchQuizDetail = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/admin/quizzes/${id}`);
      setQuiz(response.data);
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to load quiz details",
        variant: "destructive",
      });
      navigate("/admin/quizzes");
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

  const formatTimeLimit = (minutes: number | undefined) => {
    if (!minutes) return "No limit";
    if (minutes < 60) return `${minutes} minutes`;
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return mins > 0 ? `${hours} hour${hours > 1 ? 's' : ''} ${mins} minutes` : `${hours} hour${hours > 1 ? 's' : ''}`;
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

  if (!quiz) {
    return (
      <div className="text-center py-12">
        <HelpCircle className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
        <p className="text-muted-foreground">Quiz not found</p>
        <Button onClick={() => navigate("/admin/quizzes")} className="mt-4">
          Back to Quizzes
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
          onClick={() => navigate("/admin/quizzes")}
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Quizzes
        </Button>
        <div className="space-y-1">
          <h1 className="text-3xl font-bold tracking-tight">{quiz.title}</h1>
          <p className="text-muted-foreground">Quiz Details</p>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <HelpCircle className="h-5 w-5" />
                Quiz Information
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              <div>
                <h3 className="font-semibold mb-3 text-lg">Description</h3>
                <div className="bg-muted/50 rounded-lg p-4">
                  <p className="text-foreground whitespace-pre-wrap leading-relaxed">
                    {quiz.description}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Quiz Settings</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="bg-muted/30 rounded-lg p-4 space-y-3">
                  <div className="flex items-center gap-3">
                    <Clock className="h-5 w-5 text-primary" />
                    <div>
                      <p className="font-medium">Time Limit</p>
                      <p className="text-sm text-muted-foreground">
                        {formatTimeLimit(quiz.timeLimit)}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="bg-muted/30 rounded-lg p-4 space-y-3">
                  <div className="flex items-center gap-3">
                    <RotateCcw className="h-5 w-5 text-primary" />
                    <div>
                      <p className="font-medium">Retakes</p>
                      <p className="text-sm text-muted-foreground">
                        {quiz.allowRetakes ? "Allowed" : "Not allowed"}
                      </p>
                    </div>
                  </div>
                </div>

                {quiz.maxAttempts && (
                  <div className="bg-muted/30 rounded-lg p-4 space-y-3">
                    <div className="flex items-center gap-3">
                      <HelpCircle className="h-5 w-5 text-primary" />
                      <div>
                        <p className="font-medium">Max Attempts</p>
                        <p className="text-sm text-muted-foreground">
                          {quiz.maxAttempts}
                        </p>
                      </div>
                    </div>
                  </div>
                )}

                <div className="bg-muted/30 rounded-lg p-4 space-y-3">
                  <div className="flex items-center gap-3">
                    {quiz.showResults ? (
                      <CheckCircle className="h-5 w-5 text-success" />
                    ) : (
                      <XCircle className="h-5 w-5 text-destructive" />
                    )}
                    <div>
                      <p className="font-medium">Show Results</p>
                      <p className="text-sm text-muted-foreground">
                        {quiz.showResults ? "Yes" : "No"}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="bg-muted/30 rounded-lg p-4 space-y-3">
                  <div className="flex items-center gap-3">
                    {quiz.shuffleQuestions ? (
                      <CheckCircle className="h-5 w-5 text-success" />
                    ) : (
                      <XCircle className="h-5 w-5 text-destructive" />
                    )}
                    <div>
                      <p className="font-medium">Shuffle Questions</p>
                      <p className="text-sm text-muted-foreground">
                        {quiz.shuffleQuestions ? "Yes" : "No"}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Quiz Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium">Status</span>
                {getStatusBadge(quiz.status)}
              </div>

              <div className="flex items-center justify-between">
                <span className="text-sm font-medium">Max Grade</span>
                <span className="font-semibold">{quiz.maxGrade} points</span>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-sm font-medium">Retakes</span>
                {quiz.allowRetakes ? (
                  <CheckCircle className="h-5 w-5 text-success" />
                ) : (
                  <XCircle className="h-5 w-5 text-destructive" />
                )}
              </div>
            </CardContent>
          </Card>

          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Course Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center gap-2">
                <BookOpen className="h-4 w-4 text-muted-foreground" />
                <span className="font-medium">{quiz.courseName || "Unknown Course"}</span>
              </div>

              <div className="flex items-center gap-2">
                <User className="h-4 w-4 text-muted-foreground" />
                <span className="font-medium">{quiz.teacherName || "Unknown Teacher"}</span>
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
                    {formatDate(quiz.dueDate)}
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <Clock className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">Created</p>
                  <p className="text-sm text-muted-foreground">
                    {formatDate(quiz.createdAt)}
                  </p>
                </div>
              </div>

              {quiz.updatedAt && (
                <div className="flex items-center gap-2">
                  <Clock className="h-4 w-4 text-muted-foreground" />
                  <div>
                    <p className="text-sm font-medium">Last Updated</p>
                    <p className="text-sm text-muted-foreground">
                      {formatDate(quiz.updatedAt)}
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