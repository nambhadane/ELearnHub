import { useState, useEffect } from "react";
import { Calendar, Users, Plus, Check, X, Clock, TrendingUp, Download } from "lucide-react";
import * as XLSX from 'xlsx';
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/hooks/use-toast";
import {
  AttendanceSessionDTO,
  AttendanceRecordDTO,
  getAttendanceSessionsByClass,
  createAttendanceSession,
  markBulkAttendance,
  getClassAttendanceStatistics,
  AttendanceStatisticsDTO,
} from "@/services/api";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

interface AttendanceManagerProps {
  classId: number;
  students: Array<{ id: number; name: string }>;
}

export function AttendanceManager({ classId, students }: AttendanceManagerProps) {
  const [sessions, setSessions] = useState<AttendanceSessionDTO[]>([]);
  const [statistics, setStatistics] = useState<AttendanceStatisticsDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [showMarkDialog, setShowMarkDialog] = useState(false);
  const [selectedSession, setSelectedSession] = useState<AttendanceSessionDTO | null>(null);
  const [attendanceMap, setAttendanceMap] = useState<Record<number, string>>({});
  const { toast } = useToast();

  useEffect(() => {
    loadSessions();
    loadStatistics();
  }, [classId]);

  const loadSessions = async () => {
    try {
      setLoading(true);
      const data = await getAttendanceSessionsByClass(classId);
      setSessions(data);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load attendance sessions",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const loadStatistics = async () => {
    try {
      const data = await getClassAttendanceStatistics(classId);
      setStatistics(data);
    } catch (error) {
      console.error("Failed to load statistics", error);
    }
  };

  const handleCreateSession = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);

    try {
      const user = JSON.parse(localStorage.getItem("user") || "{}");
      
      if (!user.id) {
        toast({
          title: "Error",
          description: "User not found. Please login again.",
          variant: "destructive",
        });
        return;
      }

      const session: AttendanceSessionDTO = {
        classId,
        sessionDate: formData.get("sessionDate") as string,
        sessionTime: formData.get("sessionTime") as string,
        title: formData.get("title") as string,
        description: formData.get("description") as string,
        createdBy: user.id,
      };

      console.log("Creating session with data:", session);

      await createAttendanceSession(session);
      toast({
        title: "Success",
        description: "Attendance session created",
      });
      setShowCreateDialog(false);
      loadSessions();
    } catch (error) {
      console.error("Error creating session:", error);
      toast({
        title: "Error",
        description: "Failed to create session",
        variant: "destructive",
      });
    }
  };

  const handleMarkAttendance = (session: AttendanceSessionDTO) => {
    setSelectedSession(session);
    // Initialize attendance map with existing records or default to ABSENT
    const initialMap: Record<number, string> = {};
    students.forEach((student) => {
      const existingRecord = session.records?.find((r) => r.studentId === student.id);
      initialMap[student.id] = existingRecord?.status || "ABSENT";
    });
    setAttendanceMap(initialMap);
    setShowMarkDialog(true);
  };

  const handleSaveAttendance = async () => {
    if (!selectedSession?.id) return;

    try {
      await markBulkAttendance(selectedSession.id, attendanceMap);
      toast({
        title: "Success",
        description: "Attendance marked successfully",
      });
      setShowMarkDialog(false);
      loadSessions();
      loadStatistics();
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to mark attendance",
        variant: "destructive",
      });
    }
  };

  const toggleStatus = (studentId: number) => {
    const currentStatus = attendanceMap[studentId] || "ABSENT";
    const nextStatus = currentStatus === "PRESENT" ? "ABSENT" : currentStatus === "ABSENT" ? "LATE" : "PRESENT";
    setAttendanceMap({ ...attendanceMap, [studentId]: nextStatus });
  };

  const exportToExcel = () => {
    try {
      const wb = XLSX.utils.book_new();
      
      // Sessions sheet
      const sessionsData = sessions.map(session => ({
        'Session Title': session.title,
        'Date': new Date(session.sessionDate).toLocaleDateString(),
        'Time': session.sessionTime || 'Not specified',
        'Total Students': session.totalStudents || 0,
        'Present': session.presentCount || 0,
        'Absent': session.absentCount || 0,
        'Late': session.lateCount || 0,
        'Attendance %': session.attendancePercentage?.toFixed(1) + '%' || '0%',
        'Description': session.description || ''
      }));
      
      const sessionsWS = XLSX.utils.json_to_sheet(sessionsData);
      XLSX.utils.book_append_sheet(wb, sessionsWS, 'Sessions');
      
      // Statistics sheet
      const statsData = statistics.map(stat => ({
        'Student Name': stat.studentName,
        'Total Sessions': stat.totalSessions,
        'Present': stat.presentCount,
        'Absent': stat.absentCount,
        'Late': stat.lateCount,
        'Attendance %': stat.attendancePercentage.toFixed(1) + '%'
      }));
      
      const statsWS = XLSX.utils.json_to_sheet(statsData);
      XLSX.utils.book_append_sheet(wb, statsWS, 'Student Statistics');
      
      // Detailed records sheet
      const detailedData: any[] = [];
      sessions.forEach(session => {
        if (session.records) {
          session.records.forEach(record => {
            detailedData.push({
              'Session': session.title,
              'Date': new Date(session.sessionDate).toLocaleDateString(),
              'Student': record.studentName,
              'Status': record.status,
              'Marked At': record.markedAt ? new Date(record.markedAt).toLocaleString() : '',
              'Notes': record.notes || ''
            });
          });
        }
      });
      
      if (detailedData.length > 0) {
        const detailedWS = XLSX.utils.json_to_sheet(detailedData);
        XLSX.utils.book_append_sheet(wb, detailedWS, 'Detailed Records');
      }
      
      const today = new Date().toISOString().split('T')[0];
      const filename = `Attendance_Report_${today}.xlsx`;
      
      XLSX.writeFile(wb, filename);
      
      toast({
        title: "Success",
        description: "Attendance report downloaded successfully",
      });
    } catch (error) {
      console.error('Export error:', error);
      toast({
        title: "Error",
        description: "Failed to export attendance report",
        variant: "destructive",
      });
    }
  };

  const exportSessionToExcel = (session: AttendanceSessionDTO) => {
    try {
      const wb = XLSX.utils.book_new();
      
      const sessionInfo = [
        ['Session Title', session.title],
        ['Date', new Date(session.sessionDate).toLocaleDateString()],
        ['Time', session.sessionTime || 'Not specified'],
        ['Description', session.description || ''],
        ['Total Students', session.totalStudents || 0],
        ['Present', session.presentCount || 0],
        ['Absent', session.absentCount || 0],
        ['Late', session.lateCount || 0],
        ['Attendance %', (session.attendancePercentage?.toFixed(1) || '0') + '%'],
        [''],
        ['Student Name', 'Status', 'Marked At', 'Notes']
      ];
      
      if (session.records) {
        session.records.forEach(record => {
          sessionInfo.push([
            record.studentName || 'Unknown',
            record.status,
            record.markedAt ? new Date(record.markedAt).toLocaleString() : '',
            record.notes || ''
          ]);
        });
      }
      
      const ws = XLSX.utils.aoa_to_sheet(sessionInfo);
      XLSX.utils.book_append_sheet(wb, ws, 'Session Report');
      
      const filename = `${session.title}_${new Date(session.sessionDate).toISOString().split('T')[0]}.xlsx`;
      XLSX.writeFile(wb, filename);
      
      toast({
        title: "Success",
        description: "Session report downloaded successfully",
      });
    } catch (error) {
      console.error('Export error:', error);
      toast({
        title: "Error",
        description: "Failed to export session report",
        variant: "destructive",
      });
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "PRESENT":
        return <Badge className="bg-green-500">Present</Badge>;
      case "ABSENT":
        return <Badge variant="destructive">Absent</Badge>;
      case "LATE":
        return <Badge className="bg-yellow-500">Late</Badge>;
      default:
        return <Badge variant="outline">{status}</Badge>;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "PRESENT":
        return "bg-green-100 dark:bg-green-900 border-green-300 dark:border-green-700";
      case "ABSENT":
        return "bg-red-100 dark:bg-red-900 border-red-300 dark:border-red-700";
      case "LATE":
        return "bg-yellow-100 dark:bg-yellow-900 border-yellow-300 dark:border-yellow-700";
      default:
        return "";
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h3 className="text-lg font-semibold">Attendance Management</h3>
          <p className="text-sm text-muted-foreground">Track and manage student attendance</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={exportToExcel} disabled={sessions.length === 0}>
            <Download className="h-4 w-4 mr-2" />
            Export Excel
          </Button>
          <Button onClick={() => setShowCreateDialog(true)}>
            <Plus className="h-4 w-4 mr-2" />
            Create Session
          </Button>
        </div>
      </div>

      <Tabs defaultValue="sessions" className="w-full">
        <TabsList>
          <TabsTrigger value="sessions">Sessions</TabsTrigger>
          <TabsTrigger value="statistics">Statistics</TabsTrigger>
        </TabsList>

        <TabsContent value="sessions" className="space-y-4">
          {loading ? (
            <div className="text-center py-8">Loading...</div>
          ) : sessions.length === 0 ? (
            <Card>
              <CardContent className="py-8 text-center text-muted-foreground">
                No attendance sessions yet. Create your first session to get started.
              </CardContent>
            </Card>
          ) : (
            sessions.map((session) => (
              <Card key={session.id}>
                <CardHeader>
                  <div className="flex justify-between items-start">
                    <div>
                      <CardTitle className="text-lg">{session.title}</CardTitle>
                      <CardDescription>{session.description}</CardDescription>
                    </div>
                    <div className="flex gap-2">
                      <Button variant="outline" size="sm" onClick={() => exportSessionToExcel(session)}>
                        <Download className="h-4 w-4 mr-1" />
                        Export
                      </Button>
                      <Button size="sm" onClick={() => handleMarkAttendance(session)}>
                        <Check className="h-4 w-4 mr-2" />
                        Mark Attendance
                      </Button>
                    </div>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-6 text-sm">
                    <span className="flex items-center gap-1">
                      <Calendar className="h-4 w-4" />
                      {new Date(session.sessionDate).toLocaleDateString()}
                    </span>
                    {session.sessionTime && (
                      <span className="flex items-center gap-1">
                        <Clock className="h-4 w-4" />
                        {session.sessionTime}
                      </span>
                    )}
                    <span className="flex items-center gap-1">
                      <Users className="h-4 w-4" />
                      {session.totalStudents || 0} students
                    </span>
                  </div>
                  {session.totalStudents && session.totalStudents > 0 && (
                    <div className="mt-4 flex gap-4">
                      <div className="flex items-center gap-2">
                        <div className="w-3 h-3 rounded-full bg-green-500"></div>
                        <span className="text-sm">Present: {session.presentCount || 0}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <div className="w-3 h-3 rounded-full bg-red-500"></div>
                        <span className="text-sm">Absent: {session.absentCount || 0}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <div className="w-3 h-3 rounded-full bg-yellow-500"></div>
                        <span className="text-sm">Late: {session.lateCount || 0}</span>
                      </div>
                      <div className="flex items-center gap-2 ml-auto">
                        <TrendingUp className="h-4 w-4" />
                        <span className="text-sm font-medium">{session.attendancePercentage?.toFixed(1)}%</span>
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>
            ))
          )}
        </TabsContent>

        <TabsContent value="statistics" className="space-y-4">
          {statistics.length === 0 ? (
            <Card>
              <CardContent className="py-8 text-center text-muted-foreground">
                No attendance data yet. Mark attendance to see statistics.
              </CardContent>
            </Card>
          ) : (
            <Card>
              <CardHeader>
                <div className="flex justify-between items-start">
                  <div>
                    <CardTitle>Class Attendance Statistics</CardTitle>
                    <CardDescription>Overall attendance performance</CardDescription>
                  </div>
                  <Button variant="outline" size="sm" onClick={exportToExcel} disabled={statistics.length === 0}>
                    <Download className="h-4 w-4 mr-1" />
                    Export Stats
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {statistics.map((stat) => (
                    <div key={stat.studentId} className="flex items-center justify-between p-3 border rounded-lg">
                      <div className="flex-1">
                        <p className="font-medium">{stat.studentName}</p>
                        <p className="text-sm text-muted-foreground">
                          {stat.presentCount} present, {stat.absentCount} absent, {stat.lateCount} late
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="text-2xl font-bold">{stat.attendancePercentage.toFixed(1)}%</p>
                        <p className="text-xs text-muted-foreground">{stat.totalSessions} sessions</p>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}
        </TabsContent>
      </Tabs>

      {/* Create Session Dialog */}
      <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create Attendance Session</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleCreateSession} className="space-y-4">
            <div>
              <Label htmlFor="title">Title *</Label>
              <Input id="title" name="title" required placeholder="e.g., Monday Class" />
            </div>
            <div>
              <Label htmlFor="sessionDate">Date *</Label>
              <Input id="sessionDate" name="sessionDate" type="date" required />
            </div>
            <div>
              <Label htmlFor="sessionTime">Time</Label>
              <Input id="sessionTime" name="sessionTime" type="time" />
            </div>
            <div>
              <Label htmlFor="description">Description</Label>
              <Textarea id="description" name="description" placeholder="Optional notes" />
            </div>
            <div className="flex gap-2 justify-end">
              <Button type="button" variant="outline" onClick={() => setShowCreateDialog(false)}>
                Cancel
              </Button>
              <Button type="submit">Create Session</Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>

      {/* Mark Attendance Dialog */}
      <Dialog open={showMarkDialog} onOpenChange={setShowMarkDialog}>
        <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Mark Attendance - {selectedSession?.title}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <div className="flex gap-2 text-sm text-muted-foreground mb-4">
              <span>Click to toggle: Present → Absent → Late → Present</span>
            </div>
            {students.map((student) => {
              const status = attendanceMap[student.id] || "ABSENT";
              return (
                <div
                  key={student.id}
                  className={`flex items-center justify-between p-3 border rounded-lg cursor-pointer transition-colors ${getStatusColor(status)}`}
                  onClick={() => toggleStatus(student.id)}
                >
                  <span className="font-medium">{student.name}</span>
                  {getStatusBadge(status)}
                </div>
              );
            })}
          </div>
          <div className="flex gap-2 justify-end mt-4">
            <Button variant="outline" onClick={() => setShowMarkDialog(false)}>
              Cancel
            </Button>
            <Button onClick={handleSaveAttendance}>Save Attendance</Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
