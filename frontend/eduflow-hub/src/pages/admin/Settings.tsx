import { useEffect, useState } from "react";
import { Save, Settings, Users, Shield, Bell, Database, Globe, Mail, Lock } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Separator } from "@/components/ui/separator";
import { useToast } from "@/hooks/use-toast";
import { api } from "@/services/api";

interface SystemSettings {
  // General Settings
  platformName: string;
  platformDescription: string;
  supportEmail: string;
  maxFileUploadSize: number;
  sessionTimeout: number;
  
  // User Management
  allowSelfRegistration: boolean;
  requireEmailVerification: boolean;
  defaultUserRole: string;
  passwordMinLength: number;
  passwordRequireSpecialChars: boolean;
  
  // Notifications
  emailNotificationsEnabled: boolean;
  pushNotificationsEnabled: boolean;
  notificationRetentionDays: number;
  
  // Security
  enableTwoFactorAuth: boolean;
  maxLoginAttempts: number;
  lockoutDurationMinutes: number;
  
  // Academic Settings
  defaultGradingScale: string;
  allowLateSubmissions: boolean;
  defaultLatePenalty: number;
  academicYearStart: string;
  academicYearEnd: string;
}

export default function AdminSettings() {
  const { toast } = useToast();
  const [settings, setSettings] = useState<SystemSettings>({
    // General Settings
    platformName: "EduFlow Hub",
    platformDescription: "Comprehensive Learning Management System",
    supportEmail: "support@eduflow.com",
    maxFileUploadSize: 50,
    sessionTimeout: 30,
    
    // User Management
    allowSelfRegistration: true,
    requireEmailVerification: false,
    defaultUserRole: "STUDENT",
    passwordMinLength: 8,
    passwordRequireSpecialChars: true,
    
    // Notifications
    emailNotificationsEnabled: true,
    pushNotificationsEnabled: false,
    notificationRetentionDays: 30,
    
    // Security
    enableTwoFactorAuth: false,
    maxLoginAttempts: 5,
    lockoutDurationMinutes: 15,
    
    // Academic Settings
    defaultGradingScale: "PERCENTAGE",
    allowLateSubmissions: true,
    defaultLatePenalty: 10,
    academicYearStart: "2024-09-01",
    academicYearEnd: "2025-06-30"
  });
  
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetchSettings();
  }, []);

  const fetchSettings = async () => {
    try {
      setLoading(true);
      const response = await api.get("/admin/settings");
      console.log("📋 Loaded settings from backend:", response.data);
      setSettings({ ...settings, ...response.data });
    } catch (error: any) {
      // If settings don't exist yet, use defaults
      console.log("📋 Using default settings, backend error:", error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    try {
      setSaving(true);
      await api.post("/admin/settings", settings);
      toast({
        title: "Success",
        description: "Settings saved successfully",
      });
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to save settings",
        variant: "destructive",
      });
    } finally {
      setSaving(false);
    }
  };

  const updateSetting = (key: keyof SystemSettings, value: any) => {
    setSettings(prev => ({ ...prev, [key]: value }));
  };

  // Auto-save for critical settings like email verification
  const updateSettingWithAutoSave = async (key: keyof SystemSettings, value: any) => {
    setSettings(prev => ({ ...prev, [key]: value }));
    
    // Auto-save for email verification setting
    if (key === 'requireEmailVerification') {
      try {
        const updatedSettings = { ...settings, [key]: value };
        await api.post("/admin/settings", updatedSettings);
        toast({
          title: "Setting Updated",
          description: `Email verification ${value ? 'enabled' : 'disabled'} successfully`,
        });
      } catch (error: any) {
        // Revert the change if save failed
        setSettings(prev => ({ ...prev, [key]: !value }));
        toast({
          title: "Error",
          description: error.response?.data?.message || "Failed to update setting",
          variant: "destructive",
        });
      }
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">System Settings</h1>
          <p className="text-muted-foreground">
            Configure platform settings and preferences
          </p>
        </div>
        <Button onClick={handleSave} disabled={saving}>
          <Save className="mr-2 h-4 w-4" />
          {saving ? "Saving..." : "Save Changes"}
        </Button>
      </div>

      <Tabs defaultValue="general" className="space-y-6">
        <TabsList className="grid w-full grid-cols-5">
          <TabsTrigger value="general" className="flex items-center gap-2">
            <Settings className="h-4 w-4" />
            General
          </TabsTrigger>
          <TabsTrigger value="users" className="flex items-center gap-2">
            <Users className="h-4 w-4" />
            Users
          </TabsTrigger>
          <TabsTrigger value="security" className="flex items-center gap-2">
            <Shield className="h-4 w-4" />
            Security
          </TabsTrigger>
          <TabsTrigger value="notifications" className="flex items-center gap-2">
            <Bell className="h-4 w-4" />
            Notifications
          </TabsTrigger>
          <TabsTrigger value="academic" className="flex items-center gap-2">
            <Database className="h-4 w-4" />
            Academic
          </TabsTrigger>
        </TabsList>

        {/* General Settings */}
        <TabsContent value="general" className="space-y-6">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Globe className="h-5 w-5" />
                Platform Configuration
              </CardTitle>
              <CardDescription>
                Basic platform information and configuration
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="platformName">Platform Name</Label>
                  <Input
                    id="platformName"
                    value={settings.platformName}
                    onChange={(e) => updateSetting("platformName", e.target.value)}
                    placeholder="Enter platform name"
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="supportEmail">Support Email</Label>
                  <Input
                    id="supportEmail"
                    type="email"
                    value={settings.supportEmail}
                    onChange={(e) => updateSetting("supportEmail", e.target.value)}
                    placeholder="support@example.com"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="platformDescription">Platform Description</Label>
                <Textarea
                  id="platformDescription"
                  value={settings.platformDescription}
                  onChange={(e) => updateSetting("platformDescription", e.target.value)}
                  placeholder="Describe your platform"
                  rows={3}
                />
              </div>

              <Separator />

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="maxFileUploadSize">Max File Upload Size (MB)</Label>
                  <Input
                    id="maxFileUploadSize"
                    type="number"
                    value={settings.maxFileUploadSize}
                    onChange={(e) => updateSetting("maxFileUploadSize", parseInt(e.target.value))}
                    min="1"
                    max="500"
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="sessionTimeout">Session Timeout (minutes)</Label>
                  <Input
                    id="sessionTimeout"
                    type="number"
                    value={settings.sessionTimeout}
                    onChange={(e) => updateSetting("sessionTimeout", parseInt(e.target.value))}
                    min="5"
                    max="480"
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* User Management */}
        <TabsContent value="users" className="space-y-6">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Users className="h-5 w-5" />
                User Management Settings
              </CardTitle>
              <CardDescription>
                Configure user registration and account settings
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label>Allow Self Registration</Label>
                    <p className="text-sm text-muted-foreground">
                      Allow users to create accounts without admin approval
                    </p>
                  </div>
                  <Switch
                    checked={settings.allowSelfRegistration}
                    onCheckedChange={(checked) => updateSetting("allowSelfRegistration", checked)}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label>Require Email Verification</Label>
                    <p className="text-sm text-muted-foreground">
                      Users must verify their email before accessing the platform
                    </p>
                  </div>
                  <Switch
                    checked={settings.requireEmailVerification}
                    onCheckedChange={(checked) => updateSettingWithAutoSave("requireEmailVerification", checked)}
                  />
                </div>
                
                {settings.requireEmailVerification && (
                  <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                    <h4 className="font-medium text-blue-900 mb-2">Email Verification Enabled</h4>
                    <p className="text-sm text-blue-700 mb-3">
                      New users will receive a verification email and must verify their email address before they can log in.
                    </p>
                    <div className="text-xs text-blue-600">
                      <p>• Verification emails are sent automatically upon registration</p>
                      <p>• Users can request a new verification email if needed</p>
                      <p>• Existing users are automatically marked as verified</p>
                    </div>
                  </div>
                )}
              </div>

              <Separator />

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="defaultUserRole">Default User Role</Label>
                  <Select
                    value={settings.defaultUserRole}
                    onValueChange={(value) => updateSetting("defaultUserRole", value)}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="STUDENT">Student</SelectItem>
                      <SelectItem value="TEACHER">Teacher</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="passwordMinLength">Minimum Password Length</Label>
                  <Input
                    id="passwordMinLength"
                    type="number"
                    value={settings.passwordMinLength}
                    onChange={(e) => updateSetting("passwordMinLength", parseInt(e.target.value))}
                    min="6"
                    max="32"
                  />
                </div>
              </div>

              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>Require Special Characters in Password</Label>
                  <p className="text-sm text-muted-foreground">
                    Passwords must contain special characters
                  </p>
                </div>
                <Switch
                  checked={settings.passwordRequireSpecialChars}
                  onCheckedChange={(checked) => updateSetting("passwordRequireSpecialChars", checked)}
                />
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Security Settings */}
        <TabsContent value="security" className="space-y-6">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Lock className="h-5 w-5" />
                Security Configuration
              </CardTitle>
              <CardDescription>
                Configure security policies and authentication settings
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>Enable Two-Factor Authentication</Label>
                  <p className="text-sm text-muted-foreground">
                    Require 2FA for enhanced security
                  </p>
                </div>
                <Switch
                  checked={settings.enableTwoFactorAuth}
                  onCheckedChange={(checked) => updateSetting("enableTwoFactorAuth", checked)}
                />
              </div>

              <Separator />

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="maxLoginAttempts">Max Login Attempts</Label>
                  <Input
                    id="maxLoginAttempts"
                    type="number"
                    value={settings.maxLoginAttempts}
                    onChange={(e) => updateSetting("maxLoginAttempts", parseInt(e.target.value))}
                    min="3"
                    max="10"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="lockoutDuration">Lockout Duration (minutes)</Label>
                  <Input
                    id="lockoutDuration"
                    type="number"
                    value={settings.lockoutDurationMinutes}
                    onChange={(e) => updateSetting("lockoutDurationMinutes", parseInt(e.target.value))}
                    min="5"
                    max="60"
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Notifications */}
        <TabsContent value="notifications" className="space-y-6">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Mail className="h-5 w-5" />
                Notification Settings
              </CardTitle>
              <CardDescription>
                Configure notification preferences and retention policies
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label>Email Notifications</Label>
                    <p className="text-sm text-muted-foreground">
                      Send notifications via email
                    </p>
                  </div>
                  <Switch
                    checked={settings.emailNotificationsEnabled}
                    onCheckedChange={(checked) => updateSetting("emailNotificationsEnabled", checked)}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label>Push Notifications</Label>
                    <p className="text-sm text-muted-foreground">
                      Send browser push notifications
                    </p>
                  </div>
                  <Switch
                    checked={settings.pushNotificationsEnabled}
                    onCheckedChange={(checked) => updateSetting("pushNotificationsEnabled", checked)}
                  />
                </div>
              </div>

              <Separator />

              <div className="space-y-2">
                <Label htmlFor="notificationRetention">Notification Retention (days)</Label>
                <Input
                  id="notificationRetention"
                  type="number"
                  value={settings.notificationRetentionDays}
                  onChange={(e) => updateSetting("notificationRetentionDays", parseInt(e.target.value))}
                  min="7"
                  max="365"
                />
                <p className="text-sm text-muted-foreground">
                  How long to keep notifications before automatic deletion
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Academic Settings */}
        <TabsContent value="academic" className="space-y-6">
          <Card className="glass-card">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Database className="h-5 w-5" />
                Academic Configuration
              </CardTitle>
              <CardDescription>
                Configure academic year and grading settings
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="academicYearStart">Academic Year Start</Label>
                  <Input
                    id="academicYearStart"
                    type="date"
                    value={settings.academicYearStart}
                    onChange={(e) => updateSetting("academicYearStart", e.target.value)}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="academicYearEnd">Academic Year End</Label>
                  <Input
                    id="academicYearEnd"
                    type="date"
                    value={settings.academicYearEnd}
                    onChange={(e) => updateSetting("academicYearEnd", e.target.value)}
                  />
                </div>
              </div>

              <Separator />

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="gradingScale">Default Grading Scale</Label>
                  <Select
                    value={settings.defaultGradingScale}
                    onValueChange={(value) => updateSetting("defaultGradingScale", value)}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="PERCENTAGE">Percentage (0-100%)</SelectItem>
                      <SelectItem value="LETTER">Letter Grades (A-F)</SelectItem>
                      <SelectItem value="POINTS">Points Based</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="defaultLatePenalty">Default Late Penalty (%)</Label>
                  <Input
                    id="defaultLatePenalty"
                    type="number"
                    value={settings.defaultLatePenalty}
                    onChange={(e) => updateSetting("defaultLatePenalty", parseInt(e.target.value))}
                    min="0"
                    max="100"
                  />
                </div>
              </div>

              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>Allow Late Submissions by Default</Label>
                  <p className="text-sm text-muted-foreground">
                    New assignments will allow late submissions unless specified otherwise
                  </p>
                </div>
                <Switch
                  checked={settings.allowLateSubmissions}
                  onCheckedChange={(checked) => updateSetting("allowLateSubmissions", checked)}
                />
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}