import { useState, useEffect } from "react";
import { Plus, Edit, Trash2, Eye, CheckCircle, Clock, FileQuestion, ListPlus, Users } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/hooks/use-toast";
import { 
  QuizDTO, 
  QuestionDTO, 
  QuestionOptionDTO,
  getQuizzesByClass, 
  deleteQuiz, 
  publishQuiz, 
  getQuizById,
  addQuestion,
  updateQuestion,
  deleteQuestion
} from "@/services/api";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Checkbox } from "@/components/ui/checkbox";

interface QuizManagerProps {
  classId: number;
}

export function QuizManager({ classId }: QuizManagerProps) {
  const [quizzes, setQuizzes] = useState<QuizDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [showViewDialog, setShowViewDialog] = useState(false);
  const [showEditDialog, setShowEditDialog] = useState(false);
  const [showQuestionsDialog, setShowQuestionsDialog] = useState(false);
  const [selectedQuiz, setSelectedQuiz] = useState<QuizDTO | null>(null);
  const { toast } = useToast();

  useEffect(() => {
    loadQuizzes();
  }, [classId]);

  const loadQuizzes = async () => {
    try {
      setLoading(true);
      const data = await getQuizzesByClass(classId);
      setQuizzes(data);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load quizzes",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (quizId: number) => {
    if (!confirm("Are you sure you want to delete this quiz?")) return;

    try {
      await deleteQuiz(quizId);
      toast({
        title: "Success",
        description: "Quiz deleted successfully",
      });
      loadQuizzes();
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete quiz",
        variant: "destructive",
      });
    }
  };

  const handlePublish = async (quizId: number) => {
    try {
      await publishQuiz(quizId);
      toast({
        title: "Success",
        description: "Quiz published successfully",
      });
      loadQuizzes();
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to publish quiz",
        variant: "destructive",
      });
    }
  };

  const handleView = async (quizId: number) => {
    try {
      const quiz = await getQuizById(quizId);
      setSelectedQuiz(quiz);
      setShowViewDialog(true);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load quiz details",
        variant: "destructive",
      });
    }
  };

  const handleEdit = async (quizId: number) => {
    try {
      const quiz = await getQuizById(quizId);
      setSelectedQuiz(quiz);
      setShowEditDialog(true);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load quiz details",
        variant: "destructive",
      });
    }
  };

  const handleManageQuestions = async (quizId: number) => {
    try {
      const quiz = await getQuizById(quizId);
      setSelectedQuiz(quiz);
      setShowQuestionsDialog(true);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load quiz details",
        variant: "destructive",
      });
    }
  };

  if (loading) {
    return <div className="text-center py-8">Loading quizzes...</div>;
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h3 className="text-lg font-semibold">Quizzes</h3>
        <Button onClick={() => setShowCreateDialog(true)}>
          <Plus className="h-4 w-4 mr-2" />
          Create Quiz
        </Button>
      </div>

      {quizzes.length === 0 ? (
        <Card>
          <CardContent className="py-8 text-center text-muted-foreground">
            No quizzes yet. Create your first quiz to get started.
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4">
          {quizzes.map((quiz) => (
            <Card key={quiz.id}>
              <CardHeader>
                <div className="flex justify-between items-start">
                  <div>
                    <CardTitle className="text-lg">{quiz.title}</CardTitle>
                    <CardDescription>{quiz.description}</CardDescription>
                  </div>
                  <Badge variant={quiz.status === "PUBLISHED" ? "default" : "secondary"}>
                    {quiz.status}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent>
                <div className="flex items-center gap-4 text-sm text-muted-foreground mb-4">
                  <span className="flex items-center gap-1">
                    <Clock className="h-4 w-4" />
                    {quiz.duration} mins
                  </span>
                  <span>{quiz.totalMarks} marks</span>
                  <span>{quiz.questionCount || 0} questions</span>
                </div>
                <div className="flex gap-2 flex-wrap">
                  <Button 
                    variant="outline" 
                    size="sm"
                    onClick={() => quiz.id && handleView(quiz.id)}
                  >
                    <Eye className="h-4 w-4 mr-1" />
                    View
                  </Button>
                  <Button 
                    variant="outline" 
                    size="sm"
                    onClick={() => quiz.id && handleEdit(quiz.id)}
                  >
                    <Edit className="h-4 w-4 mr-1" />
                    Edit
                  </Button>
                  <Button 
                    variant="outline" 
                    size="sm"
                    onClick={() => quiz.id && handleManageQuestions(quiz.id)}
                  >
                    <ListPlus className="h-4 w-4 mr-1" />
                    Questions
                  </Button>
                  {quiz.status === "PUBLISHED" && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => {
                        window.open(`/teacher/quiz/${quiz.id}/attempts`, '_blank');
                      }}
                    >
                      <Users className="h-4 w-4 mr-1" />
                      Attempts
                    </Button>
                  )}
                  {quiz.status === "DRAFT" && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => quiz.id && handlePublish(quiz.id)}
                    >
                      <CheckCircle className="h-4 w-4 mr-1" />
                      Publish
                    </Button>
                  )}
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => quiz.id && handleDelete(quiz.id)}
                  >
                    <Trash2 className="h-4 w-4 mr-1" />
                    Delete
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <CreateQuizDialog
        open={showCreateDialog}
        onClose={() => setShowCreateDialog(false)}
        classId={classId}
        onSuccess={loadQuizzes}
      />

      <ViewQuizDialog
        open={showViewDialog}
        onClose={() => {
          setShowViewDialog(false);
          setSelectedQuiz(null);
        }}
        quiz={selectedQuiz}
      />

      <EditQuizDialog
        open={showEditDialog}
        onClose={() => {
          setShowEditDialog(false);
          setSelectedQuiz(null);
        }}
        quiz={selectedQuiz}
        onSuccess={loadQuizzes}
      />

      <ManageQuestionsDialog
        open={showQuestionsDialog}
        onClose={() => {
          setShowQuestionsDialog(false);
          setSelectedQuiz(null);
        }}
        quiz={selectedQuiz}
        onSuccess={loadQuizzes}
      />
    </div>
  );
}

interface CreateQuizDialogProps {
  open: boolean;
  onClose: () => void;
  classId: number;
  onSuccess: () => void;
}

function CreateQuizDialog({ open, onClose, classId, onSuccess }: CreateQuizDialogProps) {
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    duration: 30,
    totalMarks: 100,
    passingMarks: 40,
    startTime: "",
    endTime: "",
    maxAttempts: 1,
    randomizeQuestions: false,
    showResultsImmediately: true,
  });
  const { toast } = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      const { createQuiz } = await import("@/services/api");
      await createQuiz({
        ...formData,
        classId,
        status: "DRAFT",
      });
      
      toast({
        title: "Success",
        description: "Quiz created successfully",
      });
      onSuccess();
      onClose();
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to create quiz",
        variant: "destructive",
      });
    }
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Create New Quiz</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="title">Quiz Title</Label>
            <Input
              id="title"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              required
            />
          </div>
          <div>
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="duration">Duration (minutes)</Label>
              <Input
                id="duration"
                type="number"
                value={formData.duration}
                onChange={(e) => setFormData({ ...formData, duration: parseInt(e.target.value) })}
                required
              />
            </div>
            <div>
              <Label htmlFor="totalMarks">Total Marks</Label>
              <Input
                id="totalMarks"
                type="number"
                value={formData.totalMarks}
                onChange={(e) => setFormData({ ...formData, totalMarks: parseInt(e.target.value) })}
                required
              />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="startTime">Start Time</Label>
              <Input
                id="startTime"
                type="datetime-local"
                value={formData.startTime}
                onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                required
              />
            </div>
            <div>
              <Label htmlFor="endTime">End Time</Label>
              <Input
                id="endTime"
                type="datetime-local"
                value={formData.endTime}
                onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                required
              />
            </div>
          </div>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit">Create Quiz</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}


interface ViewQuizDialogProps {
  open: boolean;
  onClose: () => void;
  quiz: QuizDTO | null;
}

function ViewQuizDialog({ open, onClose, quiz }: ViewQuizDialogProps) {
  if (!quiz) return null;

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-3xl max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{quiz.title}</DialogTitle>
          <DialogDescription>{quiz.description}</DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-muted-foreground">Duration</p>
              <p className="font-medium">{quiz.duration} minutes</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Total Marks</p>
              <p className="font-medium">{quiz.totalMarks}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Passing Marks</p>
              <p className="font-medium">{quiz.passingMarks}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Status</p>
              <Badge variant={quiz.status === "PUBLISHED" ? "default" : "secondary"}>
                {quiz.status}
              </Badge>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Start Time</p>
              <p className="font-medium">
                {quiz.startTime ? new Date(quiz.startTime).toLocaleString() : "N/A"}
              </p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">End Time</p>
              <p className="font-medium">
                {quiz.endTime ? new Date(quiz.endTime).toLocaleString() : "N/A"}
              </p>
            </div>
          </div>

          {quiz.questions && quiz.questions.length > 0 && (
            <div>
              <h3 className="font-semibold mb-3 flex items-center gap-2">
                <FileQuestion className="h-5 w-5" />
                Questions ({quiz.questions.length})
              </h3>
              <div className="space-y-3">
                {quiz.questions.map((question, index) => (
                  <Card key={question.id || index}>
                    <CardContent className="pt-4">
                      <p className="font-medium mb-2">
                        {index + 1}. {question.questionText}
                      </p>
                      <div className="flex gap-2 text-sm text-muted-foreground">
                        <Badge variant="outline">{question.questionType}</Badge>
                        <span>{question.marks} marks</span>
                      </div>
                      {question.options && question.options.length > 0 && (
                        <div className="mt-3 space-y-1">
                          {question.options.map((option, optIndex) => (
                            <div
                              key={option.id || optIndex}
                              className={`text-sm p-2 rounded ${
                                option.isCorrect ? "bg-green-50 text-green-900" : ""
                              }`}
                            >
                              {String.fromCharCode(65 + optIndex)}. {option.optionText}
                              {option.isCorrect && " ✓"}
                            </div>
                          ))}
                        </div>
                      )}
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}

interface EditQuizDialogProps {
  open: boolean;
  onClose: () => void;
  quiz: QuizDTO | null;
  onSuccess: () => void;
}

function EditQuizDialog({ open, onClose, quiz, onSuccess }: EditQuizDialogProps) {
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    duration: 30,
    totalMarks: 100,
    passingMarks: 40,
    startTime: "",
    endTime: "",
    maxAttempts: 1,
    randomizeQuestions: false,
    showResultsImmediately: true,
  });
  const { toast } = useToast();

  useEffect(() => {
    if (quiz) {
      setFormData({
        title: quiz.title || "",
        description: quiz.description || "",
        duration: quiz.duration || 30,
        totalMarks: quiz.totalMarks || 100,
        passingMarks: quiz.passingMarks || 40,
        startTime: quiz.startTime ? quiz.startTime.slice(0, 16) : "",
        endTime: quiz.endTime ? quiz.endTime.slice(0, 16) : "",
        maxAttempts: quiz.maxAttempts || 1,
        randomizeQuestions: quiz.randomizeQuestions || false,
        showResultsImmediately: quiz.showResultsImmediately !== undefined ? quiz.showResultsImmediately : true,
      });
    }
  }, [quiz]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!quiz?.id) return;

    try {
      const { updateQuiz } = await import("@/services/api");
      await updateQuiz(quiz.id, {
        ...formData,
        classId: quiz.classId,
        status: quiz.status,
      });
      
      toast({
        title: "Success",
        description: "Quiz updated successfully",
      });
      onSuccess();
      onClose();
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to update quiz",
        variant: "destructive",
      });
    }
  };

  if (!quiz) return null;

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Edit Quiz</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="edit-title">Quiz Title</Label>
            <Input
              id="edit-title"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              required
            />
          </div>
          <div>
            <Label htmlFor="edit-description">Description</Label>
            <Textarea
              id="edit-description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="edit-duration">Duration (minutes)</Label>
              <Input
                id="edit-duration"
                type="number"
                value={formData.duration}
                onChange={(e) => setFormData({ ...formData, duration: parseInt(e.target.value) })}
                required
              />
            </div>
            <div>
              <Label htmlFor="edit-totalMarks">Total Marks</Label>
              <Input
                id="edit-totalMarks"
                type="number"
                value={formData.totalMarks}
                onChange={(e) => setFormData({ ...formData, totalMarks: parseInt(e.target.value) })}
                required
              />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="edit-startTime">Start Time</Label>
              <Input
                id="edit-startTime"
                type="datetime-local"
                value={formData.startTime}
                onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                required
              />
            </div>
            <div>
              <Label htmlFor="edit-endTime">End Time</Label>
              <Input
                id="edit-endTime"
                type="datetime-local"
                value={formData.endTime}
                onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                required
              />
            </div>
          </div>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit">Save Changes</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}


interface ManageQuestionsDialogProps {
  open: boolean;
  onClose: () => void;
  quiz: QuizDTO | null;
  onSuccess: () => void;
}

function ManageQuestionsDialog({ open, onClose, quiz, onSuccess }: ManageQuestionsDialogProps) {
  const [questions, setQuestions] = useState<QuestionDTO[]>([]);
  const [showAddQuestion, setShowAddQuestion] = useState(false);
  const [editingQuestion, setEditingQuestion] = useState<QuestionDTO | null>(null);
  const { toast } = useToast();

  useEffect(() => {
    if (quiz?.questions) {
      setQuestions(quiz.questions);
    }
  }, [quiz]);

  const handleAddQuestion = async (question: QuestionDTO) => {
    if (!quiz?.id) return;

    try {
      await addQuestion(quiz.id, question);
      toast({
        title: "Success",
        description: "Question added successfully",
      });
      onSuccess();
      setShowAddQuestion(false);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to add question",
        variant: "destructive",
      });
    }
  };

  const handleUpdateQuestion = async (question: QuestionDTO) => {
    if (!question.id) return;

    try {
      await updateQuestion(question.id, question);
      toast({
        title: "Success",
        description: "Question updated successfully",
      });
      onSuccess();
      setEditingQuestion(null);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to update question",
        variant: "destructive",
      });
    }
  };

  const handleDeleteQuestion = async (questionId: number) => {
    if (!confirm("Are you sure you want to delete this question?")) return;

    try {
      await deleteQuestion(questionId);
      toast({
        title: "Success",
        description: "Question deleted successfully",
      });
      onSuccess();
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete question",
        variant: "destructive",
      });
    }
  };

  if (!quiz) return null;

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Manage Questions - {quiz.title}</DialogTitle>
          <DialogDescription>
            Add, edit, or remove questions for this quiz
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <p className="text-sm text-muted-foreground">
              {questions.length} question(s) | Total: {questions.reduce((sum, q) => sum + (q.marks || 0), 0)} marks
            </p>
            <Button onClick={() => setShowAddQuestion(true)} size="sm">
              <Plus className="h-4 w-4 mr-2" />
              Add Question
            </Button>
          </div>

          {questions.length === 0 ? (
            <Card>
              <CardContent className="py-8 text-center text-muted-foreground">
                No questions yet. Add your first question to get started.
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-3">
              {questions.map((question, index) => (
                <Card key={question.id || index}>
                  <CardContent className="pt-4">
                    <div className="flex justify-between items-start mb-2">
                      <p className="font-medium flex-1">
                        {index + 1}. {question.questionText}
                      </p>
                      <div className="flex gap-2 ml-4">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => setEditingQuestion(question)}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => question.id && handleDeleteQuestion(question.id)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                    <div className="flex gap-2 text-sm text-muted-foreground mb-2">
                      <Badge variant="outline">{question.questionType}</Badge>
                      <span>{question.marks} marks</span>
                    </div>
                    {question.options && question.options.length > 0 && (
                      <div className="mt-2 space-y-1">
                        {question.options.map((option, optIndex) => (
                          <div
                            key={option.id || optIndex}
                            className={`text-sm p-2 rounded ${
                              option.isCorrect ? "bg-green-50 text-green-900" : "bg-gray-50"
                            }`}
                          >
                            {String.fromCharCode(65 + optIndex)}. {option.optionText}
                            {option.isCorrect && " ✓"}
                          </div>
                        ))}
                      </div>
                    )}
                    {question.correctAnswer && (
                      <div className="mt-2 text-sm">
                        <span className="font-medium">Answer:</span> {question.correctAnswer}
                      </div>
                    )}
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>

        <QuestionFormDialog
          open={showAddQuestion}
          onClose={() => setShowAddQuestion(false)}
          onSubmit={handleAddQuestion}
          title="Add Question"
        />

        <QuestionFormDialog
          open={!!editingQuestion}
          onClose={() => setEditingQuestion(null)}
          onSubmit={handleUpdateQuestion}
          question={editingQuestion}
          title="Edit Question"
        />
      </DialogContent>
    </Dialog>
  );
}

interface QuestionFormDialogProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (question: QuestionDTO) => void;
  question?: QuestionDTO | null;
  title: string;
}

function QuestionFormDialog({ open, onClose, onSubmit, question, title }: QuestionFormDialogProps) {
  const [formData, setFormData] = useState<QuestionDTO>({
    questionText: "",
    questionType: "MULTIPLE_CHOICE",
    marks: 1,
    explanation: "",
    options: [
      { optionText: "", isCorrect: false },
      { optionText: "", isCorrect: false },
      { optionText: "", isCorrect: false },
      { optionText: "", isCorrect: false },
    ],
    correctAnswer: "",
  });

  useEffect(() => {
    if (question) {
      setFormData({
        ...question,
        options: question.options || [
          { optionText: "", isCorrect: false },
          { optionText: "", isCorrect: false },
          { optionText: "", isCorrect: false },
          { optionText: "", isCorrect: false },
        ],
      });
    } else {
      setFormData({
        questionText: "",
        questionType: "MULTIPLE_CHOICE",
        marks: 1,
        explanation: "",
        options: [
          { optionText: "", isCorrect: false },
          { optionText: "", isCorrect: false },
          { optionText: "", isCorrect: false },
          { optionText: "", isCorrect: false },
        ],
        correctAnswer: "",
      });
    }
  }, [question, open]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // Validation
    if (formData.questionType === "MULTIPLE_CHOICE") {
      const hasCorrectAnswer = formData.options?.some(opt => opt.isCorrect);
      if (!hasCorrectAnswer) {
        alert("Please mark at least one option as correct");
        return;
      }
      const filledOptions = formData.options?.filter(opt => opt.optionText.trim());
      if (!filledOptions || filledOptions.length < 2) {
        alert("Please provide at least 2 options");
        return;
      }
    }

    if (formData.questionType === "TRUE_FALSE" && !formData.correctAnswer) {
      alert("Please select the correct answer");
      return;
    }

    // Clean up options for non-MCQ questions
    const submitData = { ...formData };
    if (formData.questionType !== "MULTIPLE_CHOICE") {
      submitData.options = [];
    } else {
      submitData.options = formData.options?.filter(opt => opt.optionText.trim());
    }

    onSubmit(submitData);
  };

  const handleOptionChange = (index: number, field: keyof QuestionOptionDTO, value: string | boolean) => {
    const newOptions = [...(formData.options || [])];
    newOptions[index] = { ...newOptions[index], [field]: value };
    setFormData({ ...formData, options: newOptions });
  };

  const addOption = () => {
    setFormData({
      ...formData,
      options: [...(formData.options || []), { optionText: "", isCorrect: false }],
    });
  };

  const removeOption = (index: number) => {
    const newOptions = formData.options?.filter((_, i) => i !== index);
    setFormData({ ...formData, options: newOptions });
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="questionText">Question Text</Label>
            <Textarea
              id="questionText"
              value={formData.questionText}
              onChange={(e) => setFormData({ ...formData, questionText: e.target.value })}
              required
              rows={3}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="questionType">Question Type</Label>
              <Select
                value={formData.questionType}
                onValueChange={(value) => setFormData({ ...formData, questionType: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="MULTIPLE_CHOICE">Multiple Choice</SelectItem>
                  <SelectItem value="TRUE_FALSE">True/False</SelectItem>
                  <SelectItem value="SHORT_ANSWER">Short Answer</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="marks">Marks</Label>
              <Input
                id="marks"
                type="number"
                min="1"
                value={formData.marks}
                onChange={(e) => setFormData({ ...formData, marks: parseInt(e.target.value) })}
                required
              />
            </div>
          </div>

          {formData.questionType === "MULTIPLE_CHOICE" && (
            <div>
              <Label>Options</Label>
              <div className="space-y-2 mt-2">
                {formData.options?.map((option, index) => (
                  <div key={index} className="flex gap-2 items-center">
                    <Checkbox
                      checked={option.isCorrect}
                      onCheckedChange={(checked) =>
                        handleOptionChange(index, "isCorrect", checked as boolean)
                      }
                    />
                    <Input
                      placeholder={`Option ${String.fromCharCode(65 + index)}`}
                      value={option.optionText}
                      onChange={(e) => handleOptionChange(index, "optionText", e.target.value)}
                    />
                    {formData.options && formData.options.length > 2 && (
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        onClick={() => removeOption(index)}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    )}
                  </div>
                ))}
                <Button type="button" variant="outline" size="sm" onClick={addOption}>
                  <Plus className="h-4 w-4 mr-2" />
                  Add Option
                </Button>
              </div>
            </div>
          )}

          {formData.questionType === "TRUE_FALSE" && (
            <div>
              <Label htmlFor="correctAnswer">Correct Answer</Label>
              <Select
                value={formData.correctAnswer}
                onValueChange={(value) => setFormData({ ...formData, correctAnswer: value })}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select correct answer" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="True">True</SelectItem>
                  <SelectItem value="False">False</SelectItem>
                </SelectContent>
              </Select>
            </div>
          )}

          {formData.questionType === "SHORT_ANSWER" && (
            <div>
              <Label htmlFor="correctAnswer">Model Answer (Optional)</Label>
              <Textarea
                id="correctAnswer"
                value={formData.correctAnswer}
                onChange={(e) => setFormData({ ...formData, correctAnswer: e.target.value })}
                placeholder="Provide a model answer for reference"
                rows={2}
              />
            </div>
          )}

          <div>
            <Label htmlFor="explanation">Explanation (Optional)</Label>
            <Textarea
              id="explanation"
              value={formData.explanation}
              onChange={(e) => setFormData({ ...formData, explanation: e.target.value })}
              placeholder="Provide an explanation for the answer"
              rows={2}
            />
          </div>

          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit">
              {question ? "Update" : "Add"} Question
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
