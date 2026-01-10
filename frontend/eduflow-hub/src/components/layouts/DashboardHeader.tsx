import { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Moon, Sun, LogOut } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useTheme } from "@/components/ThemeProvider";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import NotificationBell from "@/components/NotificationBell";
import { getTeacherProfile, getStudentProfile, UserProfile } from "@/services/api";
import { useToast } from "@/hooks/use-toast";

export function DashboardHeader() {
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const location = useLocation();
  const { toast } = useToast();
  
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  // Determine if user is teacher or student from current path
  const isTeacher = location.pathname.includes("/teacher");
  const isStudent = location.pathname.includes("/student");

  // Fetch user profile
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        setLoading(true);
        if (isTeacher) {
          const data = await getTeacherProfile();
          setProfile(data);
        } else if (isStudent) {
          const data = await getStudentProfile();
          setProfile(data);
        }
      } catch (err) {
        console.error("Failed to fetch profile:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [isTeacher, isStudent]);

  // Get user initials
  const getInitials = () => {
    if (!profile) return "U";
    const name = profile.name || profile.username;
    return name
      .split(" ")
      .map((n) => n[0])
      .join("")
      .toUpperCase()
      .slice(0, 2);
  };

  // Handle logout
  const handleLogout = () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("userRole");
    toast({
      title: "Logged out",
      description: "You have been successfully logged out",
    });
    navigate("/login");
  };

  // Handle profile navigation
  const handleProfile = () => {
    if (isTeacher) {
      navigate("/teacher/profile");
    } else if (isStudent) {
      navigate("/student/profile");
    }
  };

  // Handle settings navigation
  const handleSettings = () => {
    if (isTeacher) {
      navigate("/teacher/settings");
    } else if (isStudent) {
      navigate("/student/settings");
    }
  };

  return (
    <header className="sticky top-0 z-40 border-b border-border bg-background/80 backdrop-blur-xl">
      <div className="flex h-16 items-center justify-between px-6">
        <div className="flex items-center gap-4">
          <h2 className="text-xl font-semibold">Dashboard</h2>
        </div>

        <div className="flex items-center gap-3">
          {/* Theme Toggle */}
          <Button
            variant="ghost"
            size="icon"
            onClick={toggleTheme}
            className="relative"
          >
            {theme === "light" ? (
              <Moon className="h-5 w-5" />
            ) : (
              <Sun className="h-5 w-5" />
            )}
          </Button>

          {/* Notifications */}
          <NotificationBell />

          {/* User Menu */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="relative h-10 w-10 rounded-full">
                <Avatar>
                  <AvatarImage src={profile?.profilePicture || "/placeholder.svg"} alt="User" />
                  <AvatarFallback className="bg-primary text-primary-foreground">
                    {loading ? "..." : getInitials()}
                  </AvatarFallback>
                </Avatar>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56">
              <DropdownMenuLabel>
                <div className="flex flex-col space-y-1">
                  <p className="text-sm font-medium">
                    {loading ? "Loading..." : profile?.name || profile?.username || "User"}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    {loading ? "" : profile?.email || ""}
                  </p>
                  {profile?.role && (
                    <p className="text-xs text-muted-foreground capitalize">
                      {profile.role.toLowerCase()}
                    </p>
                  )}
                </div>
              </DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={handleProfile} className="cursor-pointer">
                Profile
              </DropdownMenuItem>
              <DropdownMenuItem onClick={handleSettings} className="cursor-pointer">
                Settings
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem 
                onClick={handleLogout} 
                className="text-destructive cursor-pointer"
              >
                <LogOut className="mr-2 h-4 w-4" />
                Log out
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </header>
  );
}
