import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ArrowLeft, CheckCircle, XCircle, Clock, Trophy, FileText } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { getQuizById, QuizDTO, QuizAttemptDTO, StudentAnswerDTO } from "@/services/api";

export default function QuizResults() {
  const { quizId } = useParams();
  const navigate = useNavigate();
  const { toast } = useToast();
  
  const [quiz, setQuiz] = useState<QuizDTO | null>(null);
  const [attempts, setAttempts] = useState<QuizAttemptDTO[]>([]);
  const [selectedAttempt, setSelectedAttempt] = useState<QuizAttemptDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (quizId) {
      loadQuizResults();
    }
  }, [quizId]);

  const loadQuizResults = async () => {
    try {
      setLoading(true);
      const quizData = await getQuizById(Number(quizId));
      setQuiz(quizData);

      // Fetch student's attempts
      const response = await fetch(`/api/quizzes/${quizId}/my-attempts`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
        },
      });
      
      if (response.ok) {
        const attemptsData = await response.json();
        setAttempts(attemptsData);
        if (attemptsData.length > 0) {
          setSelectedAttempt(attemptsData[attemptsData.length - 1]); // Show latest attempt
        }
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load quiz results",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const getAnswerStatus = (answer: StudentAnswerDTO) => {
    if (answer.isCorrect === true) {
      return <Badge variant="default" className="bg-green-500"><CheckCircle className="h-3 w-3 mr-1" />Correct</Badge>;
    } else if (answer.isCorrect === false) {
      return <Badge variant="destructive"><XCircle className="h-3 w-3 mr-1" />Incorrect</Badge>;
    } else {
      return <Badge variant="secondary"><Clock className="h-3 w-3 mr-1" />Pending Review</Badge>;
    }
  };

  const getQuestionById = (questionId: number) => {
    return quiz?.questions?.find(q => q.id === questionId);
  };

  const getOptionText = (questionId: number, optionId: number) => {
    const question = getQuestionById(questionId);
    const option = question?.options?.find(o => o.id === optionId);
    return option?.optionText || "";
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <FileText className="h-12 w-12 animate-pulse text-primary mx-auto mb-4" />
          <p className="text-muted-foreground">Loading results...</p>
        </div>
      </div>
    );
  }

  if (!quiz) {
    return null;
  }

  const bestAttempt = attempts.reduce((best, current) => 
    (current.score || 0) > (best.score || 0) ? current : best
  , attempts[0]);

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold">{quiz.title}</h1>
          <p className="text-muted-foreground">Quiz Results</p>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Total Attempts</CardDescription>
            <CardTitle className="text-3xl">{attempts.length}</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Best Score</CardDescription>
            <CardTitle className="text-3xl flex items-center gap-2">
              <Trophy className="h-6 w-6 text-yellow-500" />
              {bestAttempt?.score || 0}/{quiz.totalMarks}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Pass Status</CardDescription>
            <CardTitle className="text-3xl">
              {(() => {
                const score = bestAttempt?.score || 0;
                const totalMarks = quiz.totalMarks;
                const passingMarks = quiz.passingMarks;
                
                // If passing marks is greater than total marks, use percentage comparison
                let isPassed = false;
                if (passingMarks > totalMarks) {
                  const percentage = (score / totalMarks) * 100;
                  isPassed = percentage >= passingMarks;
                } else {
                  isPassed = score >= passingMarks;
                }
                
                return isPassed ? (
                  <Badge variant="default" className="bg-green-500">Passed</Badge>
                ) : (
                  <Badge variant="destructive">Not Passed</Badge>
                );
              })()}
            </CardTitle>
          </CardHeader>
        </Card>
      </div>

      {/* Attempts List */}
      {attempts.length > 1 && (
        <Card>
          <CardHeader>
            <CardTitle>Your Attempts</CardTitle>
            <CardDescription>Select an attempt to view details</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              {attempts.map((attempt, index) => (
                <div
                  key={attempt.id}
                  className={`flex items-center justify-between p-3 rounded-lg border cursor-pointer transition-colors ${
                    selectedAttempt?.id === attempt.id ? 'bg-accent border-primary' : 'hover:bg-accent/50'
                  }`}
                  onClick={() => setSelectedAttempt(attempt)}
                >
                  <div>
                    <p className="font-medium">Attempt {index + 1}</p>
                    <p className="text-sm text-muted-foreground">
                      {new Date(attempt.submittedAt!).toLocaleString()}
                    </p>
                  </div>
                  <Badge variant={attempt.id === bestAttempt?.id ? "default" : "outline"}>
                    {attempt.score}/{quiz.totalMarks}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Selected Attempt Details */}
      {selectedAttempt && (
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle>Attempt Details</CardTitle>
                <CardDescription>
                  Submitted on {new Date(selectedAttempt.submittedAt!).toLocaleString()}
                </CardDescription>
              </div>
              <div className="text-right">
                <p className="text-2xl font-bold">
                  {selectedAttempt.score}/{selectedAttempt.totalMarks}
                </p>
                <p className="text-sm text-muted-foreground">
                  {Math.round((selectedAttempt.score! / selectedAttempt.totalMarks!) * 100)}%
                </p>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-6">
              {selectedAttempt.answers?.map((answer, index) => {
                const question = getQuestionById(answer.questionId);
                if (!question) return null;

                return (
                  <div key={answer.id} className="border-b pb-4 last:border-0">
                    <div className="flex items-start justify-between mb-2">
                      <div className="flex-1">
                        <p className="font-medium">Question {index + 1}</p>
                        <p className="text-sm text-muted-foreground mt-1">{question.questionText}</p>
                      </div>
                      <div className="flex items-center gap-2">
                        {getAnswerStatus(answer)}
                        <Badge variant="outline">{answer.marksAwarded || 0}/{question.marks}</Badge>
                      </div>
                    </div>

                    <div className="mt-3 space-y-2">
                      <div className="bg-accent/50 p-3 rounded-lg">
                        <p className="text-sm font-medium mb-1">Your Answer:</p>
                        {question.questionType === "MULTIPLE_CHOICE" && answer.selectedOptionId ? (
                          <p className="text-sm">{getOptionText(question.id!, answer.selectedOptionId)}</p>
                        ) : (
                          <p className="text-sm">{answer.answerText || "No answer provided"}</p>
                        )}
                      </div>

                      {quiz.showResultsImmediately && question.questionType === "MULTIPLE_CHOICE" && (
                        <div className="bg-green-50 p-3 rounded-lg">
                          <p className="text-sm font-medium mb-1 text-green-900">Correct Answer:</p>
                          <p className="text-sm text-green-900">
                            {question.options?.find(o => o.isCorrect)?.optionText}
                          </p>
                        </div>
                      )}

                      {quiz.showResultsImmediately && question.explanation && (
                        <div className="bg-blue-50 p-3 rounded-lg">
                          <p className="text-sm font-medium mb-1 text-blue-900">Explanation:</p>
                          <p className="text-sm text-blue-900">{question.explanation}</p>
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
