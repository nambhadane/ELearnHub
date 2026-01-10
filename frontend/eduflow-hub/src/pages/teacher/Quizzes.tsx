import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ClipboardList, Plus, Eye, Edit, Trash2, CheckCircle, Clock } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/hooks/use-toast";
import { QuizDTO, getClassesByTeacher, ClassDTO } from "@/services/api";

export default function Quizzes() {
  const [classes, setClasses] = useState<ClassDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const { toast } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    loadClasses();
  }, []);

  const loadClasses = async () => {
    try {
      setLoading(true);
      const teacherId = parseInt(localStorage.getItem("userId") || "0");
      const data = await getClassesByTeacher(teacherId);
      setClasses(data);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load classes",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-muted-foreground">Loading quizzes...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
            <ClipboardList className="h-8 w-8" />
            Quizzes
          </h1>
          <p className="text-muted-foreground mt-2">
            Manage quizzes across all your classes
          </p>
        </div>
      </div>

      {classes.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center">
            <ClipboardList className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
            <h3 className="text-lg font-semibold mb-2">No Classes Yet</h3>
            <p className="text-muted-foreground mb-4">
              Create a class first to start adding quizzes
            </p>
            <Button onClick={() => navigate("/teacher/classes")}>
              Go to My Classes
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-6">
          {classes.map((classItem) => (
            <Card key={classItem.id} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex justify-between items-start">
                  <div>
                    <CardTitle>{classItem.name}</CardTitle>
                    <CardDescription>Class ID: {classItem.id}</CardDescription>
                  </div>
                  <Button
                    onClick={() => navigate(`/teacher/classes/${classItem.id}?tab=quizzes`)}
                  >
                    <Plus className="h-4 w-4 mr-2" />
                    Create Quiz
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                <Button
                  variant="outline"
                  onClick={() => navigate(`/teacher/classes/${classItem.id}?tab=quizzes`)}
                >
                  <Eye className="h-4 w-4 mr-2" />
                  View Quizzes
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
