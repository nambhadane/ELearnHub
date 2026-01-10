import { useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getLiveClassById } from "@/services/api";
import { useToast } from "@/hooks/use-toast";
import { Loader2 } from "lucide-react";

export default function LiveClassRoom() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { toast } = useToast();

  useEffect(() => {
    loadLiveClassAndRedirect();
  }, [id]);

  const loadLiveClassAndRedirect = async () => {
    try {
      setLoading(true);
      const data = await getLiveClassById(Number(id));
      
      if (data.status !== "LIVE") {
        toast({
          title: "Class Not Live",
          description: "This class is not currently active",
          variant: "destructive",
        });
        navigate(-1);
        return;
      }

      // Get user info
      const user = JSON.parse(localStorage.getItem("user") || "{}");
      const displayName = user.name || "User";
      
      // Redirect directly to Jitsi URL - this avoids the moderator issue!
      const jitsiUrl = `https://meet.jit.si/${data.meetingId}#userInfo.displayName="${encodeURIComponent(displayName)}"`;
      
      // Open in same window
      window.location.href = jitsiUrl;
      
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load live class",
        variant: "destructive",
      });
      navigate(-1);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="text-center">
        <Loader2 className="h-12 w-12 animate-spin text-primary mx-auto mb-4" />
        <p className="text-muted-foreground">Redirecting to meeting...</p>
        <p className="text-sm text-muted-foreground mt-2">
          If you're not redirected, <a href="#" onClick={() => navigate(-1)} className="text-primary underline">go back</a>
        </p>
      </div>
    </div>
  );
}
function setLoading(arg0: boolean) {
  throw new Error("Function not implemented.");
}

