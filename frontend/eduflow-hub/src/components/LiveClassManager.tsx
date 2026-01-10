import { useState, useEffect } from "react";
import { Plus, Video, Edit, Trash2, Play, StopCircle, Calendar, Users, Clock, Link2, Copy } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/hooks/use-toast";
import { 
  LiveClassDTO, 
  getLiveClassesByClass, 
  cancelLiveClass, 
  startLiveClass,
  endLiveClass,
  scheduleLiveClass,
  updateLiveClass
} from "@/services/api";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useNavigate } from "react-router-dom";

interface LiveClassManagerProps {
  classId: number;
}

export function LiveClassManager({ classId }: LiveClassManagerProps) {
  const [liveClasses, setLiveClasses] = useState<LiveClassDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [showScheduleDialog, setShowScheduleDialog] = useState(false);
  const { toast } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    loadLiveClasses();
  }, [classId]);

  const loadLiveClasses = async () => {
    try {
      setLoading(true);
      const data = await getLiveClassesByClass(classId);
      setLiveClasses(data);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load live classes",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleStart = async (id: number) => {
    try {
      const updatedClass = await startLiveClass(id);
      toast({
        title: "Success",
        description: "Live class started",
      });
      loadLiveClasses(); // Refresh the list
      
      // Open Jitsi directly
      if (updatedClass.meetingId) {
        const user = JSON.parse(localStorage.getItem("user") || "{}");
        const displayName = user.name || "Teacher";
        const jitsiUrl = `https://meet.jit.si/${updatedClass.meetingId}#userInfo.displayName="${encodeURIComponent(displayName)}"`;
        window.open(jitsiUrl, '_blank');
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to start live class",
        variant: "destructive",
      });
    }
  };

  const handleEnd = async (id: number) => {
    if (!confirm("Are you sure you want to end this live class?")) return;

    try {
      await endLiveClass(id);
      toast({
        title: "Success",
        description: "Live class ended",
      });
      loadLiveClasses();
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to end live class",
        variant: "destructive",
      });
    }
  };

  const handleCancel = async (id: number) => {
    if (!confirm("Are you sure you want to cancel this live class?")) return;

    try {
      await cancelLiveClass(id);
      toast({
        title: "Success",
        description: "Live class cancelled",
      });
      loadLiveClasses();
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to cancel live class",
        variant: "destructive",
      });
    }
  };

  const copyMeetingLink = (meetingId: string) => {
    const meetingUrl = `https://meet.jit.si/${meetingId}`;
    navigator.clipboard.writeText(meetingUrl);
    toast({
      title: "Link Copied!",
      description: "Meeting link copied to clipboard. Share it to join from other devices.",
    });
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "SCHEDULED":
        return <Badge variant="secondary">Scheduled</Badge>;
      case "LIVE":
        return <Badge variant="default" className="bg-red-500">Live Now</Badge>;
      case "ENDED":
        return <Badge variant="outline">Ended</Badge>;
      case "CANCELLED":
        return <Badge variant="destructive">Cancelled</Badge>;
      default:
        return <Badge>{status}</Badge>;
    }
  };

  if (loading) {
    return <div className="text-center py-8">Loading live classes...</div>;
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h3 className="text-lg font-semibold">Live Classes</h3>
        <Button onClick={() => setShowScheduleDialog(true)}>
          <Plus className="h-4 w-4 mr-2" />
          Schedule Live Class
        </Button>
      </div>

      {liveClasses.length === 0 ? (
        <Card>
          <CardContent className="py-8 text-center text-muted-foreground">
            No live classes scheduled. Create your first live class to get started.
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4">
          {liveClasses.map((liveClass) => (
            <Card key={liveClass.id}>
              <CardHeader>
                <div className="flex justify-between items-start">
                  <div>
                    <CardTitle className="text-lg">{liveClass.title}</CardTitle>
                    <CardDescription>{liveClass.description}</CardDescription>
                  </div>
                  {getStatusBadge(liveClass.status!)}
                </div>
              </CardHeader>
              <CardContent>
                <div className="flex items-center gap-4 text-sm text-muted-foreground mb-4">
                  <span className="flex items-center gap-1">
                    <Calendar className="h-4 w-4" />
                    {new Date(liveClass.scheduledStartTime).toLocaleDateString()}
                  </span>
                  <span className="flex items-center gap-1">
                    <Clock className="h-4 w-4" />
                    {new Date(liveClass.scheduledStartTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </span>
                  <span className="flex items-center gap-1">
                    <Users className="h-4 w-4" />
                    Max {liveClass.maxParticipants} participants
                  </span>
                </div>
                <div className="space-y-3">
                  <div className="flex gap-2 flex-wrap">
                    {liveClass.status === "SCHEDULED" && (
                      <>
                        <Button 
                          variant="default" 
                          size="sm"
                          onClick={() => liveClass.id && handleStart(liveClass.id)}
                        >
                          <Play className="h-4 w-4 mr-1" />
                          Start Class
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => liveClass.id && handleCancel(liveClass.id)}
                        >
                          <Trash2 className="h-4 w-4 mr-1" />
                          Cancel
                        </Button>
                      </>
                    )}
                    {liveClass.status === "LIVE" && (
                      <>
                        <Button 
                          variant="default" 
                          size="sm"
                          onClick={() => {
                            if (liveClass.meetingId) {
                              const user = JSON.parse(localStorage.getItem("user") || "{}");
                              const displayName = user.name || "Teacher";
                              const jitsiUrl = `https://meet.jit.si/${liveClass.meetingId}#userInfo.displayName="${encodeURIComponent(displayName)}"`;
                              window.open(jitsiUrl, '_blank');
                            }
                          }}
                        >
                          <Video className="h-4 w-4 mr-1" />
                          Join Class
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => liveClass.meetingId && copyMeetingLink(liveClass.meetingId)}
                        >
                          <Copy className="h-4 w-4 mr-1" />
                          Copy Link
                        </Button>
                        <Button
                          variant="destructive"
                          size="sm"
                          onClick={() => liveClass.id && handleEnd(liveClass.id)}
                        >
                          <StopCircle className="h-4 w-4 mr-1" />
                          End Class
                        </Button>
                      </>
                    )}
                  </div>
                  {liveClass.status === "SCHEDULED" && (
                    <div className="p-3 bg-blue-50 dark:bg-blue-950 border border-blue-200 dark:border-blue-800 rounded-md space-y-2">
                      <p className="text-sm text-blue-800 dark:text-blue-200">
                        💡 <strong>Important:</strong> After clicking "Start Class", join the meeting immediately so students can join without waiting.
                      </p>
                      <p className="text-sm text-blue-800 dark:text-blue-200">
                        ⚠️ <strong>Tip:</strong> If Jitsi asks you to "Log-in to become moderator", DON'T click it! Just wait 5-10 seconds and the meeting will start automatically.
                      </p>
                    </div>
                  )}
                  {liveClass.status === "LIVE" && (
                    <div className="p-3 bg-green-50 dark:bg-green-950 border border-green-200 dark:border-green-800 rounded-md space-y-2">
                      <p className="text-sm text-green-800 dark:text-green-200">
                        🎥 <strong>Class is live!</strong> Students can now join. Make sure you're in the meeting room.
                      </p>
                      <div className="flex items-center gap-2 p-2 bg-white dark:bg-gray-900 rounded border border-green-300 dark:border-green-700">
                        <Link2 className="h-4 w-4 text-green-600 dark:text-green-400 flex-shrink-0" />
                        <code className="text-xs text-green-700 dark:text-green-300 break-all flex-1">
                          https://meet.jit.si/{liveClass.meetingId}
                        </code>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-6 px-2"
                          onClick={() => liveClass.meetingId && copyMeetingLink(liveClass.meetingId)}
                        >
                          <Copy className="h-3 w-3" />
                        </Button>
                      </div>
                      <p className="text-xs text-green-700 dark:text-green-300">
                        💡 <strong>Testing tip:</strong> Copy this link and open it on your mobile phone to test as a student!
                      </p>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <ScheduleLiveClassDialog
        open={showScheduleDialog}
        onClose={() => setShowScheduleDialog(false)}
        classId={classId}
        onSuccess={loadLiveClasses}
      />
    </div>
  );
}

interface ScheduleLiveClassDialogProps {
  open: boolean;
  onClose: () => void;
  classId: number;
  onSuccess: () => void;
}

function ScheduleLiveClassDialog({ open, onClose, classId, onSuccess }: ScheduleLiveClassDialogProps) {
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    scheduledStartTime: "",
    scheduledEndTime: "",
    maxParticipants: 100,
    allowRecording: false,
    allowChat: true,
    allowScreenShare: true,
  });
  const { toast } = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      await scheduleLiveClass({
        ...formData,
        classId,
      });
      
      toast({
        title: "Success",
        description: "Live class scheduled successfully",
      });
      onSuccess();
      onClose();
      setFormData({
        title: "",
        description: "",
        scheduledStartTime: "",
        scheduledEndTime: "",
        maxParticipants: 100,
        allowRecording: false,
        allowChat: true,
        allowScreenShare: true,
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to schedule live class",
        variant: "destructive",
      });
    }
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Schedule Live Class</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="title">Class Title</Label>
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
              rows={3}
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="startTime">Start Time</Label>
              <Input
                id="startTime"
                type="datetime-local"
                value={formData.scheduledStartTime}
                onChange={(e) => setFormData({ ...formData, scheduledStartTime: e.target.value })}
                required
              />
            </div>
            <div>
              <Label htmlFor="endTime">End Time</Label>
              <Input
                id="endTime"
                type="datetime-local"
                value={formData.scheduledEndTime}
                onChange={(e) => setFormData({ ...formData, scheduledEndTime: e.target.value })}
                required
              />
            </div>
          </div>
          <div>
            <Label htmlFor="maxParticipants">Max Participants</Label>
            <Input
              id="maxParticipants"
              type="number"
              min="2"
              max="200"
              value={formData.maxParticipants}
              onChange={(e) => setFormData({ ...formData, maxParticipants: parseInt(e.target.value) })}
              required
            />
          </div>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit">Schedule Class</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
