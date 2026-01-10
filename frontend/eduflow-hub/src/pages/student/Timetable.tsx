import { useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Calendar, Clock, MapPin, FileText, Loader2 } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { getMyTimetable, Schedule } from "@/services/api";

const DAYS = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];

export default function Timetable() {
  const { toast } = useToast();
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTimetable();
  }, []);

  const fetchTimetable = async () => {
    try {
      setLoading(true);
      const data = await getMyTimetable();
      setSchedules(data);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load timetable",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const getSchedulesForDay = (day: string) => {
    return schedules
      .filter((s) => s.dayOfWeek === day)
      .sort((a, b) => a.startTime.localeCompare(b.startTime));
  };

  const formatTime = (time: string) => {
    // time is in format "HH:mm:ss" or "HH:mm"
    const [hours, minutes] = time.split(":");
    const hour = parseInt(hours);
    const ampm = hour >= 12 ? "PM" : "AM";
    const displayHour = hour % 12 || 12;
    return `${displayHour}:${minutes} ${ampm}`;
  };

  const getColorForClass = (index: number) => {
    const colors = [
      "bg-blue-500",
      "bg-green-500",
      "bg-purple-500",
      "bg-orange-500",
      "bg-pink-500",
      "bg-cyan-500",
    ];
    return colors[index % colors.length];
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">My Timetable</h1>
          <p className="text-muted-foreground">View your weekly class schedule</p>
        </div>
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">My Timetable</h1>
        <p className="text-muted-foreground">
          Your weekly class schedule • {schedules.length} class{schedules.length !== 1 ? "es" : ""} scheduled
        </p>
      </div>

      {schedules.length === 0 ? (
        <Card className="glass-card">
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Calendar className="h-16 w-16 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">No Schedule Yet</h3>
            <p className="text-sm text-muted-foreground text-center">
              Your teachers haven't set up class schedules yet
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4">
          {DAYS.map((day) => {
            const daySchedules = getSchedulesForDay(day);
            if (daySchedules.length === 0) return null;

            return (
              <Card key={day} className="glass-card">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Calendar className="h-5 w-5" />
                    {day.charAt(0) + day.slice(1).toLowerCase()}
                  </CardTitle>
                  <CardDescription>{daySchedules.length} class{daySchedules.length !== 1 ? "es" : ""}</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    {daySchedules.map((schedule, index) => (
                      <div
                        key={schedule.id}
                        className="flex items-start gap-4 rounded-lg border p-4 hover:bg-accent/50 transition-colors"
                      >
                        <div className={`h-12 w-1 rounded-full ${getColorForClass(index)}`} />
                        <div className="flex-1 space-y-2">
                          <div className="flex items-start justify-between">
                            <div>
                              <h4 className="font-semibold">{schedule.className}</h4>
                              <p className="text-sm text-muted-foreground">{schedule.courseName}</p>
                            </div>
                            <Badge variant="secondary">
                              <Clock className="h-3 w-3 mr-1" />
                              {formatTime(schedule.startTime)} - {formatTime(schedule.endTime)}
                            </Badge>
                          </div>
                          
                          <div className="flex flex-wrap gap-3 text-sm text-muted-foreground">
                            {schedule.room && (
                              <span className="flex items-center gap-1">
                                <FileText className="h-3 w-3" />
                                Room: {schedule.room}
                              </span>
                            )}
                            {schedule.location && (
                              <span className="flex items-center gap-1">
                                <MapPin className="h-3 w-3" />
                                {schedule.location}
                              </span>
                            )}
                          </div>
                          
                          {schedule.notes && (
                            <p className="text-sm text-muted-foreground italic">
                              Note: {schedule.notes}
                            </p>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}
