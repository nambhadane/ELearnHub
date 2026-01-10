import { Home, BookOpen, FileText, MessageSquare, Award, User, ChevronLeft, GraduationCap, Users, Settings, BarChart, Calendar, ClipboardList, HelpCircle } from "lucide-react";
import { NavLink } from "@/components/NavLink";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarTrigger,
} from "@/components/ui/sidebar";

const studentNavItems = [
  { title: "Dashboard", url: "/student", icon: Home },
  { title: "My Classes", url: "/student/classes", icon: BookOpen },
  { title: "Timetable", url: "/student/timetable", icon: Calendar },
  { title: "Assignments", url: "/student/assignments", icon: FileText },
  { title: "Messages", url: "/student/messages", icon: MessageSquare },
  { title: "Grades", url: "/student/grades", icon: Award },
  { title: "Profile", url: "/student/profile", icon: User },
];

const teacherNavItems = [
  { title: "Dashboard", url: "/teacher", icon: Home },
  { title: "My Classes", url: "/teacher/classes", icon: BookOpen },
  { title: "Create Assignment", url: "/teacher/create-assignment", icon: FileText },
  { title: "Quizzes", url: "/teacher/quizzes", icon: ClipboardList },
  { title: "Submissions", url: "/teacher/submissions", icon: Award },
  { title: "Messages", url: "/teacher/messages", icon: MessageSquare },
  { title: "Profile", url: "/teacher/profile", icon: User },
];

const adminNavItems = [
  { title: "Dashboard", url: "/admin", icon: Home },
  { title: "Users", url: "/admin/users", icon: Users },
  { title: "Classes", url: "/admin/classes", icon: BookOpen },
  { title: "Assignments", url: "/admin/assignments", icon: FileText },
  { title: "Quizzes", url: "/admin/quizzes", icon: HelpCircle },
  { title: "Reports", url: "/admin/reports", icon: BarChart },
  { title: "Settings", url: "/admin/settings", icon: Settings },
];

interface DashboardSidebarProps {
  collapsed: boolean;
  setCollapsed: (collapsed: boolean) => void;
  role?: "student" | "teacher" | "admin";
}

export function DashboardSidebar({ collapsed, setCollapsed, role = "student" }: DashboardSidebarProps) {
  const navItems = 
    role === "teacher" ? teacherNavItems : 
    role === "admin" ? adminNavItems : 
    studentNavItems;

  const roleLabel = 
    role === "teacher" ? "Teacher Portal" : 
    role === "admin" ? "Admin Panel" : 
    "Student Portal";

  return (
    <Sidebar
      className={`border-r border-sidebar-border transition-all duration-300 ${
        collapsed ? "w-16" : "w-64"
      }`}
    >
      <div className="flex h-16 items-center justify-between border-b border-sidebar-border px-4">
        {!collapsed && (
          <div className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
              <GraduationCap className="h-5 w-5" />
            </div>
            <span className="text-lg font-bold">E-Learn Hub</span>
          </div>
        )}
        <SidebarTrigger
          onClick={() => setCollapsed(!collapsed)}
          className={collapsed ? "mx-auto" : ""}
        >
          <ChevronLeft
            className={`h-4 w-4 transition-transform ${
              collapsed ? "rotate-180" : ""
            }`}
          />
        </SidebarTrigger>
      </div>

      <SidebarContent>
        <SidebarGroup>
          {!collapsed && (
            <SidebarGroupLabel className="px-4 text-xs font-semibold uppercase text-sidebar-foreground/60">
              {roleLabel}
            </SidebarGroupLabel>
          )}
          <SidebarGroupContent>
            <SidebarMenu className="gap-1 px-2">
              {navItems.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild>
                    <NavLink
                      to={item.url}
                      end
                      className="group relative flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all hover:bg-sidebar-accent"
                      activeClassName="bg-primary text-primary-foreground shadow-sm"
                    >
                      <item.icon className="h-5 w-5 flex-shrink-0" />
                      {!collapsed && <span>{item.title}</span>}
                      {collapsed && (
                        <span className="absolute left-full ml-6 hidden whitespace-nowrap rounded-md bg-sidebar px-2 py-1 text-xs group-hover:block">
                          {item.title}
                        </span>
                      )}
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
    </Sidebar>
  );
}
