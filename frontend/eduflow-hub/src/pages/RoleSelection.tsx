// import { useNavigate } from "react-router-dom";
// import { Button } from "@/components/ui/button";
// import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
// import { GraduationCap, BookOpen, Shield } from "lucide-react";

// const roles = [
//   {
//     id: "student",
//     title: "Student",
//     description: "Access classes, submit assignments, and track your progress",
//     icon: GraduationCap,
//     path: "/student",
//     color: "text-primary",
//     bgColor: "bg-primary/10",
//   },
//   {
//     id: "teacher",
//     title: "Teacher",
//     description: "Manage classes, create assignments, and evaluate student work",
//     icon: BookOpen,
//     path: "/teacher",
//     color: "text-accent",
//     bgColor: "bg-accent/10",
//   },
//   {
//     id: "admin",
//     title: "Admin",
//     description: "Oversee the system, manage users, and view analytics",
//     icon: Shield,
//     path: "/admin",
//     color: "text-success",
//     bgColor: "bg-success/10",
//   },
// ];

// export default function RoleSelection() {
//   const navigate = useNavigate();

//   return (
//     <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-background via-background to-primary/5 p-4">
//       <div className="w-full max-w-5xl space-y-8 animate-slide-up">
//         <div className="text-center space-y-3">
//           <div className="flex justify-center mb-4">
//             <div className="flex h-16 w-16 items-center justify-center rounded-2xl gradient-primary shadow-lg shadow-primary/30">
//               <GraduationCap className="h-9 w-9 text-white" />
//             </div>
//           </div>
//           <h1 className="text-4xl font-bold tracking-tight">Welcome to E-Learn Hub</h1>
//           <p className="text-lg text-muted-foreground">
//             Select your role to continue to your dashboard
//           </p>
//         </div>

//         <div className="grid gap-6 md:grid-cols-3">
//           {roles.map((role, index) => (
//             <Card
//               key={role.id}
//               className="group relative overflow-hidden border-2 transition-all hover:border-primary hover:shadow-xl cursor-pointer"
//               style={{ animationDelay: `${index * 100}ms` }}
//               onClick={() => navigate("/login")}
//             >
//               <div className="absolute inset-0 bg-gradient-to-br from-transparent via-transparent to-primary/5 opacity-0 transition-opacity group-hover:opacity-100" />
              
//               <CardHeader className="relative space-y-4">
//                 <div className={`flex h-14 w-14 items-center justify-center rounded-xl ${role.bgColor}`}>
//                   <role.icon className={`h-7 w-7 ${role.color}`} />
//                 </div>
//                 <div className="space-y-1">
//                   <CardTitle className="text-xl">{role.title}</CardTitle>
//                   <CardDescription className="text-sm">{role.description}</CardDescription>
//                 </div>
//               </CardHeader>

//               <CardContent className="relative">
//                 <Button
//                   className="w-full"
//                   variant={role.id === "student" ? "default" : "outline"}
//                 >
//                   Continue as {role.title}
//                 </Button>
//               </CardContent>

//               <div className="absolute top-0 right-0 h-24 w-24 -translate-y-12 translate-x-12 rounded-full bg-primary/10 opacity-0 blur-2xl transition-opacity group-hover:opacity-100" />
//             </Card>
//           ))}
//         </div>

//         <div className="text-center">
//           <p className="text-sm text-muted-foreground">
//             Need help? <Button variant="link" className="h-auto p-0 text-sm">Contact Support</Button>
//           </p>
//         </div>
//       </div>
//     </div>
//   );
// }


import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { GraduationCap, BookOpen, Shield } from "lucide-react";

const roles = [
  {
    id: "student",
    title: "Student",
    description: "Access classes, submit assignments, and track your progress",
    icon: GraduationCap,
    path: "/student",
    color: "text-primary",
    bgColor: "bg-primary/10",
    buttonColor: "bg-blue-600 hover:bg-blue-700 text-white",
  },
  {
    id: "teacher",
    title: "Teacher",
    description: "Manage classes, create assignments, and evaluate student work",
    icon: BookOpen,
    path: "/teacher",
    color: "text-accent",
    bgColor: "bg-accent/10",
    buttonColor: "bg-orange-500 hover:bg-orange-600 text-white",
  },
  {
    id: "admin",
    title: "Admin",
    description: "Oversee the system, manage users, and view analytics",
    icon: Shield,
    path: "/admin",
    color: "text-success",
    bgColor: "bg-success/10",
    buttonColor: "bg-green-600 hover:bg-green-700 text-white",
  },
];

export default function RoleSelection() {
  const navigate = useNavigate();

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-background via-background to-primary/5 p-4">
      <div className="w-full max-w-5xl space-y-8 animate-slide-up">
        
        {/* Header */}
        <div className="text-center space-y-3">
          <div className="flex justify-center mb-4">
            <div className="flex h-16 w-16 items-center justify-center rounded-2xl gradient-primary shadow-lg shadow-primary/30">
              <GraduationCap className="h-9 w-9 text-white" />
            </div>
          </div>
          <h1 className="text-4xl font-bold tracking-tight">Welcome to E-Learn Hub</h1>
          <p className="text-lg text-muted-foreground">
            Select your role to continue to your dashboard
          </p>
        </div>

        {/* Role Cards */}
        <div className="grid gap-6 md:grid-cols-3">
          {roles.map((role, index) => (
            <Card
              key={role.id}
              className="group relative overflow-hidden border-2 transition-all hover:border-primary hover:shadow-xl cursor-pointer"
              style={{ animationDelay: `${index * 100}ms` }}
              onClick={() => navigate("/login")}
            >
              <div className="absolute inset-0 bg-gradient-to-br from-transparent via-transparent to-primary/5 opacity-0 transition-opacity group-hover:opacity-100" />
              
              <CardHeader className="relative space-y-4">
                <div className={`flex h-14 w-14 items-center justify-center rounded-xl ${role.bgColor}`}>
                  <role.icon className={`h-7 w-7 ${role.color}`} />
                </div>
                <div className="space-y-1">
                  <CardTitle className="text-xl">{role.title}</CardTitle>
                  <CardDescription className="text-sm">{role.description}</CardDescription>
                </div>
              </CardHeader>

              <CardContent className="relative">
                <Button className={`w-full ${role.buttonColor}`}>
                  Continue as {role.title}
                </Button>
              </CardContent>

              <div className="absolute top-0 right-0 h-24 w-24 -translate-y-12 translate-x-12 rounded-full bg-primary/10 opacity-0 blur-2xl transition-opacity group-hover:opacity-100" />
            </Card>
          ))}
        </div>

        {/* Footer */}
        <div className="text-center">
          <p className="text-sm text-muted-foreground">
            Need help? <Button variant="link" className="h-auto p-0 text-sm">Contact Support</Button>
          </p>
        </div>

      </div>
    </div>
  );
}
