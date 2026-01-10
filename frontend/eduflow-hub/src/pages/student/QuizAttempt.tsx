import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { ArrowLeft, Clock, AlertCircle, CheckCircle } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { 
  getQuizById, 
  startQuizAttempt, 
  submitQuizAttempt,
  QuizDTO, 
  QuizAttemptDTO,
  StudentAnswerDTO 
} from "@/services/api";
import { Alert, AlertDescription } from "@/components/ui/alert";

export default function QuizAttempt() {
  const { quizId } = useParams();
  const navigate = useNavigate();
  const { toast } = useToast();
  
  const [quiz, setQuiz] = useState<QuizDTO | null>(null);
  const [attempt, setAttempt] = useState<QuizAttemptDTO | null>(null);
  const [answers, setAnswers] = useState<Map<number, StudentAnswerDTO>>(new Map());
  const [timeRemaining, setTimeRemaining] = useState<number>(0);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (quizId) {
      initializeQuiz();
    }
  }, [quizId]);

  useEffect(() => {
    if (timeRemaining > 0) {
      const timer = setInterval(() => {
        setTimeRemaining((prev) => {
          if (prev <= 1) {
            handleSubmit();
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
      return () => clearInterval(timer);
    }
  }, [timeRemaining]);

  const initializeQuiz = async () => {
    try {
      setLoading(true);
      const quizData = await getQuizById(Number(quizId));
      setQuiz(quizData);

      const attemptData = await startQuizAttempt(Number(quizId));
      setAttempt(attemptData);
      
      if (quizData.duration) {
        setTimeRemaining(quizData.duration * 60);
      }
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Failed to start quiz",
        variant: "destructive",
      });
      navigate(-1);
    } finally {
      setLoading(false);
    }
  };

  const handleAnswerChange = (questionId: number, answer: Partial<StudentAnswerDTO>) => {
    const currentAnswer = answers.get(questionId) || { questionId };
    setAnswers(new Map(answers.set(questionId, { ...currentAnswer, ...answer })));
  };

  const handleSubmit = async () => {
    if (submitting) return;

    const unanswered = quiz?.questions?.filter(q => !answers.has(q.id!)) || [];
    if (unanswered.length > 0) {
      const confirm = window.confirm(
        `You have ${unanswered.length} unanswered question(s). Do you want to submit anyway?`
      );
      if (!confirm) return;
    }

    try {
      setSubmitting(true);
      const answerList = Array.from(answers.values());
      await submitQuizAttempt(attempt!.id!, answerList);
      
      toast({
        title: "Success",
        description: "Quiz submitted successfully!",
      });
      navigate(-1);
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Failed to submit quiz",
        variant: "destructive",
      });
    } finally {
      setSubmitting(false);
    }
  };

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, "0")}`;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <Clock className="h-12 w-12 animate-spin text-primary mx-auto mb-4" />
          <p className="text-muted-foreground">Loading quiz...</p>
        </div>
      </div>
    );
  }

  if (!quiz || !attempt) {
    return null;
  }

  const answeredCount = answers.size;
  const totalQuestions = quiz.questions?.length || 0;

  return (
    <div className="max-w-4xl mx-auto space-y-6 pb-20">
      {/* Header */}
      <div className="sticky top-0 z-10 bg-background border-b pb-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" onClick={() => navigate(-1)} disabled={submitting}>
              <ArrowLeft className="h-5 w-5" />
            </Button>
            <div>
              <h1 className="text-2xl font-bold">{quiz.title}</h1>
              <p className="text-sm text-muted-foreground">
                Attempt {attempt.attemptNumber} of {quiz.maxAttempts}
              </p>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <Badge variant={timeRemaining < 300 ? "destructive" : "default"} className="text-lg px-4 py-2">
              <Clock className="h-4 w-4 mr-2" />
              {formatTime(timeRemaining)}
            </Badge>
            <Badge variant="outline" className="text-lg px-4 py-2">
              {answeredCount}/{totalQuestions}
            </Badge>
          </div>
        </div>
      </div>

      {/* Warning for low time */}
      {timeRemaining < 300 && timeRemaining > 0 && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            Less than 5 minutes remaining! Your quiz will auto-submit when time runs out.
          </AlertDescription>
        </Alert>
      )}

      {/* Questions */}
      <div className="space-y-6">
        {quiz.questions?.map((question, index) => (
          <Card key={question.id}>
            <CardHeader>
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <CardTitle className="text-lg">
                    Question {index + 1}
                    {answers.has(question.id!) && (
                      <CheckCircle className="inline-block h-5 w-5 ml-2 text-green-500" />
                    )}
                  </CardTitle>
                  <CardDescription className="mt-2">{question.questionText}</CardDescription>
                </div>
                <Badge variant="outline">{question.marks} marks</Badge>
              </div>
            </CardHeader>
            <CardContent>
              {question.questionType === "MULTIPLE_CHOICE" && question.options && (
                <RadioGroup
                  value={answers.get(question.id!)?.selectedOptionId?.toString()}
                  onValueChange={(value) =>
                    handleAnswerChange(question.id!, { selectedOptionId: Number(value) })
                  }
                >
                  <div className="space-y-3">
                    {question.options.map((option, optIndex) => (
                      <div key={option.id} className="flex items-center space-x-2">
                        <RadioGroupItem value={option.id!.toString()} id={`q${question.id}-opt${option.id}`} />
                        <Label htmlFor={`q${question.id}-opt${option.id}`} className="flex-1 cursor-pointer">
                          {String.fromCharCode(65 + optIndex)}. {option.optionText}
                        </Label>
                      </div>
                    ))}
                  </div>
                </RadioGroup>
              )}

              {question.questionType === "TRUE_FALSE" && (
                <RadioGroup
                  value={answers.get(question.id!)?.answerText}
                  onValueChange={(value) => handleAnswerChange(question.id!, { answerText: value })}
                >
                  <div className="space-y-3">
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="True" id={`q${question.id}-true`} />
                      <Label htmlFor={`q${question.id}-true`} className="cursor-pointer">True</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="False" id={`q${question.id}-false`} />
                      <Label htmlFor={`q${question.id}-false`} className="cursor-pointer">False</Label>
                    </div>
                  </div>
                </RadioGroup>
              )}

              {question.questionType === "SHORT_ANSWER" && (
                <Textarea
                  placeholder="Type your answer here..."
                  value={answers.get(question.id!)?.answerText || ""}
                  onChange={(e) => handleAnswerChange(question.id!, { answerText: e.target.value })}
                  rows={4}
                />
              )}
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Submit Button */}
      <div className="fixed bottom-0 left-0 right-0 bg-background border-t p-4">
        <div className="max-w-4xl mx-auto flex justify-between items-center">
          <p className="text-sm text-muted-foreground">
            {answeredCount} of {totalQuestions} questions answered
          </p>
          <Button 
            onClick={handleSubmit} 
            disabled={submitting}
            size="lg"
          >
            {submitting ? "Submitting..." : "Submit Quiz"}
          </Button>
        </div>
      </div>
    </div>
  );
}
