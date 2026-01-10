import { useEffect, useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { CheckCircle, XCircle, Mail, RefreshCw } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useToast } from "@/hooks/use-toast";
import { api } from "@/services/api";

export default function EmailVerification() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { toast } = useToast();
  
  const [verificationStatus, setVerificationStatus] = useState<'loading' | 'success' | 'error' | 'resend'>('loading');
  const [message, setMessage] = useState('');
  const [email, setEmail] = useState('');
  const [resending, setResending] = useState(false);
  
  const token = searchParams.get('token');

  useEffect(() => {
    if (token) {
      verifyEmail(token);
    } else {
      setVerificationStatus('resend');
      setMessage('Enter your email to resend verification link');
    }
  }, [token]);

  const verifyEmail = async (verificationToken: string) => {
    try {
      const response = await api.post(`/auth/verify-email?token=${verificationToken}`);
      
      if (response.data.success) {
        setVerificationStatus('success');
        setMessage(response.data.message);
        
        toast({
          title: "Email Verified!",
          description: "Your email has been successfully verified. You can now log in.",
        });
        
        // Redirect to login after 3 seconds
        setTimeout(() => {
          navigate('/login');
        }, 3000);
      } else {
        setVerificationStatus('error');
        setMessage(response.data.message || 'Verification failed');
      }
    } catch (error: any) {
      setVerificationStatus('error');
      setMessage(error.response?.data?.message || 'Verification failed. The link may be expired or invalid.');
      
      toast({
        title: "Verification Failed",
        description: error.response?.data?.message || "The verification link may be expired or invalid.",
        variant: "destructive",
      });
    }
  };

  const resendVerificationEmail = async () => {
    if (!email.trim()) {
      toast({
        title: "Email Required",
        description: "Please enter your email address",
        variant: "destructive",
      });
      return;
    }

    try {
      setResending(true);
      await api.post('/auth/resend-verification', { email });
      
      toast({
        title: "Email Sent",
        description: "Verification email has been sent to your email address",
      });
      
      setMessage('Verification email sent! Please check your inbox and spam folder.');
    } catch (error: any) {
      toast({
        title: "Failed to Send Email",
        description: error.response?.data?.message || "Failed to send verification email",
        variant: "destructive",
      });
    } finally {
      setResending(false);
    }
  };

  const renderContent = () => {
    switch (verificationStatus) {
      case 'loading':
        return (
          <div className="text-center">
            <RefreshCw className="h-12 w-12 animate-spin text-primary mx-auto mb-4" />
            <h2 className="text-2xl font-bold mb-2">Verifying Email...</h2>
            <p className="text-muted-foreground">Please wait while we verify your email address.</p>
          </div>
        );

      case 'success':
        return (
          <div className="text-center">
            <CheckCircle className="h-12 w-12 text-success mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-success mb-2">Email Verified!</h2>
            <p className="text-muted-foreground mb-4">{message}</p>
            <p className="text-sm text-muted-foreground">Redirecting to login page...</p>
            <Button onClick={() => navigate('/login')} className="mt-4">
              Go to Login
            </Button>
          </div>
        );

      case 'error':
        return (
          <div className="text-center">
            <XCircle className="h-12 w-12 text-destructive mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-destructive mb-2">Verification Failed</h2>
            <p className="text-muted-foreground mb-4">{message}</p>
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email">Enter your email to resend verification</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="your@email.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
              <Button onClick={resendVerificationEmail} disabled={resending} className="w-full">
                {resending ? (
                  <>
                    <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
                    Sending...
                  </>
                ) : (
                  <>
                    <Mail className="mr-2 h-4 w-4" />
                    Resend Verification Email
                  </>
                )}
              </Button>
            </div>
          </div>
        );

      case 'resend':
        return (
          <div className="text-center">
            <Mail className="h-12 w-12 text-primary mx-auto mb-4" />
            <h2 className="text-2xl font-bold mb-2">Email Verification</h2>
            <p className="text-muted-foreground mb-4">{message}</p>
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email">Email Address</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="your@email.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
              <Button onClick={resendVerificationEmail} disabled={resending} className="w-full">
                {resending ? (
                  <>
                    <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
                    Sending...
                  </>
                ) : (
                  <>
                    <Mail className="mr-2 h-4 w-4" />
                    Send Verification Email
                  </>
                )}
              </Button>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle className="text-center">Email Verification</CardTitle>
          <CardDescription className="text-center">
            Verify your email address to complete registration
          </CardDescription>
        </CardHeader>
        <CardContent>
          {renderContent()}
        </CardContent>
      </Card>
    </div>
  );
}