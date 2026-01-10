import { useState } from "react";
import { Outlet, useLocation } from "react-router-dom";
import { SidebarProvider } from "@/components/ui/sidebar";
import { DashboardSidebar } from "./DashboardSidebar";
import { DashboardHeader } from "./DashboardHeader";

interface DashboardLayoutProps {
  role?: "student" | "teacher" | "admin";
}

export function DashboardLayout({ role = "student" }: DashboardLayoutProps) {
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);

  return (
    <SidebarProvider>
      <div className="flex min-h-screen w-full">
        <DashboardSidebar collapsed={collapsed} setCollapsed={setCollapsed} role={role} />
        
        <div className="flex-1 flex flex-col">
          <DashboardHeader />
          
          <main className="flex-1 p-6 animate-slide-up">
            <Outlet />
          </main>
        </div>
      </div>
    </SidebarProvider>
  );
}
