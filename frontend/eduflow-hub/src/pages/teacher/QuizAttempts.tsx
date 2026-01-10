import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ArrowLeft, User, Clock, Trophy, FileText, CheckCircle, XCircle } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { getQuizById, QuizDTO, QuizAttemptDTO } from "@/services/api";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

export default function QuizAttempts() {
  const { quizId } = useParams();
  const navigate = useNavigate();
  const { toast } = useToast();
  
  const [quiz, setQuiz] = useState<QuizDTO | null>(null);
  const [attempts, setAttempts] = useState<QuizAttemptDTO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (quizId) {
      loadQuizAttempts();
    }
  }, [quizId]);

  const loadQuizAttempts = async () => {
    try {
      setLoading(true);
      const quizData = await getQuizById(Number(quizId));
      setQuiz(quizData);

      // Fetch all attempts for this quiz
      const response = await fetch(`/api/quizzes/${quizId}/attempts`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
        },
      });
      
      if (response.ok) {
        const attemptsData = await response.json();
        setAttempts(attemptsData);
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load quiz attempts",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const getPassStatus = (score: number, totalMarks: number, passingMarks: number) => {
    // Debug logging
    console.log('Pass Status Check:', { score, totalMarks, passingMarks, passed: score >= passingMarks });
    
    // If passing marks is greater than total marks, use percentage comparison
    // This handles cases where passing marks was set as a percentage value
    let isPassed = false;
    if (passingMarks > totalMarks) {
      // Assume passing marks is a percentage (e.g., 40 means 40%)
      const percentage = (score / totalMarks) * 100;
      isPassed = percentage >= passingMarks;
    } else {
      // Direct comparison: score should be >= passing marks
      isPassed = score >= passingMarks;
    }
    
    if (isPassed) {
      return <Badge variant="default" className="bg-green-500"><CheckCircle className="h-3 w-3 mr-1" />Passed</Badge>;
    } else {
      return <Badge variant="destructive"><XCircle className="h-3 w-3 mr-1" />Failed</Badge>;
    }
  };

  const getAverageScore = () => {
    if (attempts.length === 0) return 0;
    const total = attempts.reduce((sum, attempt) => sum + (attempt.score || 0), 0);
    return (total / attempts.length).toFixed(1);
  };

  const getPassRate = () => {
    if (attempts.length === 0 || !quiz) return 0;
    const passed = attempts.filter(a => (a.score || 0) >= quiz.passingMarks).length;
    return ((passed / attempts.length) * 100).toFixed(1);
  };

  const getUniqueStudents = () => {
    const uniqueIds = new Set(attempts.map(a => a.studentId));
    return uniqueIds.size;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <FileText className="h-12 w-12 animate-pulse text-primary mx-auto mb-4" />
          <p className="text-muted-foreground">Loading attempts...</p>
        </div>
      </div>
    );
  }

  if (!quiz) {
    return null;
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold">{quiz.title}</h1>
          <p className="text-muted-foreground">Quiz Attempts Overview</p>
        </div>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Total Attempts</CardDescription>
            <CardTitle className="text-3xl">{attempts.length}</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Unique Students</CardDescription>
            <CardTitle className="text-3xl flex items-center gap-2">
              <User className="h-6 w-6" />
              {getUniqueStudents()}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Average Score</CardDescription>
            <CardTitle className="text-3xl">
              {getAverageScore()}/{quiz.totalMarks}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Pass Rate</CardDescription>
            <CardTitle className="text-3xl flex items-center gap-2">
              <Trophy className="h-6 w-6 text-yellow-500" />
              {getPassRate()}%
            </CardTitle>
          </CardHeader>
        </Card>
      </div>

      {/* Attempts Table */}
      <Card>
        <CardHeader>
          <CardTitle>All Attempts</CardTitle>
          <CardDescription>
            {attempts.length > 0 
              ? `${attempts.length} attempt(s) recorded`
              : "No attempts yet"}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {attempts.length === 0 ? (
            <div className="text-center py-8">
              <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-2" />
              <p className="text-sm text-muted-foreground">
                No students have attempted this quiz yet
              </p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Student</TableHead>
                  <TableHead>Attempt #</TableHead>
                  <TableHead>Score</TableHead>
                  <TableHead>Percentage</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Submitted At</TableHead>
                  <TableHead>Duration</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {attempts.map((attempt) => {
                  const percentage = ((attempt.score || 0) / (attempt.totalMarks || 1) * 100).toFixed(1);
                  const duration = attempt.startedAt && attempt.submittedAt
                    ? Math.round((new Date(attempt.submittedAt).getTime() - new Date(attempt.startedAt).getTime()) / 60000)
                    : 0;

                  return (
                    <TableRow key={attempt.id}>
                      <TableCell className="font-medium">
                        {attempt.studentName || `Student ${attempt.studentId}`}
                      </TableCell>
                      <TableCell>{attempt.attemptNumber}</TableCell>
                      <TableCell>
                        <Badge variant="outline">
                          {attempt.score}/{attempt.totalMarks}
                        </Badge>
                      </TableCell>
                      <TableCell>{percentage}%</TableCell>
                      <TableCell>
                        {getPassStatus(attempt.score || 0, attempt.totalMarks || 1, quiz.passingMarks)}
                      </TableCell>
                      <TableCell className="text-sm text-muted-foreground">
                        {attempt.submittedAt 
                          ? new Date(attempt.submittedAt).toLocaleString()
                          : "In Progress"}
                      </TableCell>
                      <TableCell>
                        <span className="flex items-center gap-1 text-sm text-muted-foreground">
                          <Clock className="h-3 w-3" />
                          {duration} mins
                        </span>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
