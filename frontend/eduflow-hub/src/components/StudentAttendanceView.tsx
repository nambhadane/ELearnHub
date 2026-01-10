import { useState, useEffect } from "react";
import { Calendar, TrendingUp, CheckCircle, XCircle, Clock, Download } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { useToast } from "@/hooks/use-toast";
import {
  AttendanceSessionDTO,
  AttendanceStatisticsDTO,
  getAttendanceSessionsByClass,
  getStudentAttendanceStatistics,
} from "@/services/api";
import * as XLSX from 'xlsx';

interface StudentAttendanceViewProps {
  classId: number;
}

export function StudentAttendanceView({ classId }: StudentAttendanceViewProps) {
  const [sessions, setSessions] = useState<AttendanceSessionDTO[]>([]);
  const [statistics, setStatistics] = useState<AttendanceStatisticsDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const { toast } = useToast();

  useEffect(() => {
    loadAttendanceData();
  }, [classId]);

  const loadAttendanceData = async () => {
    try {
      setLoading(true);
      const user = JSON.parse(localStorage.getItem("user") || "{}");
      
      if (!user.id) {
        toast({
          title: "Error",
          description: "User not found. Please login again.",
          variant: "destructive",
        });
        return;
      }

      // Load sessions and statistics
      const [sessionsData, statsData] = await Promise.all([
        getAttendanceSessionsByClass(classId),
        getStudentAttendanceStatistics(classId, user.id),
      ]);

      // Filter sessions to only show those where student has a record
      const studentSessions = sessionsData.filter(session => 
        session.records?.some(record => record.studentId === user.id)
      );

      setSessions(studentSessions);
      setStatistics(statsData);
    } catch (error) {
      console.error("Failed to load attendance data:", error);
      toast({
        title: "Error",
        description: "Failed to load attendance data",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const exportToExcel = () => {
    try {
      const wb = XLSX.utils.book_new();
      
      // My attendance records
      const recordsData = sessions.map(session => {
        const user = JSON.parse(localStorage.getItem("user") || "{}");
        const myRecord = session.records?.find(r => r.studentId === user.id);
        
        return {
          'Session': session.title,
          'Date': new Date(session.sessionDate).toLocaleDateString(),
          'Time': session.sessionTime || 'Not specified',
          'Status': myRecord?.status || 'Not Marked',
          'Marked At': myRecord?.markedAt ? new Date(myRecord.markedAt).toLocaleString() : '',
          'Notes': myRecord?.notes || ''
        };
      });
      
      const recordsWS = XLSX.utils.json_to_sheet(recordsData);
      XLSX.utils.book_append_sheet(wb, recordsWS, 'My Attendance');
      
      // My statistics
      if (statistics) {
        const statsData = [
          ['Metric', 'Value'],
          ['Total Sessions', statistics.totalSessions],
          ['Present', statistics.presentCount],
          ['Absent', statistics.absentCount],
          ['Late', statistics.lateCount],
          ['Attendance Percentage', statistics.attendancePercentage.toFixed(1) + '%']
        ];
        
        const statsWS = XLSX.utils.aoa_to_sheet(statsData);
        XLSX.utils.book_append_sheet(wb, statsWS, 'My Statistics');
      }
      
      const today = new Date().toISOString().split('T')[0];
      const filename = `My_Attendance_${today}.xlsx`;
      
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

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "PRESENT":
        return (
          <Badge className="bg-green-500 hover:bg-green-600">
            <CheckCircle className="h-3 w-3 mr-1" />
            Present
          </Badge>
        );
      case "ABSENT":
        return (
          <Badge variant="destructive">
            <XCircle className="h-3 w-3 mr-1" />
            Absent
          </Badge>
        );
      case "LATE":
        return (
          <Badge className="bg-yellow-500 hover:bg-yellow-600">
            <Clock className="h-3 w-3 mr-1" />
            Late
          </Badge>
        );
      default:
        return <Badge variant="outline">Not Marked</Badge>;
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "PRESENT":
        return <CheckCircle className="h-5 w-5 text-green-500" />;
      case "ABSENT":
        return <XCircle className="h-5 w-5 text-red-500" />;
      case "LATE":
        return <Clock className="h-5 w-5 text-yellow-500" />;
      default:
        return <Calendar className="h-5 w-5 text-muted-foreground" />;
    }
  };

  if (loading) {
    return (
      <Card>
        <CardContent className="py-8 text-center">
          <p className="text-muted-foreground">Loading attendance data...</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header with Export */}
      <div className="flex justify-between items-center">
        <div>
          <h3 className="text-lg font-semibold">My Attendance</h3>
          <p className="text-sm text-muted-foreground">View your attendance records and statistics</p>
        </div>
        <Button 
          variant="outline" 
          onClick={exportToExcel} 
          disabled={sessions.length === 0}
        >
          <Download className="h-4 w-4 mr-2" />
          Export Excel
        </Button>
      </div>

      {/* Statistics Overview */}
      {statistics && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <TrendingUp className="h-5 w-5" />
              My Attendance Statistics
            </CardTitle>
            <CardDescription>Your overall attendance performance</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-6">
              {/* Attendance Percentage */}
              <div>
                <div className="flex justify-between items-center mb-2">
                  <span className="text-sm font-medium">Overall Attendance</span>
                  <span className="text-2xl font-bold text-primary">
                    {statistics.attendancePercentage.toFixed(1)}%
                  </span>
                </div>
                <Progress value={statistics.attendancePercentage} className="h-3" />
              </div>

              {/* Stats Grid */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="text-center p-4 bg-muted rounded-lg">
                  <Calendar className="h-5 w-5 mx-auto mb-2 text-muted-foreground" />
                  <p className="text-2xl font-bold">{statistics.totalSessions}</p>
                  <p className="text-xs text-muted-foreground">Total Sessions</p>
                </div>
                <div className="text-center p-4 bg-green-50 dark:bg-green-950 rounded-lg">
                  <CheckCircle className="h-5 w-5 mx-auto mb-2 text-green-600" />
                  <p className="text-2xl font-bold text-green-600">{statistics.presentCount}</p>
                  <p className="text-xs text-muted-foreground">Present</p>
                </div>
                <div className="text-center p-4 bg-red-50 dark:bg-red-950 rounded-lg">
                  <XCircle className="h-5 w-5 mx-auto mb-2 text-red-600" />
                  <p className="text-2xl font-bold text-red-600">{statistics.absentCount}</p>
                  <p className="text-xs text-muted-foreground">Absent</p>
                </div>
                <div className="text-center p-4 bg-yellow-50 dark:bg-yellow-950 rounded-lg">
                  <Clock className="h-5 w-5 mx-auto mb-2 text-yellow-600" />
                  <p className="text-2xl font-bold text-yellow-600">{statistics.lateCount}</p>
                  <p className="text-xs text-muted-foreground">Late</p>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Attendance Records */}
      <Card>
        <CardHeader>
          <CardTitle>Attendance Records</CardTitle>
          <CardDescription>
            {sessions.length > 0 
              ? `${sessions.length} session(s) recorded`
              : "No attendance records yet"}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {sessions.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              <Calendar className="h-12 w-12 mx-auto mb-3 opacity-50" />
              <p>No attendance records found</p>
              <p className="text-sm">Your attendance will appear here once marked by your teacher</p>
            </div>
          ) : (
            <div className="space-y-3">
              {sessions.map((session) => {
                const user = JSON.parse(localStorage.getItem("user") || "{}");
                const myRecord = session.records?.find(r => r.studentId === user.id);
                
                return (
                  <div
                    key={session.id}
                    className="flex items-center justify-between p-4 border rounded-lg hover:bg-muted/50 transition-colors"
                  >
                    <div className="flex items-start gap-4 flex-1">
                      <div className="mt-1">
                        {getStatusIcon(myRecord?.status || "")}
                      </div>
                      <div className="flex-1">
                        <h4 className="font-medium">{session.title}</h4>
                        {session.description && (
                          <p className="text-sm text-muted-foreground">{session.description}</p>
                        )}
                        <div className="flex items-center gap-4 mt-2 text-sm text-muted-foreground">
                          <span className="flex items-center gap-1">
                            <Calendar className="h-3 w-3" />
                            {new Date(session.sessionDate).toLocaleDateString()}
                          </span>
                          {session.sessionTime && (
                            <span className="flex items-center gap-1">
                              <Clock className="h-3 w-3" />
                              {session.sessionTime}
                            </span>
                          )}
                          {myRecord?.markedAt && (
                            <span className="text-xs">
                              Marked: {new Date(myRecord.markedAt).toLocaleString()}
                            </span>
                          )}
                        </div>
                        {myRecord?.notes && (
                          <p className="text-sm text-muted-foreground mt-1 italic">
                            Note: {myRecord.notes}
                          </p>
                        )}
                      </div>
                    </div>
                    <div>
                      {getStatusBadge(myRecord?.status || "")}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
