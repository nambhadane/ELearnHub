// import { useState, FormEvent } from "react";
// import { Link, useNavigate, useLocation } from "react-router-dom";
// import { Button } from "@/components/ui/button";
// import { Input } from "@/components/ui/input";
// import { Label } from "@/components/ui/label";
// import { Card } from "@/components/ui/card";
// import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
// import { BookOpen } from "lucide-react";
// import { useToast } from "@/hooks/use-toast";
// import { loginApi, registerApi } from "@/services/api";

// const Login = () => {
//   const navigate = useNavigate();
//   const location = useLocation();
//   const { toast } = useToast();
  
//   // Get the intended destination from location state (if redirected from protected route)
//   const from = (location.state as { from?: { pathname: string } })?.from?.pathname || null;

//   const [loginUsername, setLoginUsername] = useState("");
//   const [loginPassword, setLoginPassword] = useState("");
//   const [signupUsername, setSignupUsername] = useState("");
//   const [signupEmail, setSignupEmail] = useState("");
//   const [signupPassword, setSignupPassword] = useState("");
//   const [signupRole, setSignupRole] = useState<"Student" | "Teacher">("Teacher");
//   const [isSubmittingLogin, setIsSubmittingLogin] = useState(false);
//   const [isSubmittingSignup, setIsSubmittingSignup] = useState(false);
//   const [activeTab, setActiveTab] = useState<"login" | "signup">("login");

//   const handleLogin = async (e: FormEvent) => {
//     e.preventDefault();

//     if (!loginUsername || !loginPassword) {
//       toast({
//         title: "Missing information",
//         description: "Please enter both username and password.",
//         variant: "destructive",
//       });
//       return;
//     }

//     try {
//       setIsSubmittingLogin(true);

//       const res = await loginApi({
//         username: loginUsername,
//         password: loginPassword,
//       });

//       // Store token (and role if provided) for later use
//       localStorage.setItem("authToken", res.token);
//       if (res.role) {
//         localStorage.setItem("authRole", res.role);
//       }

//       // Fetch and store user profile
//       try {
//         const profileResponse = await fetch('/api/teacher/profile', {
//           headers: {
//             'Authorization': `Bearer ${res.token}`,
//           },
//         });
//         if (profileResponse.ok) {
//           const profile = await profileResponse.json();
//           localStorage.setItem("user", JSON.stringify(profile));
//         }
//       } catch (error) {
//         console.error("Failed to fetch user profile:", error);
//       }

//       toast({
//         title: "Login successful",
//         description: "Welcome back to E-Learn Hub.",
//       });

//       // Redirect to intended page or based on role
//       if (from) {
//         // Redirect back to the page user was trying to access
//         navigate(from, { replace: true });
//       } else {
//         // Redirect based on role
//         const role = res.role?.toUpperCase();
//         if (role === "TEACHER") {
//           navigate("/teacher", { replace: true });
//         } else if (role === "ADMIN") {
//           navigate("/admin", { replace: true });
//         } else {
//           navigate("/student", { replace: true });
//         }
//       }
//     } catch (err) {
//       const message = err instanceof Error ? err.message : "Login failed";
//       toast({
//         title: "Login failed",
//         description: message,
//         variant: "destructive",
//       });
//     } finally {
//       setIsSubmittingLogin(false);
//     }
//   };

//   const handleSignup = async (e: FormEvent) => {
//     e.preventDefault();

//     if (!signupUsername || !signupEmail || !signupPassword) {
//       toast({
//         title: "Missing information",
//         description: "Username, email, and password are required.",
//         variant: "destructive",
//       });
//       return;
//     }

//     try {
//       setIsSubmittingSignup(true);

//       await registerApi({
//         username: signupUsername,
//         password: signupPassword,
//         email: signupEmail,
//         role: signupRole.toUpperCase(),
//       });

//       toast({
//         title: "Registration successful",
//         description: "You can now sign in with your credentials.",
//       });

//       // Clear form and switch to login tab
//       setSignupUsername("");
//       setSignupEmail("");
//       setSignupPassword("");
//       setSignupRole("Teacher");
//       setActiveTab("login");
//     } catch (err) {
//       const message = err instanceof Error ? err.message : "Registration failed";
//       toast({
//         title: "Registration failed",
//         description: message,
//         variant: "destructive",
//       });
//     } finally {
//       setIsSubmittingSignup(false);
//     }
//   };

//   return (
//     <div className="min-h-screen bg-gradient-hero flex items-center justify-center p-4">
//       <div className="w-full max-w-md">
//         {/* Logo */}
//         <Link to="/" className="flex items-center justify-center gap-2 mb-8">
//           <div className="w-12 h-12 rounded-xl bg-white flex items-center justify-center shadow-soft">
//             <BookOpen className="w-7 h-7 text-primary" />
//           </div>
//           <span className="font-bold text-2xl text-white">E-Learn Hub</span>
//         </Link>

//         {/* Auth Card */}
//         <Card className="p-8 shadow-hover">
//           <Tabs value={activeTab} onValueChange={(value) => setActiveTab(value as "login" | "signup")} className="w-full">
//             <TabsList className="grid w-full grid-cols-2 mb-6">
//               <TabsTrigger value="login">Sign In</TabsTrigger>
//               <TabsTrigger value="signup">Sign Up</TabsTrigger>
//             </TabsList>

//             {/* Login Form */}
//             <TabsContent value="login">
//               <form onSubmit={handleLogin} className="space-y-4">
//                 <div>
//                   <h2 className="text-2xl font-bold text-foreground mb-2">
//                     Welcome back
//                   </h2>
//                   <p className="text-muted-foreground">
//                     Enter your credentials to access your account
//                   </p>
//                 </div>

//                 <div className="space-y-4">
//                   <div className="space-y-2">
//                     <Label htmlFor="username">Username</Label>
//                     <Input
//                       id="username"
//                       type="text"
//                       placeholder="Enter your username (not email)"
//                       value={loginUsername}
//                       onChange={(e) => setLoginUsername(e.target.value)}
//                     />
//                     <p className="text-xs text-muted-foreground">
//                       Use the username you created during registration
//                     </p>
//                   </div>
//                   <div className="space-y-2">
//                     <Label htmlFor="password">Password</Label>
//                     <Input
//                       id="password"
//                       type="password"
//                       placeholder="••••••••"
//                       value={loginPassword}
//                       onChange={(e) => setLoginPassword(e.target.value)}
//                     />
//                   </div>
//                   <div className="flex items-center justify-between">
//                     <label className="flex items-center gap-2 text-sm">
//                       <input type="checkbox" className="rounded" />
//                       <span className="text-muted-foreground">Remember me</span>
//                     </label>
//                     <a href="#" className="text-sm text-primary hover:underline">
//                       Forgot password?
//                     </a>
//                   </div>
//                   <Button
//                     type="submit"
//                     className="w-full bg-gradient-primary hover:opacity-90"
//                     disabled={isSubmittingLogin}
//                   >
//                     {isSubmittingLogin ? "Signing In..." : "Sign In"}
//                   </Button>
//                 </div>
//               </form>
//             </TabsContent>

//             {/* Signup Form */}
//             <TabsContent value="signup">
//               <form onSubmit={handleSignup} className="space-y-4">
//                 <div>
//                   <h2 className="text-2xl font-bold text-foreground mb-2">
//                     Create an account
//                   </h2>
//                   <p className="text-muted-foreground">
//                     Start your learning journey today
//                   </p>
//                 </div>

//                 <div className="space-y-4">
//                   <div className="space-y-2">
//                     <Label htmlFor="signup-username">Username</Label>
//                     <Input
//                       id="signup-username"
//                       type="text"
//                       placeholder="Choose a username"
//                       value={signupUsername}
//                       onChange={(e) => setSignupUsername(e.target.value)}
//                     />
//                   </div>
//                   <div className="space-y-2">
//                     <Label htmlFor="signup-email">Email</Label>
//                     <Input
//                       id="signup-email"
//                       type="email"
//                       placeholder="your.email@example.com"
//                       value={signupEmail}
//                       onChange={(e) => setSignupEmail(e.target.value)}
//                     />
//                   </div>
//                   <div className="space-y-2">
//                     <Label htmlFor="signup-password">Password</Label>
//                     <Input
//                       id="signup-password"
//                       type="password"
//                       placeholder="••••••••"
//                       value={signupPassword}
//                       onChange={(e) => setSignupPassword(e.target.value)}
//                     />
//                   </div>
//                   <div className="space-y-2">
//                     <Label htmlFor="role">I am a</Label>
//                     <select
//                       id="role"
//                       className="w-full px-3 py-2 border border-input rounded-md bg-background"
//                       value={signupRole}
//                       onChange={(e) => setSignupRole(e.target.value as "Student" | "Teacher")}
//                     >
//                       <option value="Teacher">Teacher</option>
//                       <option value="Student">Student</option>
//                     </select>
//                   </div>
//                   <Button
//                     type="submit"
//                     className="w-full bg-gradient-primary hover:opacity-90"
//                     disabled={isSubmittingSignup}
//                   >
//                     {isSubmittingSignup ? "Creating Account..." : "Create Account"}
//                   </Button>
//                   <p className="text-xs text-center text-muted-foreground">
//                     By signing up, you agree to our Terms of Service and Privacy Policy
//                   </p>
//                 </div>
//               </form>
//             </TabsContent>
//           </Tabs>
//         </Card>

//         <p className="text-center text-white/80 text-sm mt-6">
//           Need help? <a href="#" className="text-white hover:underline">Contact Support</a>
//         </p>
//       </div>
//     </div>
//   );
// };

// export default Login;


import { useState, FormEvent } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { BookOpen } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { loginApi, registerApi } from "@/services/api";

const Login = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { toast } = useToast();
  
  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || null;

  const [loginUsername, setLoginUsername] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [signupUsername, setSignupUsername] = useState("");
  const [signupName, setSignupName] = useState("");
  const [signupEmail, setSignupEmail] = useState("");
  const [signupPassword, setSignupPassword] = useState("");
  const [signupRole, setSignupRole] = useState<"Student" | "Teacher">("Teacher");
  const [isSubmittingLogin, setIsSubmittingLogin] = useState(false);
  const [isSubmittingSignup, setIsSubmittingSignup] = useState(false);
  const [activeTab, setActiveTab] = useState<"login" | "signup">("login");

  const handleLogin = async (e: FormEvent) => {
    e.preventDefault();

    if (!loginUsername || !loginPassword) {
      toast({
        title: "Missing information",
        description: "Please enter both username and password.",
        variant: "destructive",
      });
      return;
    }

    try {
      setIsSubmittingLogin(true);

      const res = await loginApi({
        username: loginUsername,
        password: loginPassword,
      });

      localStorage.setItem("authToken", res.token);
      if (res.role) localStorage.setItem("authRole", res.role);

      try {
        const profileResponse = await fetch('/api/teacher/profile', {
          headers: { 'Authorization': `Bearer ${res.token}` },
        });

        if (profileResponse.ok) {
          const profile = await profileResponse.json();
          localStorage.setItem("user", JSON.stringify(profile));
        }
      } catch (error) {
        console.error("Failed to fetch user profile:", error);
      }

      toast({
        title: "Login successful",
        description: "Welcome back to E-Learn Hub.",
      });

      if (from) navigate(from, { replace: true });
      else {
        const role = res.role?.toUpperCase();
        if (role === "TEACHER") navigate("/teacher", { replace: true });
        else if (role === "ADMIN") navigate("/admin", { replace: true });
        else navigate("/student", { replace: true });
      }
    } catch (err: any) {
      // Check if this is an email verification error
      if (err.status === 403 && err.data?.emailVerificationRequired) {
        toast({
          title: "Email Verification Required",
          description: "Please check your email and verify your account before logging in.",
          variant: "destructive",
        });
        
        // Optionally show resend verification option
        // You could set a state here to show a "Resend Verification Email" button
      } else {
        const message = err instanceof Error ? err.message : "Login failed";
        toast({
          title: "Login failed",
          description: message,
          variant: "destructive",
        });
      }
    } finally {
      setIsSubmittingLogin(false);
    }
  };

  const handleSignup = async (e: FormEvent) => {
    e.preventDefault();

    if (!signupUsername || !signupName || !signupEmail || !signupPassword) {
      toast({
        title: "Missing information",
        description: "All fields are required.",
        variant: "destructive",
      });
      return;
    }

    try {
      setIsSubmittingSignup(true);

      const response = await registerApi({
        username: signupUsername,
        name: signupName,
        password: signupPassword,
        email: signupEmail,
        role: signupRole.toUpperCase(),
      });

      toast({
        title: "Registration successful",
        description: response.message || "Registration completed successfully.",
      });

      setSignupUsername("");
      setSignupName("");
      setSignupEmail("");
      setSignupPassword("");
      setSignupRole("Teacher");
      setActiveTab("login");
    } catch (err) {
      const message = err instanceof Error ? err.message : "Registration failed";
      toast({
        title: "Registration failed",
        description: message,
        variant: "destructive",
      });
    } finally {
      setIsSubmittingSignup(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-hero flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        
        {/* Logo */}
        <Link to="/" className="flex items-center justify-center gap-2 mb-8">
          <div className="w-12 h-12 rounded-xl bg-white flex items-center justify-center shadow-soft">
            <BookOpen className="w-7 h-7 text-primary" />
          </div>
          <span className="font-bold text-2xl text-white">E-Learn Hub</span>
        </Link>

        {/* Auth Card */}
        <Card className="p-8 shadow-hover">
          <Tabs value={activeTab} onValueChange={(value) => setActiveTab(value as "login" | "signup")} className="w-full">
            
            <TabsList className="grid w-full grid-cols-2 mb-6">
              <TabsTrigger value="login">Sign In</TabsTrigger>
              <TabsTrigger value="signup">Sign Up</TabsTrigger>
            </TabsList>

            {/* Login Form */}
            <TabsContent value="login">
              <form onSubmit={handleLogin} className="space-y-4">
                
                <div>
                  <h2 className="text-2xl font-bold text-foreground mb-2">Welcome back</h2>
                  <p className="text-muted-foreground">Enter your credentials to access your account</p>
                </div>

                <div className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="username">Username</Label>
                    <Input
                      id="username"
                      type="text"
                      placeholder="Enter your username"
                      value={loginUsername}
                      onChange={(e) => setLoginUsername(e.target.value)}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="password">Password</Label>
                    <Input
                      id="password"
                      type="password"
                      placeholder="••••••••"
                      value={loginPassword}
                      onChange={(e) => setLoginPassword(e.target.value)}
                    />
                  </div>

                  <Button
                    type="submit"
                    className="w-full bg-blue-600 hover:bg-blue-700 text-white"
                    disabled={isSubmittingLogin}
                  >
                    {isSubmittingLogin ? "Signing In..." : "Sign In"}
                  </Button>
                </div>
              </form>
            </TabsContent>

            {/* Signup Form */}
            <TabsContent value="signup">
              <form onSubmit={handleSignup} className="space-y-4">

                <div>
                  <h2 className="text-2xl font-bold text-foreground mb-2">Create an account</h2>
                  <p className="text-muted-foreground">Start your learning journey today</p>
                </div>

                <div className="space-y-4">
                  
                  <div className="space-y-2">
                    <Label htmlFor="signup-username">Username</Label>
                    <Input
                      id="signup-username"
                      type="text"
                      placeholder="Choose a username"
                      value={signupUsername}
                      onChange={(e) => setSignupUsername(e.target.value)}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="signup-name">Full Name</Label>
                    <Input
                      id="signup-name"
                      type="text"
                      placeholder="Enter your full name"
                      value={signupName}
                      onChange={(e) => setSignupName(e.target.value)}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="signup-email">Email</Label>
                    <Input
                      id="signup-email"
                      type="email"
                      placeholder="your.email@example.com"
                      value={signupEmail}
                      onChange={(e) => setSignupEmail(e.target.value)}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="signup-password">Password</Label>
                    <Input
                      id="signup-password"
                      type="password"
                      placeholder="••••••••"
                      value={signupPassword}
                      onChange={(e) => setSignupPassword(e.target.value)}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="role">I am a</Label>
                    <select
                      id="role"
                      className="w-full px-3 py-2 border border-input rounded-md bg-background"
                      value={signupRole}
                      onChange={(e) => setSignupRole(e.target.value as "Student" | "Teacher")}
                    >
                      <option value="Teacher">Teacher</option>
                      <option value="Student">Student</option>
                    </select>
                  </div>

                  <Button
                    type="submit"
                    className="w-full bg-blue-600 hover:bg-blue-700 text-white"
                    disabled={isSubmittingSignup}
                  >
                    {isSubmittingSignup ? "Creating Account..." : "Create Account"}
                  </Button>

                </div>
              </form>
            </TabsContent>

          </Tabs>
        </Card>

        <p className="text-center text-white/80 text-sm mt-6">
          Need help? <a href="#" className="text-white hover:underline">Contact Support</a>
        </p>

      </div>
    </div>
  );
};

export default Login;
