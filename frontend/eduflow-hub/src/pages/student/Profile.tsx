import { useEffect, useState, FormEvent, useRef } from "react";
import { useLocation } from "react-router-dom";
import { User, Mail, Phone, MapPin, Calendar, BookOpen, Award, Edit, Loader2, GraduationCap, Users, FileText, Building2, Camera, Trash2 } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { useToast } from "@/hooks/use-toast";
import { getUserProfile, updateUserProfile, UserProfile } from "@/services/api";

export default function Profile() {
  const location = useLocation();
  const { toast } = useToast();
  
  // Determine role from URL path
  const getRoleFromPath = () => {
    if (location.pathname.includes("/teacher")) return "TEACHER";
    if (location.pathname.includes("/admin")) return "ADMIN";
    return "STUDENT";
  };

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [uploadingPicture, setUploadingPicture] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  
  // Edit form state
  const [editName, setEditName] = useState("");
  const [editEmail, setEditEmail] = useState("");
  const [editPhone, setEditPhone] = useState("");
  const [editLocation, setEditLocation] = useState("");
  
  const role = getRoleFromPath();

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        setLoading(true);
        const profileData = await getUserProfile(role);
        setProfile(profileData);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Failed to load profile";
        toast({
          title: "Error",
          description: message,
          variant: "destructive",
        });
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [role, toast]);

  // Update edit form when profile changes or dialog opens
  useEffect(() => {
    if (profile) {
      setEditName(profile.name || "");
      setEditEmail(profile.email || "");
      setEditPhone(profile.phone || "");
      setEditLocation(profile.location || "");
    }
  }, [profile, isEditOpen]);

  const handleEditClick = () => {
    setIsEditOpen(true);
  };

  const handlePictureUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('image/')) {
      toast({
        title: "Invalid File",
        description: "Please select an image file",
        variant: "destructive",
      });
      return;
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      toast({
        title: "File Too Large",
        description: "Please select an image smaller than 5MB",
        variant: "destructive",
      });
      return;
    }

    try {
      setUploadingPicture(true);
      const { uploadProfilePicture } = await import("@/services/api");
      const updated = await uploadProfilePicture(file);
      
      // Reload profile to get updated picture
      const profileData = await getUserProfile(role);
      setProfile(profileData);
      
      toast({
        title: "Success",
        description: "Profile picture uploaded successfully",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to upload profile picture",
        variant: "destructive",
      });
    } finally {
      setUploadingPicture(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const handleDeletePicture = async () => {
    if (!confirm("Are you sure you want to delete your profile picture?")) return;

    try {
      const { deleteProfilePicture } = await import("@/services/api");
      await deleteProfilePicture();
      
      // Reload profile
      const profileData = await getUserProfile(role);
      setProfile(profileData);
      
      toast({
        title: "Success",
        description: "Profile picture deleted successfully",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete profile picture",
        variant: "destructive",
      });
    }
  };

  const handleUpdateProfile = async (e: FormEvent) => {
    e.preventDefault();

    if (!profile) return;

    // Validate email is provided
    if (!editEmail.trim()) {
      toast({
        title: "Validation Error",
        description: "Email is required.",
        variant: "destructive",
      });
      return;
    }

    try {
      setIsUpdating(true);

      const updatedProfile = await updateUserProfile(
        {
          name: editName.trim() || undefined,
          email: editEmail.trim(),
          phone: editPhone.trim() || undefined,
          location: editLocation.trim() || undefined,
        },
        role
      );

      setProfile(updatedProfile);
      setIsEditOpen(false);

      toast({
        title: "Profile Updated",
        description: "Your profile has been saved successfully.",
      });
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to update profile";
      toast({
        title: "Error",
        description: message,
        variant: "destructive",
      });
    } finally {
      setIsUpdating(false);
    }
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="space-y-6">
        <Card className="glass-card">
          <CardContent className="flex flex-col items-center justify-center py-12">
            <p className="text-muted-foreground">Failed to load profile data</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  // Get initials for avatar
  const getInitials = () => {
    if (profile.name) {
      return profile.name
        .split(" ")
        .map((n) => n[0])
        .join("")
        .toUpperCase()
        .slice(0, 2);
    }
    return profile.username.slice(0, 2).toUpperCase();
  };

  // Get role badge color
  const getRoleBadge = () => {
    const roleUpper = profile.role?.toUpperCase() || role;
    if (roleUpper === "TEACHER") {
      return <Badge className="bg-blue-500">Teacher</Badge>;
    } else if (roleUpper === "ADMIN") {
      return <Badge className="bg-purple-500">Admin</Badge>;
    } else {
      return <Badge variant="secondary">Student</Badge>;
    }
  };

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">My Profile</h1>
        <p className="text-muted-foreground">
          {role === "TEACHER" 
            ? "Manage your teaching profile and information"
            : role === "ADMIN"
            ? "Manage your administrator profile"
            : "Manage your personal information and academic details"}
        </p>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Profile Card */}
        <Card className="glass-card lg:col-span-1">
          <CardHeader>
            <div className="flex flex-col items-center space-y-4">
              <Avatar className="h-32 w-32">
                <AvatarImage src="/placeholder.svg" alt={profile.name || profile.username} />
                <AvatarFallback className="text-2xl bg-primary text-primary-foreground">
                  {getInitials()}
                </AvatarFallback>
              </Avatar>
              <div className="text-center space-y-2">
                <h2 className="text-2xl font-bold">{profile.name || profile.username}</h2>
                <div className="flex items-center justify-center gap-2 flex-wrap">
                  {getRoleBadge()}
                  {role === "STUDENT" && profile.major && (
                    <Badge variant="secondary">{profile.major}</Badge>
                  )}
                  {role === "STUDENT" && profile.year && (
                    <Badge>{profile.year}</Badge>
                  )}
                </div>
                {role === "STUDENT" && profile.studentId && (
                  <p className="text-sm text-muted-foreground">
                    ID: {profile.studentId}
                  </p>
                )}
                {role === "TEACHER" && profile.department && (
                  <p className="text-sm text-muted-foreground">
                    {profile.department}
                  </p>
                )}
              </div>
              <Button className="w-full" onClick={handleEditClick}>
                <Edit className="mr-2 h-4 w-4" />
                Edit Profile
              </Button>
            </div>
          </CardHeader>
        </Card>

        {/* Information Card */}
        <Card className="glass-card lg:col-span-2">
          <CardHeader>
            <CardTitle>Personal Information</CardTitle>
            <CardDescription>Your contact and profile details</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid gap-4 md:grid-cols-2">
              <div className="flex items-start gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                  <Mail className="h-5 w-5 text-primary" />
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Email</p>
                  <p className="font-medium">{profile.email || "Not provided"}</p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-accent/10">
                  <User className="h-5 w-5 text-accent" />
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Username</p>
                  <p className="font-medium">{profile.username}</p>
                </div>
              </div>

              {profile.phone && (
                <div className="flex items-start gap-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-success/10">
                    <Phone className="h-5 w-5 text-success" />
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Phone</p>
                    <p className="font-medium">{profile.phone}</p>
                  </div>
                </div>
              )}

              {profile.location && (
                <div className="flex items-start gap-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-purple-500/10">
                    <MapPin className="h-5 w-5 text-purple-500" />
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Location</p>
                    <p className="font-medium">{profile.location}</p>
                  </div>
                </div>
              )}

              {profile.joinDate && (
                <div className="flex items-start gap-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                    <Calendar className="h-5 w-5 text-primary" />
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Joined</p>
                    <p className="font-medium">{profile.joinDate}</p>
                  </div>
                </div>
              )}

              {/* Student Specific Fields */}
              {role === "STUDENT" && (
                <>
                  {profile.major && (
                    <div className="flex items-start gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                        <BookOpen className="h-5 w-5 text-primary" />
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">Major</p>
                        <p className="font-medium">{profile.major}</p>
                      </div>
                    </div>
                  )}

                  {profile.expectedGraduation && (
                    <div className="flex items-start gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-accent/10">
                        <GraduationCap className="h-5 w-5 text-accent" />
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">Expected Graduation</p>
                        <p className="font-medium">{profile.expectedGraduation}</p>
                      </div>
                    </div>
                  )}
                </>
              )}

              {/* Teacher Specific Fields */}
              {role === "TEACHER" && (
                <>
                  {profile.department && (
                    <div className="flex items-start gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                        <Building2 className="h-5 w-5 text-primary" />
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">Department</p>
                        <p className="font-medium">{profile.department}</p>
                      </div>
                    </div>
                  )}

                  {profile.specialization && (
                    <div className="flex items-start gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-accent/10">
                        <BookOpen className="h-5 w-5 text-accent" />
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">Specialization</p>
                        <p className="font-medium">{profile.specialization}</p>
                      </div>
                    </div>
                  )}

                  {profile.yearsOfExperience !== undefined && (
                    <div className="flex items-start gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-success/10">
                        <Award className="h-5 w-5 text-success" />
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">Years of Experience</p>
                        <p className="font-medium">{profile.yearsOfExperience} years</p>
                      </div>
                    </div>
                  )}
                </>
              )}

              {/* Admin Specific Fields */}
              {role === "ADMIN" && profile.adminLevel && (
                <div className="flex items-start gap-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                    <Award className="h-5 w-5 text-primary" />
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Admin Level</p>
                    <p className="font-medium">{profile.adminLevel}</p>
                  </div>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Role-Specific Stats */}
      {role === "STUDENT" && (
        <div className="grid gap-6 md:grid-cols-3">
          {profile.gpa != null && typeof profile.gpa === 'number' && (
            <Card className="glass-card">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Current GPA
                </CardTitle>
                <Award className="h-5 w-5 text-primary" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">{profile.gpa.toFixed(2)}</div>
                <p className="text-xs text-muted-foreground mt-1">
                  {profile.gpa >= 3.5 ? "Excellent" : profile.gpa >= 3.0 ? "Good" : "Keep improving"}
                </p>
              </CardContent>
            </Card>
          )}

          {profile.creditsCompleted != null && profile.totalCredits != null && 
           typeof profile.creditsCompleted === 'number' && typeof profile.totalCredits === 'number' && (
            <Card className="glass-card">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Credits Completed
                </CardTitle>
                <BookOpen className="h-5 w-5 text-accent" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">
                  {profile.creditsCompleted}/{profile.totalCredits}
                </div>
                <p className="text-xs text-muted-foreground mt-1">
                  {Math.round((profile.creditsCompleted / profile.totalCredits) * 100)}% towards graduation
                </p>
              </CardContent>
            </Card>
          )}

          {profile.attendanceRate != null && typeof profile.attendanceRate === 'number' && (
            <Card className="glass-card">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Attendance Rate
                </CardTitle>
                <Calendar className="h-5 w-5 text-success" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">{profile.attendanceRate}%</div>
                <p className="text-xs text-muted-foreground mt-1">
                  {profile.attendanceRate >= 90 ? "Excellent attendance" : "Good attendance"}
                </p>
              </CardContent>
            </Card>
          )}
        </div>
      )}

      {role === "TEACHER" && (
        <div className="grid gap-6 md:grid-cols-3">
          {profile.totalClasses !== undefined && (
            <Card className="glass-card">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Total Classes
                </CardTitle>
                <BookOpen className="h-5 w-5 text-primary" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">{profile.totalClasses}</div>
                <p className="text-xs text-muted-foreground mt-1">
                  Active classes you're teaching
                </p>
              </CardContent>
            </Card>
          )}

          {profile.totalStudents !== undefined && (
            <Card className="glass-card">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Total Students
                </CardTitle>
                <Users className="h-5 w-5 text-accent" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">{profile.totalStudents}</div>
                <p className="text-xs text-muted-foreground mt-1">
                  Students across all classes
                </p>
              </CardContent>
            </Card>
          )}

          {profile.yearsOfExperience !== undefined && (
            <Card className="glass-card">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Experience
                </CardTitle>
                <Award className="h-5 w-5 text-success" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">{profile.yearsOfExperience}</div>
                <p className="text-xs text-muted-foreground mt-1">
                  Years of teaching experience
                </p>
              </CardContent>
            </Card>
          )}
        </div>
      )}

      {role === "ADMIN" && (
        <div className="grid gap-6 md:grid-cols-3">
          <Card className="glass-card">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                Admin Level
              </CardTitle>
              <Award className="h-5 w-5 text-primary" />
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold">{profile.adminLevel || "Admin"}</div>
              <p className="text-xs text-muted-foreground mt-1">
                System administrator
              </p>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Student Achievements Section (if data available) */}
      {role === "STUDENT" && (
        <Card className="glass-card">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Award className="h-5 w-5 text-accent" />
              Academic Information
            </CardTitle>
            <CardDescription>Your academic progress and achievements</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-muted-foreground">
              {profile.major || profile.year 
                ? `Currently enrolled as a ${profile.year || "student"} in ${profile.major || "your program"}`
                : "Academic information will be displayed here"}
            </p>
          </CardContent>
        </Card>
      )}

      {/* Edit Profile Dialog */}
      <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Edit Profile</DialogTitle>
            <DialogDescription>
              Update your personal information. Click save when you're done.
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleUpdateProfile}>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label htmlFor="edit-name">Full Name</Label>
                <Input
                  id="edit-name"
                  value={editName}
                  onChange={(e) => setEditName(e.target.value)}
                  placeholder="Enter your full name"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="edit-email">Email</Label>
                <Input
                  id="edit-email"
                  type="email"
                  value={editEmail}
                  onChange={(e) => setEditEmail(e.target.value)}
                  placeholder="Enter your email"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="edit-phone">Phone Number</Label>
                <Input
                  id="edit-phone"
                  type="tel"
                  value={editPhone}
                  onChange={(e) => setEditPhone(e.target.value)}
                  placeholder="Enter your phone number"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="edit-location">Location/Address</Label>
                <Input
                  id="edit-location"
                  value={editLocation}
                  onChange={(e) => setEditLocation(e.target.value)}
                  placeholder="Enter your location or address"
                />
              </div>

              <div className="rounded-lg border border-border bg-muted p-3">
                <p className="text-xs text-muted-foreground">
                  <strong>Note:</strong> Username and role cannot be changed. These are managed by the system administrator.
                </p>
              </div>
            </div>
            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => setIsEditOpen(false)}
                disabled={isUpdating}
              >
                Cancel
              </Button>
              <Button type="submit" disabled={isUpdating}>
                {isUpdating ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Saving...
                  </>
                ) : (
                  "Save Changes"
                )}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
