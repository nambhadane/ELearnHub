import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Calendar, Clock, MapPin, FileText, Plus, Trash2, Edit, Loader2 } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { getSchedulesByClass, createSchedule, updateSchedule, deleteSchedule, Schedule, ScheduleRequest } from "@/services/api";

const DAYS = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];

interface ScheduleManagerProps {
  classId: number;
}

export function ScheduleManager({ classId }: ScheduleManagerProps) {
  const { toast } = useToast();
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [loading, setLoading] = useState(true);
  const [showDialog, setShowDialog] = useState(false);
  const [editingSchedule, setEditingSchedule] = useState<Schedule | null>(null);
  const [formData, setFormData] = useState<ScheduleRequest>({
    classId,
    dayOfWeek: "MONDAY",
    startTime: "09:00",
    endTime: "10:00",
    room: "",
    location: "",
    notes: "",
  });

  useEffect(() => {
    fetchSchedules();
  }, [classId]);

  const fetchSchedules = async () => {
    try {
      setLoading(true);
      const data = await getSchedulesByClass(classId);
      setSchedules(data);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load schedules",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (schedule?: Schedule) => {
    if (schedule) {
      setEditingSchedule(schedule);
      setFormData({
        classId,
        dayOfWeek: schedule.dayOfWeek,
        startTime: schedule.startTime.substring(0, 5), // HH:mm
        endTime: schedule.endTime.substring(0, 5),
        room: schedule.room || "",
        location: schedule.location || "",
        notes: schedule.notes || "",
      });
    } else {
      setEditingSchedule(null);
      setFormData({
        classId,
        dayOfWeek: "MONDAY",
        startTime: "09:00",
        endTime: "10:00",
        room: "",
        location: "",
        notes: "",
      });
    }
    setShowDialog(true);
  };

  const handleSubmit = async () => {
    try {
      const scheduleData = {
        ...formData,
        startTime: formData.startTime + ":00", // Add seconds
        endTime: formData.endTime + ":00",
      };

      if (editingSchedule) {
        await updateSchedule(editingSchedule.id, scheduleData);
        toast({
          title: "Success",
          description: "Schedule updated successfully",
        });
      } else {
        await createSchedule(scheduleData);
        toast({
          title: "Success",
          description: "Schedule created successfully",
        });
      }

      setShowDialog(false);
      fetchSchedules();
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to save schedule",
        variant: "destructive",
      });
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Are you sure you want to delete this schedule?")) return;

    try {
      await deleteSchedule(id);
      toast({
        title: "Success",
        description: "Schedule deleted successfully",
      });
      fetchSchedules();
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete schedule",
        variant: "destructive",
      });
    }
  };

  const formatTime = (time: string) => {
    const [hours, minutes] = time.split(":");
    const hour = parseInt(hours);
    const ampm = hour >= 12 ? "PM" : "AM";
    const displayHour = hour % 12 || 12;
    return `${displayHour}:${minutes} ${ampm}`;
  };

  const getSchedulesForDay = (day: string) => {
    return schedules
      .filter((s) => s.dayOfWeek === day)
      .sort((a, b) => a.startTime.localeCompare(b.startTime));
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <p className="text-sm text-muted-foreground">
          {schedules.length} schedule{schedules.length !== 1 ? "s" : ""} configured
        </p>
        <Button onClick={() => handleOpenDialog()}>
          <Plus className="mr-2 h-4 w-4" />
          Add Schedule
        </Button>
      </div>

      {schedules.length === 0 ? (
        <div className="text-center py-12 border rounded-lg">
          <Calendar className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <h3 className="text-lg font-semibold mb-2">No Schedule Yet</h3>
          <p className="text-sm text-muted-foreground mb-4">
            Add class schedules to help students plan their week
          </p>
          <Button onClick={() => handleOpenDialog()}>
            <Plus className="mr-2 h-4 w-4" />
            Add First Schedule
          </Button>
        </div>
      ) : (
        <div className="space-y-4">
          {DAYS.map((day) => {
            const daySchedules = getSchedulesForDay(day);
            if (daySchedules.length === 0) return null;

            return (
              <div key={day} className="border rounded-lg p-4">
                <h4 className="font-semibold mb-3 flex items-center gap-2">
                  <Calendar className="h-4 w-4" />
                  {day.charAt(0) + day.slice(1).toLowerCase()}
                </h4>
                <div className="space-y-2">
                  {daySchedules.map((schedule) => (
                    <div
                      key={schedule.id}
                      className="flex items-start justify-between p-3 rounded-lg border bg-card hover:bg-accent/50 transition-colors"
                    >
                      <div className="flex-1 space-y-1">
                        <div className="flex items-center gap-2">
                          <Clock className="h-4 w-4 text-muted-foreground" />
                          <span className="font-medium">
                            {formatTime(schedule.startTime)} - {formatTime(schedule.endTime)}
                          </span>
                        </div>
                        {schedule.room && (
                          <div className="flex items-center gap-2 text-sm text-muted-foreground">
                            <FileText className="h-3 w-3" />
                            Room: {schedule.room}
                          </div>
                        )}
                        {schedule.location && (
                          <div className="flex items-center gap-2 text-sm text-muted-foreground">
                            <MapPin className="h-3 w-3" />
                            {schedule.location}
                          </div>
                        )}
                        {schedule.notes && (
                          <p className="text-sm text-muted-foreground italic">
                            {schedule.notes}
                          </p>
                        )}
                      </div>
                      <div className="flex gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleOpenDialog(schedule)}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleDelete(schedule.id)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Add/Edit Dialog */}
      <Dialog open={showDialog} onOpenChange={setShowDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editingSchedule ? "Edit Schedule" : "Add Schedule"}</DialogTitle>
            <DialogDescription>
              Set the class schedule for students to view in their timetable
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="dayOfWeek">Day of Week *</Label>
              <Select
                value={formData.dayOfWeek}
                onValueChange={(value) => setFormData({ ...formData, dayOfWeek: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {DAYS.map((day) => (
                    <SelectItem key={day} value={day}>
                      {day.charAt(0) + day.slice(1).toLowerCase()}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="startTime">Start Time *</Label>
                <Input
                  id="startTime"
                  type="time"
                  value={formData.startTime}
                  onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                />
              </div>
              <div>
                <Label htmlFor="endTime">End Time *</Label>
                <Input
                  id="endTime"
                  type="time"
                  value={formData.endTime}
                  onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                />
              </div>
            </div>
            <div>
              <Label htmlFor="room">Room</Label>
              <Input
                id="room"
                value={formData.room}
                onChange={(e) => setFormData({ ...formData, room: e.target.value })}
                placeholder="e.g., Room 101"
              />
            </div>
            <div>
              <Label htmlFor="location">Location</Label>
              <Input
                id="location"
                value={formData.location}
                onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                placeholder="e.g., Main Building"
              />
            </div>
            <div>
              <Label htmlFor="notes">Notes</Label>
              <Textarea
                id="notes"
                value={formData.notes}
                onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                placeholder="Any additional information for students"
                rows={3}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowDialog(false)}>
              Cancel
            </Button>
            <Button onClick={handleSubmit}>
              {editingSchedule ? "Update" : "Create"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
