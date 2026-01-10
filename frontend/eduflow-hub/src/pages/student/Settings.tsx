import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { 
  User, 
  Lock, 
  Bell, 
  Palette, 
  Globe, 
  Shield,
  Save,
  Loader2,
  Check
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Separator } from "@/components/ui/separator";
import { useToast } from "@/hooks/use-toast";
import { getStudentProfile, changePassword, UserProfile } from "@/services/api";
import { useTheme } from "@/components/ThemeProvider";

export default function StudentSettings() {
  const { toast } = useToast();
  const navigate = useNavigate();
  const { theme, toggleTheme } = useTheme();
  
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  
  // Settings state
  const [settings, setSettings] = useState({
    // Notification settings
    emailNotifications: true,
    assignmentReminders: true,
    gradeNotifications: true,
    messageNotifications: true,
    
    // Privacy settings
    showEmail: false,
    showPhone: false,
    
    // Preferences
    language: "en",
  });

  // Password change state
  const [passwordData, setPasswordData] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        setLoading(true);
        const data = await getStudentProfile();
        setProfile(data);
      } catch (err) {
        toast({
          title: "Error",
          description: "Failed to load profile",
          variant: "destructive",
        });
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [toast]);

  const handleSaveSettings = async () => {
    setSaving(true);
    
    // Simulate API call
    setTimeout(() => {
      setSaving(false);
      toast({
        title: "Settings saved",
        description: "Your preferences have been updated successfully",
      });
    }, 1000);
  };

  const handleChangePassword = async () => {
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      toast({
        title: "Error",
        description: "New passwords do not match",
        variant: "destructive",
      });
      return;
    }

    if (passwordData.newPassword.length < 6) {
      toast({
        title: "Error",
        description: "Password must be at least 6 characters",
        variant: "destructive",
      });
      return;
    }

    setSaving(true);
    
    try {
      await changePassword({
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword,
      });
      
      setPasswordData({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
      
      toast({
        title: "Password changed",
        description: "Your password has been updated successfully",
      });
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to change password";
      toast({
        title: "Error",
        description: message,
        variant: "destructive",
      });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">Settings</h1>
        <p className="text-muted-foreground">
          Manage your account settings and preferences
        </p>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Settings Navigation */}
        <Card className="glass-card lg:col-span-1 h-fit">
          <CardHeader>
            <CardTitle>Settings</CardTitle>
            <CardDescription>Choose a category</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2">
            <Button variant="ghost" className="w-full justify-start">
              <User className="mr-2 h-4 w-4" />
              Account
            </Button>
            <Button variant="ghost" className="w-full justify-start">
              <Lock className="mr-2 h-4 w-4" />
              Security
            </Button>
            <Button variant="ghost" className="w-full justify-start">
              <Bell className="mr-2 h-4 w-4" />
              Notifications
            </Button>
            <Button variant="ghost" className="w-full justify-start">
              <Palette className="mr-2 h-4 w-4" />
              Appearance
            </Button>
            <Button variant="ghost" className="w-full justify-start">
              <Shield className="mr-2 h-4 w-4" />
              Privacy
            </Button>
          </CardContent>
        </Card>

        {/* Settings Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Account Information */}
          <Card className="glass-card">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <User className="h-5 w-5" />
                Account Information
              </CardTitle>
              <CardDescription>
                Your basic account details
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label>Name</Label>
                  <Input value={profile?.name || ""} disabled />
                </div>
                <div className="space-y-2">
                  <Label>Username</Label>
                  <Input value={profile?.username || ""} disabled />
                </div>
              </div>
              <div className="space-y-2">
                <Label>Email</Label>
                <Input value={profile?.email || ""} disabled />
              </div>
              <div className="space-y-2">
                <Label>Phone Number</Label>
                <Input 
                  value={profile?.phoneNumber || ""} 
                  placeholder="Not set"
                  disabled 
                />
              </div>
              <p className="text-xs text-muted-foreground">
                To update your account information, please contact your administrator.
              </p>
            </CardContent>
          </Card>

          {/* Change Password */}
          <Card className="glass-card">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Lock className="h-5 w-5" />
                Change Password
              </CardTitle>
              <CardDescription>
                Update your password to keep your account secure
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label>Current Password</Label>
                <Input 
                  type="password"
                  value={passwordData.currentPassword}
                  onChange={(e) => setPasswordData({...passwordData, currentPassword: e.target.value})}
                  placeholder="Enter current password"
                />
              </div>
              <div className="space-y-2">
                <Label>New Password</Label>
                <Input 
                  type="password"
                  value={passwordData.newPassword}
                  onChange={(e) => setPasswordData({...passwordData, newPassword: e.target.value})}
                  placeholder="Enter new password"
                />
              </div>
              <div className="space-y-2">
                <Label>Confirm New Password</Label>
                <Input 
                  type="password"
                  value={passwordData.confirmPassword}
                  onChange={(e) => setPasswordData({...passwordData, confirmPassword: e.target.value})}
                  placeholder="Confirm new password"
                />
              </div>
              <Button 
                onClick={handleChangePassword}
                disabled={saving || !passwordData.currentPassword || !passwordData.newPassword}
              >
                {saving ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Updating...
                  </>
                ) : (
                  <>
                    <Lock className="mr-2 h-4 w-4" />
                    Change Password
                  </>
                )}
              </Button>
            </CardContent>
          </Card>

          {/* Notification Preferences */}
          <Card className="glass-card">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Bell className="h-5 w-5" />
                Notification Preferences
              </CardTitle>
              <CardDescription>
                Choose what notifications you want to receive
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>Email Notifications</Label>
                  <p className="text-sm text-muted-foreground">
                    Receive notifications via email
                  </p>
                </div>
                <Switch 
                  checked={settings.emailNotifications}
                  onCheckedChange={(checked) => 
                    setSettings({...settings, emailNotifications: checked})
                  }
                />
              </div>
              <Separator />
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>Assignment Reminders</Label>
                  <p className="text-sm text-muted-foreground">
                    Get reminded about upcoming assignments
                  </p>
                </div>
                <Switch 
                  checked={settings.assignmentReminders}
                  onCheckedChange={(checked) => 
                    setSettings({...settings, assignmentReminders: checked})
                  }
                />
              </div>
              <Separator />
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>Grade Notifications</Label>
                  <p className="text-sm text-muted-foreground">
                    Be notified when assignments are graded
                  </p>
                </div>
                <Switch 
                  checked={settings.gradeNotifications}
                  onCheckedChange={(checked) => 
                    setSettings({...settings, gradeNotifications: checked})
                  }
                />
              </div>
              <Separator />
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>Message Notifications</Label>
                  <p className="text-sm text-muted-foreground">
                    Get notified about new messages
                  </p>
                </div>
                <Switch 
                  checked={settings.messageNotifications}
                  onCheckedChange={(checked) => 
                    setSettings({...settings, messageNotifications: checked})
                  }
                />
              </div>
            </CardContent>
          </Card>

          {/* Appearance */}
          <Card className="glass-card">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Palette className="h-5 w-5" />
                Appearance
              </CardTitle>
              <CardDescription>
                Customize how the app looks
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>Dark Mode</Label>
                  <p className="text-sm text-muted-foreground">
                    Use dark theme for better visibility at night
                  </p>
                </div>
                <Switch 
                  checked={theme === "dark"}
                  onCheckedChange={toggleTheme}
                />
              </div>
            </CardContent>
          </Card>

          {/* Privacy */}
          <Card className="glass-card">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Shield className="h-5 w-5" />
                Privacy Settings
              </CardTitle>
              <CardDescription>
                Control who can see your information
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>Show Email to Others</Label>
                  <p className="text-sm text-muted-foreground">
                    Allow other students to see your email
                  </p>
                </div>
                <Switch 
                  checked={settings.showEmail}
                  onCheckedChange={(checked) => 
                    setSettings({...settings, showEmail: checked})
                  }
                />
              </div>
              <Separator />
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>Show Phone Number</Label>
                  <p className="text-sm text-muted-foreground">
                    Allow others to see your phone number
                  </p>
                </div>
                <Switch 
                  checked={settings.showPhone}
                  onCheckedChange={(checked) => 
                    setSettings({...settings, showPhone: checked})
                  }
                />
              </div>
            </CardContent>
          </Card>

          {/* Save Button */}
          <div className="flex justify-end gap-4">
            <Button variant="outline" onClick={() => navigate("/student/dashboard")}>
              Cancel
            </Button>
            <Button onClick={handleSaveSettings} disabled={saving}>
              {saving ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Saving...
                </>
              ) : (
                <>
                  <Save className="mr-2 h-4 w-4" />
                  Save Changes
                </>
              )}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
