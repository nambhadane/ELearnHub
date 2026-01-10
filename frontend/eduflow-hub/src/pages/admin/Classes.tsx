import { useEffect, useState } from "react";
import { BookOpen, Users, GraduationCap, Eye, Trash2, Plus, Edit, UserPlus, UserMinus } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { api } from "@/services/api";
import { useToast } from "@/hooks/use-toast";

interface ClassData {
  id: number;
  name: string;
  courseName?: string;
  courseId?: number;
  teacherId: number;
  teacherName?: string;
  studentCount?: number;
  students?: Student[];
}

interface Student {
  id: number;
  name: string;
  email: string;
  username: string;
}

interface Stats {
  totalClasses: number;
  totalStudents: number;
  totalTeachers: number;
}

interface Course {
  id: number;
  name: string;
  teacherId?: number;
  teacherName?: string;
}

interface Teacher {
  id: number;
  name: string;
  email: string;
}

interface CreateClassForm {
  name: string;
  teacherId: string;
  courseId: string;
}

interface EditClassForm {
  name: string;
}

export default function Classes() {
  const [classes, setClasses] = useState<ClassData[]>([]);
  const [stats, setStats] = useState<Stats | null>(null);
  const [courses, setCourses] = useState<Course[]>([]);
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedClass, setSelectedClass] = useState<ClassData | null>(null);
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [editingClass, setEditingClass] = useState<ClassData | null>(null);
  const [createForm, setCreateForm] = useState<CreateClassForm>({
    name: "",
    teacherId: "",
    courseId: "",
  });
  const [editForm, setEditForm] = useState<EditClassForm>({
    name: "",
  });
  const { toast } = useToast();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const [classesRes, statsRes, coursesRes, teachersRes] = await Promise.all([
        api.get("/admin/classes"),
        api.get("/admin/stats"),
        api.get("/admin/courses"),
        api.get("/admin/users?role=TEACHER")
      ]);
      
      setClasses(classesRes.data);
      setStats({
        totalClasses: statsRes.data.totalClasses,
        totalStudents: statsRes.data.totalStudents,
        totalTeachers: statsRes.data.totalTeachers,
      });
      setCourses(coursesRes.data);
      setTeachers(teachersRes.data);
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to load classes",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleViewDetails = async (classItem: ClassData) => {
    try {
      const response = await api.get(`/admin/classes/${classItem.id}`);
      setSelectedClass(response.data);
      setDetailsOpen(true);
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to load class details",
        variant: "destructive",
      });
    }
  };

  const handleCreateClass = async () => {
    if (!createForm.name.trim() || !createForm.teacherId || !createForm.courseId) {
      toast({
        title: "Error",
        description: "Please fill in all fields",
        variant: "destructive",
      });
      return;
    }

    try {
      await api.post("/admin/classes", {
        name: createForm.name.trim(),
        teacherId: parseInt(createForm.teacherId),
        courseId: parseInt(createForm.courseId),
      });
      
      toast({
        title: "Success",
        description: "Class created successfully",
      });
      
      setCreateOpen(false);
      setCreateForm({ name: "", teacherId: "", courseId: "" });
      fetchData();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to create class",
        variant: "destructive",
      });
    }
  };

  const handleEditClass = (classItem: ClassData) => {
    setEditingClass(classItem);
    setEditForm({ name: classItem.name });
    setEditOpen(true);
  };

  const handleUpdateClass = async () => {
    if (!editForm.name.trim() || !editingClass) {
      toast({
        title: "Error",
        description: "Please enter a class name",
        variant: "destructive",
      });
      return;
    }

    try {
      await api.put(`/admin/classes/${editingClass.id}`, {
        name: editForm.name.trim(),
      });
      
      toast({
        title: "Success",
        description: "Class updated successfully",
      });
      
      setEditOpen(false);
      setEditingClass(null);
      setEditForm({ name: "" });
      fetchData();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to update class",
        variant: "destructive",
      });
    }
  };

  const handleDeleteClass = async (classId: number, className: string) => {
    if (!confirm(`Are you sure you want to delete class "${className}"? This will remove all associated data.`)) return;
    
    try {
      await api.delete(`/admin/classes/${classId}`);
      toast({
        title: "Success",
        description: "Class deleted successfully",
      });
      fetchData();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to delete class",
        variant: "destructive",
      });
    }
  };

  const filteredClasses = classes.filter(classItem =>
    (classItem.name?.toLowerCase() || '').includes(searchTerm.toLowerCase()) ||
    (classItem.courseName?.toLowerCase() || '').includes(searchTerm.toLowerCase()) ||
    (classItem.teacherName?.toLowerCase() || '').includes(searchTerm.toLowerCase())
  );

  if (loading || !stats) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>
    );
  }

  const statsData = [
    { label: "Total Classes", value: stats.totalClasses.toString(), icon: BookOpen, color: "text-primary" },
    { label: "Total Students", value: stats.totalStudents.toString(), icon: GraduationCap, color: "text-accent" },
    { label: "Total Teachers", value: stats.totalTeachers.toString(), icon: Users, color: "text-success" },
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">Class Management</h1>
          <p className="text-muted-foreground">
            View and manage all classes in the system
          </p>
        </div>
        <Button onClick={() => setCreateOpen(true)}>
          <Plus className="h-4 w-4 mr-2" />
          Create Class
        </Button>
      </div>

      {/* Stats */}
      <div className="grid gap-4 md:grid-cols-3">
        {statsData.map((stat, index) => (
          <Card key={index} className="glass-card hover:shadow-lg transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {stat.label}
              </CardTitle>
              <stat.icon className={`h-5 w-5 ${stat.color}`} />
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold">{stat.value}</div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Classes Table */}
      <Card className="glass-card">
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>All Classes</CardTitle>
              <CardDescription>A list of all classes in the system</CardDescription>
            </div>
            <div className="relative w-64">
              <Input
                placeholder="Search classes..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Class Name</TableHead>
                <TableHead>Course</TableHead>
                <TableHead>Teacher</TableHead>
                <TableHead>Students</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredClasses.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} className="text-center text-muted-foreground">
                    No classes found
                  </TableCell>
                </TableRow>
              ) : (
                filteredClasses.map((classItem) => (
                  <TableRow key={classItem.id}>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <BookOpen className="h-4 w-4 text-muted-foreground" />
                        <span className="font-medium">{classItem.name}</span>
                      </div>
                    </TableCell>
                    <TableCell>
                      {classItem.courseName ? (
                        <Badge variant="secondary">{classItem.courseName}</Badge>
                      ) : (
                        <span className="text-sm text-muted-foreground">No course</span>
                      )}
                    </TableCell>
                    <TableCell>
                      {classItem.teacherName || "Unknown"}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <Users className="h-4 w-4 text-muted-foreground" />
                        <span>{classItem.studentCount || 0}</span>
                      </div>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-2">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleViewDetails(classItem)}
                          title="View Details"
                        >
                          <Eye className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleEditClass(classItem)}
                          title="Edit Class"
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleDeleteClass(classItem.id, classItem.name)}
                          title="Delete Class"
                        >
                          <Trash2 className="h-4 w-4 text-destructive" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {/* Class Details Dialog */}
      <Dialog open={detailsOpen} onOpenChange={setDetailsOpen}>
        <DialogContent className="sm:max-w-[600px]">
          <DialogHeader>
            <DialogTitle>Class Details</DialogTitle>
            <DialogDescription>
              Detailed information about {selectedClass?.name}
            </DialogDescription>
          </DialogHeader>
          {selectedClass && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Class Name</p>
                  <p className="text-lg font-semibold">{selectedClass.name}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Course</p>
                  <p className="text-lg">{selectedClass.courseName || "N/A"}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Teacher</p>
                  <p className="text-lg">{selectedClass.teacherName || "Unknown"}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Total Students</p>
                  <p className="text-lg font-semibold">{selectedClass.students?.length || 0}</p>
                </div>
              </div>

              {selectedClass.students && selectedClass.students.length > 0 && (
                <div>
                  <p className="text-sm font-medium text-muted-foreground mb-2">Enrolled Students</p>
                  <div className="max-h-60 overflow-y-auto space-y-2">
                    {selectedClass.students.map((student) => (
                      <div
                        key={student.id}
                        className="flex items-center justify-between p-2 rounded-lg border bg-muted/50"
                      >
                        <div>
                          <p className="font-medium">{student.name || student.username}</p>
                          <p className="text-sm text-muted-foreground">{student.email}</p>
                        </div>
                        <Badge variant="secondary">
                          <GraduationCap className="h-3 w-3 mr-1" />
                          Student
                        </Badge>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Create Class Dialog */}
      <Dialog open={createOpen} onOpenChange={setCreateOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Create New Class</DialogTitle>
            <DialogDescription>
              Add a new class to the system
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="className">Class Name</Label>
              <Input
                id="className"
                placeholder="Enter class name"
                value={createForm.name}
                onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="teacher">Teacher</Label>
              <Select
                value={createForm.teacherId}
                onValueChange={(value) => setCreateForm({ ...createForm, teacherId: value })}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select a teacher" />
                </SelectTrigger>
                <SelectContent>
                  {teachers.map((teacher) => (
                    <SelectItem key={teacher.id} value={teacher.id.toString()}>
                      {teacher.name} ({teacher.email})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="course">Course</Label>
              <Select
                value={createForm.courseId}
                onValueChange={(value) => setCreateForm({ ...createForm, courseId: value })}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select a course" />
                </SelectTrigger>
                <SelectContent>
                  {courses.map((course) => (
                    <SelectItem key={course.id} value={course.id.toString()}>
                      {course.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="flex justify-end gap-2 pt-4">
              <Button variant="outline" onClick={() => setCreateOpen(false)}>
                Cancel
              </Button>
              <Button onClick={handleCreateClass}>
                Create Class
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Edit Class Dialog */}
      <Dialog open={editOpen} onOpenChange={setEditOpen}>
        <DialogContent className="sm:max-w-[400px]">
          <DialogHeader>
            <DialogTitle>Edit Class</DialogTitle>
            <DialogDescription>
              Update class information
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="editClassName">Class Name</Label>
              <Input
                id="editClassName"
                placeholder="Enter class name"
                value={editForm.name}
                onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
              />
            </div>
            <div className="flex justify-end gap-2 pt-4">
              <Button variant="outline" onClick={() => setEditOpen(false)}>
                Cancel
              </Button>
              <Button onClick={handleUpdateClass}>
                Update Class
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
