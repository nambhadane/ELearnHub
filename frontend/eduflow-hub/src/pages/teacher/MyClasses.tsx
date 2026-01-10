import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { 
  BookOpen, 
  Users, 
  FileText, 
  Clock, 
  Plus, 
  Loader2,
  UserPlus,
  Search,
  Check,
  X
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { useToast } from "@/hooks/use-toast";
import { 
  getClassesByTeacher, 
  getTeacherProfile, 
  ClassDTO,
  addStudentToClass,
  getAllStudents,
  getClassStudents,
  ParticipantDTO
} from "@/services/api";

export default function MyClasses() {
  const navigate = useNavigate();
  const { toast } = useToast();
  
  const [classes, setClasses] = useState<ClassDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [teacherId, setTeacherId] = useState<number | null>(null);
  
  // Add Student Dialog State
  const [showAddStudentDialog, setShowAddStudentDialog] = useState(false);
  const [selectedClass, setSelectedClass] = useState<ClassDTO | null>(null);
  const [allStudents, setAllStudents] = useState<ParticipantDTO[]>([]);
  const [classStudents, setClassStudents] = useState<ParticipantDTO[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedStudentIds, setSelectedStudentIds] = useState<Set<number>>(new Set());
  const [isAdding, setIsAdding] = useState(false);
  const [loadingStudents, setLoadingStudents] = useState(false);
  
  // View Students State
  const [expandedClassId, setExpandedClassId] = useState<number | null>(null);
  const [classStudentsMap, setClassStudentsMap] = useState<Map<number, ParticipantDTO[]>>(new Map());
  const [loadingClassStudents, setLoadingClassStudents] = useState<Set<number>>(new Set());

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        // Get teacher profile to get teacher ID
        const profile = await getTeacherProfile();
        setTeacherId(profile.id);
        
        // Fetch classes for this teacher
        const teacherClasses = await getClassesByTeacher(profile.id);
        setClasses(teacherClasses);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Failed to load classes";
        toast({
          title: "Error Loading Classes",
          description: message + ". Please check your backend connection.",
          variant: "destructive",
        });
        setClasses([]);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [toast]);

  // Fetch students when dialog opens
  useEffect(() => {
    if (showAddStudentDialog && selectedClass) {
      const fetchStudents = async () => {
        try {
          setLoadingStudents(true);
          
          // Fetch all students
          const students = await getAllStudents();
          setAllStudents(students);
          
          // Fetch students already in this class
          const enrolledStudents = await getClassStudents(selectedClass.id);
          setClassStudents(enrolledStudents);
          
          // Pre-select already enrolled students (so they're shown as checked)
          const enrolledIds = new Set(enrolledStudents.map(s => s.id));
          setSelectedStudentIds(enrolledIds);
        } catch (err) {
          const message = err instanceof Error ? err.message : "Failed to load students";
          toast({
            title: "Error",
            description: message,
            variant: "destructive",
          });
        } finally {
          setLoadingStudents(false);
        }
      };

      fetchStudents();
    }
  }, [showAddStudentDialog, selectedClass, toast]);

  const handleOpenAddStudentDialog = (classItem: ClassDTO) => {
    setSelectedClass(classItem);
    setShowAddStudentDialog(true);
    setSearchQuery("");
    setSelectedStudentIds(new Set());
  };

  const handleCloseDialog = () => {
    setShowAddStudentDialog(false);
    setSelectedClass(null);
    setSearchQuery("");
    setSelectedStudentIds(new Set());
  };

  // Toggle student list view
  const toggleStudentList = async (classId: number) => {
    if (expandedClassId === classId) {
      setExpandedClassId(null);
    } else {
      setExpandedClassId(classId);
      
      // Fetch students if not already loaded
      if (!classStudentsMap.has(classId)) {
        try {
          setLoadingClassStudents(prev => new Set(prev).add(classId));
          const students = await getClassStudents(classId);
          setClassStudentsMap(prev => {
            const newMap = new Map(prev);
            newMap.set(classId, students);
            return newMap;
          });
        } catch (err) {
          toast({
            title: "Error",
            description: "Failed to load students",
            variant: "destructive",
          });
        } finally {
          setLoadingClassStudents(prev => {
            const newSet = new Set(prev);
            newSet.delete(classId);
            return newSet;
          });
        }
      }
    }
  };

  const toggleStudentSelection = (studentId: number) => {
    const newSelection = new Set(selectedStudentIds);
    if (newSelection.has(studentId)) {
      newSelection.delete(studentId);
    } else {
      newSelection.add(studentId);
    }
    setSelectedStudentIds(newSelection);
  };

  const handleAddStudents = async () => {
    if (!selectedClass) return;

    const studentsToAdd = Array.from(selectedStudentIds).filter(
      id => !classStudents.some(s => s.id === id)
    );

    if (studentsToAdd.length === 0) {
      toast({
        title: "No Changes",
        description: "No new students selected to add.",
        variant: "default",
      });
      return;
    }

    try {
      setIsAdding(true);

      // Add each selected student
      const addPromises = studentsToAdd.map(studentId =>
        addStudentToClass(selectedClass.id, studentId)
      );

      await Promise.all(addPromises);

      toast({
        title: "Success",
        description: `Successfully added ${studentsToAdd.length} student(s) to ${selectedClass.name}`,
        variant: "default",
      });

      // Refresh class students list
      const updatedEnrolledStudents = await getClassStudents(selectedClass.id);
      setClassStudents(updatedEnrolledStudents);

      // Update selected IDs to include newly added
      const updatedIds = new Set([
        ...Array.from(selectedStudentIds),
        ...studentsToAdd
      ]);
      setSelectedStudentIds(updatedIds);
      
      // Refresh classes to update student count
      if (teacherId) {
        const updatedClasses = await getClassesByTeacher(teacherId);
        setClasses(updatedClasses);
        
        // Update the students map if this class is expanded
        if (expandedClassId === selectedClass.id) {
          setClassStudentsMap(prev => {
            const newMap = new Map(prev);
            newMap.set(selectedClass.id, updatedEnrolledStudents);
            return newMap;
          });
        }
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to add students";
      toast({
        title: "Error",
        description: message,
        variant: "destructive",
      });
    } finally {
      setIsAdding(false);
    }
  };

  // Filter students based on search query
  const filteredStudents = allStudents.filter(student =>
    student.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    student.username.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // Separate already enrolled vs not enrolled
  const enrolledStudentIds = new Set(classStudents.map(s => s.id));
  const availableStudents = filteredStudents.filter(s => !enrolledStudentIds.has(s.id));
  const enrolledStudents = filteredStudents.filter(s => enrolledStudentIds.has(s.id));

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div className="space-y-2">
            <h1 className="text-3xl font-bold tracking-tight">My Classes</h1>
            <p className="text-muted-foreground">
              Manage your classes and course content
            </p>
          </div>
        </div>
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">My Classes</h1>
          <p className="text-muted-foreground">
            Manage your classes and course content
          </p>
        </div>
        <Button onClick={() => navigate("/teacher/classes/create")}>
          <Plus className="mr-2 h-4 w-4" />
          Create New Class
        </Button>
      </div>

      {classes.length === 0 ? (
        <Card className="glass-card">
          <CardContent className="flex flex-col items-center justify-center py-12">
            <BookOpen className="h-12 w-12 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">No classes yet</h3>
            <p className="text-muted-foreground text-center mb-4">
              Create your first class to get started
            </p>
            <Button onClick={() => navigate("/teacher/classes/create")}>
              <Plus className="mr-2 h-4 w-4" />
              Create Your First Class
            </Button>
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2">
            {classes.map((classItem) => (
              <Card key={classItem.id} className="glass-card hover:shadow-lg transition-all group">
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <div className="space-y-1">
                      <CardTitle className="text-lg">{classItem.name}</CardTitle>
                      <CardDescription>
                        Class ID: {classItem.id} • Course ID: {classItem.courseId}
                      </CardDescription>
                    </div>
                    <Badge variant="secondary">
                      <Users className="mr-1 h-3 w-3" />
                      {classItem.studentCount ?? 0}
                    </Badge>
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex gap-2 pt-2">
                    <Button 
                      variant="outline" 
                      className="flex-1"
                      onClick={() => handleOpenAddStudentDialog(classItem)}
                    >
                      <UserPlus className="mr-2 h-4 w-4" />
                      Add Students
                    </Button>
                    <Button 
                      variant="outline" 
                      className="flex-1"
                      onClick={() => navigate(`/teacher/classes/${classItem.id}/materials`)}
                    >
                      <FileText className="mr-2 h-4 w-4" />
                      Materials
                    </Button>
                    <Button 
                      className="flex-1"
                      onClick={() => navigate(`/teacher/classes/${classItem.id}`)}
                    >
                      View Class
                    </Button>
                  </div>
                  
                  {/* View Students Button */}
                  {(classItem.studentCount ?? 0) > 0 && (
                    <Button
                      variant="ghost"
                      size="sm"
                      className="w-full"
                      onClick={() => toggleStudentList(classItem.id)}
                      disabled={loadingClassStudents.has(classItem.id)}
                    >
                      {loadingClassStudents.has(classItem.id) ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          Loading...
                        </>
                      ) : expandedClassId === classItem.id ? (
                        <>
                          <X className="mr-2 h-4 w-4" />
                          Hide Students
                        </>
                      ) : (
                        <>
                          <Users className="mr-2 h-4 w-4" />
                          View Students ({classItem.studentCount})
                        </>
                      )}
                    </Button>
                  )}
                  
                  {/* Student List */}
                  {expandedClassId === classItem.id && (
                    <div className="border-t pt-4 mt-2">
                      <div className="flex items-center justify-between mb-3">
                        <h4 className="text-sm font-semibold">
                          Enrolled Students ({classStudentsMap.get(classItem.id)?.length ?? 0})
                        </h4>
                      </div>
                      
                      {loadingClassStudents.has(classItem.id) ? (
                        <div className="flex items-center justify-center py-4">
                          <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
                        </div>
                      ) : classStudentsMap.get(classItem.id)?.length === 0 ? (
                        <p className="text-sm text-muted-foreground text-center py-4">
                          No students enrolled yet.
                        </p>
                      ) : (
                        <div className="space-y-2 max-h-48 overflow-y-auto">
                          {classStudentsMap.get(classItem.id)?.map((student) => (
                            <div
                              key={student.id}
                              className="flex items-center justify-between p-2 rounded-lg border bg-muted/50"
                            >
                              <div className="flex items-center gap-2">
                                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary/10">
                                  <Users className="h-4 w-4 text-primary" />
                                </div>
                                <div>
                                  <p className="text-sm font-medium">
                                    {student.name || student.username}
                                  </p>
                                  <p className="text-xs text-muted-foreground">
                                    @{student.username}
                                  </p>
                                </div>
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>

          <Card className="glass-card">
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
              <CardDescription>Common tasks for managing your classes</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 md:grid-cols-3">
                <Button variant="outline" className="h-20 flex-col gap-2">
                  <Plus className="h-6 w-6" />
                  <span>Upload Material</span>
                </Button>
                <Button 
                  variant="outline" 
                  className="h-20 flex-col gap-2"
                  onClick={() => navigate("/teacher/create-assignment")}
                >
                  <FileText className="h-6 w-6" />
                  <span>Create Assignment</span>
                </Button>
                <Button variant="outline" className="h-20 flex-col gap-2">
                  <Users className="h-6 w-6" />
                  <span>View Students</span>
                </Button>
              </div>
            </CardContent>
          </Card>
        </>
      )}

      {/* Add Student Dialog */}
      <Dialog open={showAddStudentDialog} onOpenChange={setShowAddStudentDialog}>
        <DialogContent className="max-w-2xl max-h-[80vh] overflow-hidden flex flex-col">
          <DialogHeader>
            <DialogTitle>Add Students to {selectedClass?.name}</DialogTitle>
            <DialogDescription>
              Select students to add to this class. They will be able to see this class in their dashboard.
            </DialogDescription>
          </DialogHeader>

          <div className="flex-1 overflow-hidden flex flex-col space-y-4">
            {/* Search Bar */}
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search students by name or username..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10"
              />
            </div>

            {/* Students List */}
            <div className="flex-1 overflow-y-auto border rounded-lg p-4 space-y-2">
              {loadingStudents ? (
                <div className="flex items-center justify-center py-8">
                  <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
                </div>
              ) : (
                <>
                  {/* Already Enrolled Students */}
                  {enrolledStudents.length > 0 && (
                    <div className="space-y-2 mb-4">
                      <h4 className="text-sm font-semibold text-muted-foreground">
                        Already Enrolled ({enrolledStudents.length})
                      </h4>
                      {enrolledStudents.map((student) => (
                        <div
                          key={student.id}
                          className="flex items-center justify-between p-3 rounded-lg border bg-muted/50"
                        >
                          <div className="flex items-center gap-3">
                            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
                              <Users className="h-5 w-5 text-primary" />
                            </div>
                            <div>
                              <p className="font-medium text-sm">
                                {student.name || student.username}
                              </p>
                              <p className="text-xs text-muted-foreground">
                                @{student.username}
                              </p>
                            </div>
                          </div>
                          <Badge variant="secondary">Enrolled</Badge>
                        </div>
                      ))}
                    </div>
                  )}

                  {/* Available Students */}
                  <div className="space-y-2">
                    {enrolledStudents.length > 0 && (
                      <h4 className="text-sm font-semibold text-muted-foreground">
                        Available Students ({availableStudents.length})
                      </h4>
                    )}
                    {availableStudents.length === 0 ? (
                      <div className="text-center py-8">
                        <Users className="h-12 w-12 text-muted-foreground mx-auto mb-2" />
                        <p className="text-sm text-muted-foreground">
                          {searchQuery ? "No students found matching your search" : "No students available"}
                        </p>
                      </div>
                    ) : (
                      availableStudents.map((student) => {
                        const isSelected = selectedStudentIds.has(student.id);
                        return (
                          <div
                            key={student.id}
                            onClick={() => toggleStudentSelection(student.id)}
                            className={`flex items-center justify-between p-3 rounded-lg border cursor-pointer transition-colors ${
                              isSelected
                                ? "bg-primary/10 border-primary"
                                : "hover:bg-muted/50"
                            }`}
                          >
                            <div className="flex items-center gap-3">
                              <div className={`flex h-10 w-10 items-center justify-center rounded-full ${
                                isSelected ? "bg-primary" : "bg-muted"
                              }`}>
                                {isSelected ? (
                                  <Check className="h-5 w-5 text-primary-foreground" />
                                ) : (
                                  <Users className="h-5 w-5 text-muted-foreground" />
                                )}
                              </div>
                              <div>
                                <p className="font-medium text-sm">
                                  {student.name || student.username}
                                </p>
                                <p className="text-xs text-muted-foreground">
                                  @{student.username}
                                </p>
                              </div>
                            </div>
                            {isSelected && (
                              <Badge variant="default">Selected</Badge>
                            )}
                          </div>
                        );
                      })
                    )}
                  </div>
                </>
              )}
            </div>

            {/* Selection Summary */}
            {selectedStudentIds.size > 0 && (
              <div className="text-sm text-muted-foreground">
                {selectedStudentIds.size} student(s) selected
              </div>
            )}
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={handleCloseDialog}>
              Cancel
            </Button>
            <Button
              onClick={handleAddStudents}
              disabled={isAdding || selectedStudentIds.size === 0 || loadingStudents}
            >
              {isAdding ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Adding...
                </>
              ) : (
                <>
                  <UserPlus className="mr-2 h-4 w-4" />
                  Add Selected Students
                </>
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
