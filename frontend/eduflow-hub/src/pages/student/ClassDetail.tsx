import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { BookOpen, Users, Calendar, ArrowLeft, FileText, Download, Loader2, File, Play, Clock, Trophy, Video, Link2, Copy } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useToast } from "@/hooks/use-toast";
import { getMaterialsByClass, downloadMaterial, Material, getAvailableQuizzes, QuizDTO, getAvailableLiveClasses, LiveClassDTO, joinLiveClass } from "@/services/api";
import { StudentAttendanceView } from "@/components/StudentAttendanceView";

export default function StudentClassDetail() {
  const { classId } = useParams();
  const navigate = useNavigate();
  const { toast } = useToast();
  
  const [materials, setMaterials] = useState<Material[]>([]);
  const [quizzes, setQuizzes] = useState<QuizDTO[]>([]);
  const [liveClasses, setLiveClasses] = useState<LiveClassDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [quizzesLoading, setQuizzesLoading] = useState(true);
  const [liveClassesLoading, setLiveClassesLoading] = useState(true);

  useEffect(() => {
    if (classId) {
      fetchMaterials();
      fetchQuizzes();
      fetchLiveClasses();
    }
  }, [classId]);

  const fetchMaterials = async () => {
    try {
      setLoading(true);
      const data = await getMaterialsByClass(Number(classId));
      setMaterials(data);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load materials",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const fetchQuizzes = async () => {
    try {
      setQuizzesLoading(true);
      const data = await getAvailableQuizzes(Number(classId));
      setQuizzes(data);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load quizzes",
        variant: "destructive",
      });
    } finally {
      setQuizzesLoading(false);
    }
  };

  const fetchLiveClasses = async () => {
    try {
      setLiveClassesLoading(true);
      const data = await getAvailableLiveClasses(Number(classId));
      setLiveClasses(data);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load live classes",
        variant: "destructive",
      });
    } finally {
      setLiveClassesLoading(false);
    }
  };

  const handleJoinLiveClass = async (meetingId: string) => {
    try {
      const user = JSON.parse(localStorage.getItem("user") || "{}");
      const displayName = user.name || "Student";
      const jitsiUrl = `https://meet.jit.si/${meetingId}#userInfo.displayName="${encodeURIComponent(displayName)}"`;
      window.open(jitsiUrl, '_blank');
      
      toast({
        title: "Joining Meeting",
        description: "Opening Jitsi meeting in new tab",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to join live class",
        variant: "destructive",
      });
    }
  };

  const copyMeetingLink = (meetingId: string) => {
    const meetingUrl = `https://meet.jit.si/${meetingId}`;
    navigator.clipboard.writeText(meetingUrl);
    toast({
      title: "Link Copied!",
      description: "Meeting link copied. You can open it on any device.",
    });
  };

  const handleDownload = async (materialId: number, fileName: string) => {
    try {
      await downloadMaterial(materialId, fileName);
      toast({
        title: "Success",
        description: "Download started",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to download material",
        variant: "destructive",
      });
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
    return (bytes / (1024 * 1024)).toFixed(1) + " MB";
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", { 
      month: "short", 
      day: "numeric", 
      year: "numeric" 
    });
  };

  const getFileIcon = (fileType: string) => {
    if (fileType === "video") return <Play className="h-5 w-5 text-primary" />;
    if (fileType === "pdf") return <FileText className="h-5 w-5 text-red-500" />;
    if (fileType === "document") return <FileText className="h-5 w-5 text-blue-500" />;
    return <File className="h-5 w-5 text-primary" />;
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate("/student/classes")}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold">Class Details</h1>
          <p className="text-muted-foreground">View class information and materials</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Class Information</CardTitle>
          <CardDescription>Details about this class</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <BookOpen className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium">Class ID</p>
                <p className="text-sm text-muted-foreground">{classId}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Users className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium">Students Enrolled</p>
                <p className="text-sm text-muted-foreground">Loading...</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Calendar className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium">Schedule</p>
                <p className="text-sm text-muted-foreground">Coming soon</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Live Classes Section */}
      <Card>
        <CardHeader>
          <CardTitle>Live Classes</CardTitle>
          <CardDescription>
            {liveClasses.length > 0 
              ? `${liveClasses.length} live class(es) scheduled or ongoing`
              : "No live classes scheduled"}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {liveClassesLoading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            </div>
          ) : liveClasses.length === 0 ? (
            <div className="text-center py-8">
              <Video className="h-12 w-12 text-muted-foreground mx-auto mb-2" />
              <p className="text-sm text-muted-foreground">
                No live classes are currently scheduled
              </p>
            </div>
          ) : (
            <div className="space-y-3">
              {liveClasses.map((liveClass) => (
                <div
                  key={liveClass.id}
                  className="flex items-center justify-between rounded-lg border border-border bg-card p-4 hover:bg-accent/50 transition-colors"
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <h4 className="font-medium">{liveClass.title}</h4>
                      <Badge 
                        variant={
                          liveClass.status === "LIVE" ? "default" : 
                          liveClass.status === "SCHEDULED" ? "secondary" : 
                          "outline"
                        }
                      >
                        {liveClass.status}
                      </Badge>
                    </div>
                    {liveClass.description && (
                      <p className="text-sm text-muted-foreground mb-2">{liveClass.description}</p>
                    )}
                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                      <span className="flex items-center gap-1">
                        <Clock className="h-4 w-4" />
                        {new Date(liveClass.scheduledStartTime).toLocaleString()}
                      </span>
                      {liveClass.maxParticipants && (
                        <span className="flex items-center gap-1">
                          <Users className="h-4 w-4" />
                          Max: {liveClass.maxParticipants}
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="flex gap-2">
                    {liveClass.status === "LIVE" ? (
                      <>
                        <Button 
                          variant="default"
                          size="sm"
                          onClick={() => liveClass.meetingId && handleJoinLiveClass(liveClass.meetingId)}
                          className="bg-green-600 hover:bg-green-700"
                        >
                          <Video className="h-4 w-4 mr-2" />
                          Join Now
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => liveClass.meetingId && copyMeetingLink(liveClass.meetingId)}
                        >
                          <Copy className="h-4 w-4 mr-2" />
                          Copy Link
                        </Button>
                      </>
                    ) : liveClass.status === "SCHEDULED" ? (
                      <Button 
                        variant="outline"
                        size="sm"
                        disabled
                      >
                        Scheduled
                      </Button>
                    ) : (
                      <Button 
                        variant="outline"
                        size="sm"
                        disabled
                      >
                        Ended
                      </Button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Available Quizzes Section */}
      <Card>
        <CardHeader>
          <CardTitle>Available Quizzes</CardTitle>
          <CardDescription>
            {quizzes.length > 0 
              ? `${quizzes.length} quiz(zes) available`
              : "No quizzes available yet"}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {quizzesLoading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            </div>
          ) : quizzes.length === 0 ? (
            <div className="text-center py-8">
              <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-2" />
              <p className="text-sm text-muted-foreground">
                No quizzes are currently available
              </p>
            </div>
          ) : (
            <div className="space-y-3">
              {quizzes.map((quiz) => (
                <div
                  key={quiz.id}
                  className="flex items-center justify-between rounded-lg border border-border bg-card p-4 hover:bg-accent/50 transition-colors"
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <h4 className="font-medium">{quiz.title}</h4>
                      <Badge variant={quiz.canAttempt ? "default" : "secondary"}>
                        {quiz.canAttempt ? "Available" : 
                         quiz.attemptsUsed >= (quiz.maxAttempts || 1) ? "No Attempts Left" : "Not Available"}
                      </Badge>
                    </div>
                    {quiz.description && (
                      <p className="text-sm text-muted-foreground mb-2">{quiz.description}</p>
                    )}
                    <div className="flex flex-col gap-2">
                      <div className="flex items-center gap-4 text-sm text-muted-foreground">
                        <span className="flex items-center gap-1">
                          <Clock className="h-4 w-4" />
                          {quiz.duration} mins
                        </span>
                        <span>{quiz.totalMarks} marks</span>
                        <span>Attempts: {quiz.attemptsUsed}/{quiz.maxAttempts}</span>
                        {quiz.bestScore !== null && quiz.bestScore !== undefined && (
                          <span className="flex items-center gap-1">
                            <Trophy className="h-4 w-4" />
                            Best: {quiz.bestScore}/{quiz.totalMarks}
                          </span>
                        )}
                      </div>
                      <div className="flex items-center gap-4 text-xs text-muted-foreground">
                        <span>Start: {new Date(quiz.startTime).toLocaleString()}</span>
                        <span>End: {new Date(quiz.endTime).toLocaleString()}</span>
                      </div>
                    </div>
                  </div>
                  {quiz.canAttempt ? (
                    <Button 
                      variant="default"
                      size="sm"
                      onClick={() => navigate(`/student/quiz/${quiz.id}`)}
                    >
                      Start Quiz
                    </Button>
                  ) : quiz.attemptsUsed > 0 ? (
                    <Button 
                      variant="outline"
                      size="sm"
                      onClick={() => navigate(`/student/quiz/${quiz.id}/results`)}
                    >
                      View Results
                    </Button>
                  ) : (
                    <Button 
                      variant="outline"
                      size="sm"
                      disabled
                    >
                      Not Available
                    </Button>
                  )}
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Class Materials Section */}
      <Card>
        <CardHeader>
          <CardTitle>Class Materials</CardTitle>
          <CardDescription>
            {materials.length > 0 
              ? `${materials.length} material(s) available for download`
              : "No materials available yet"}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            </div>
          ) : materials.length === 0 ? (
            <div className="text-center py-8">
              <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-2" />
              <p className="text-sm text-muted-foreground">
                No materials have been uploaded yet
              </p>
            </div>
          ) : (
            <div className="space-y-3">
              {materials.map((material) => (
                <div
                  key={material.id}
                  className="flex items-center justify-between rounded-lg border border-border bg-card p-4 hover:bg-accent/50 transition-colors"
                >
                  <div className="flex items-center gap-4 flex-1">
                    <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                      {getFileIcon(material.fileType)}
                    </div>
                    <div className="flex-1">
                      <h4 className="font-medium">{material.title}</h4>
                      <p className="text-sm text-muted-foreground">
                        {material.fileName} • {formatFileSize(material.fileSize)} • 
                        Uploaded by {material.uploadedByName} on {formatDate(material.uploadedAt)}
                      </p>
                      {material.description && (
                        <p className="text-sm text-muted-foreground mt-1">{material.description}</p>
                      )}
                    </div>
                  </div>
                  <Button 
                    variant="outline" 
                    size="sm"
                    onClick={() => handleDownload(material.id, material.fileName)}
                  >
                    <Download className="h-4 w-4 mr-2" />
                    Download
                  </Button>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Attendance Section */}
      <StudentAttendanceView classId={Number(classId)} />
    </div>
  );
}
