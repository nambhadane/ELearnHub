import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, Upload, FileText, Play, Download, Loader2, Trash2, File } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { useToast } from "@/hooks/use-toast";
import { getMaterialsByClass, uploadMaterial, downloadMaterial, deleteMaterial } from "@/services/api";

interface Material {
  id: number;
  title: string;
  description?: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  classId: number;
  uploadedByName: string;
  uploadedAt: string;
}

export default function ClassMaterials() {
  const { classId } = useParams<{ classId: string }>();
  const navigate = useNavigate();
  const { toast } = useToast();
  
  const [materials, setMaterials] = useState<Material[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [showUploadDialog, setShowUploadDialog] = useState(false);
  const [uploadForm, setUploadForm] = useState({
    title: "",
    description: "",
    file: null as File | null,
  });

  useEffect(() => {
    if (classId) {
      fetchMaterials();
    }
  }, [classId]);

  const fetchMaterials = async () => {
    try {
      setLoading(true);
      const data = await getMaterialsByClass(Number(classId));
      setMaterials(data);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load materials",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleUpload = async () => {
    if (!uploadForm.file || !uploadForm.title) {
      toast({
        title: "Error",
        description: "Please provide a title and select a file",
        variant: "destructive",
      });
      return;
    }

    try {
      setUploading(true);
      await uploadMaterial(
        uploadForm.file,
        uploadForm.title,
        uploadForm.description,
        Number(classId)
      );
      
      toast({
        title: "Success",
        description: "Material uploaded successfully",
      });
      
      setShowUploadDialog(false);
      setUploadForm({ title: "", description: "", file: null });
      fetchMaterials();
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to upload material",
        variant: "destructive",
      });
    } finally {
      setUploading(false);
    }
  };

  const handleDownload = async (materialId: number, fileName: string) => {
    try {
      await downloadMaterial(materialId, fileName);
      toast({
        title: "Success",
        description: "Download started",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to download material",
        variant: "destructive",
      });
    }
  };

  const handleDelete = async (materialId: number) => {
    if (!confirm("Are you sure you want to delete this material?")) return;

    try {
      await deleteMaterial(materialId);
      toast({
        title: "Success",
        description: "Material deleted successfully",
      });
      fetchMaterials();
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete material",
        variant: "destructive",
      });
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
    return (bytes / (1024 * 1024)).toFixed(1) + " MB";
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", { 
      month: "short", 
      day: "numeric", 
      year: "numeric" 
    });
  };

  const getFileIcon = (fileType: string) => {
    if (fileType === "video") return <Play className="h-5 w-5 text-primary" />;
    if (fileType === "pdf") return <FileText className="h-5 w-5 text-red-500" />;
    if (fileType === "document") return <FileText className="h-5 w-5 text-blue-500" />;
    return <File className="h-5 w-5 text-primary" />;
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

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => navigate("/teacher/classes")}
          >
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div className="space-y-2">
            <h1 className="text-3xl font-bold tracking-tight">Class Materials</h1>
            <p className="text-muted-foreground">
              Manage materials for Class {classId}
            </p>
          </div>
        </div>
        <Button onClick={() => setShowUploadDialog(true)}>
          <Upload className="mr-2 h-4 w-4" />
          Upload Material
        </Button>
      </div>

      {materials.length === 0 ? (
        <Card className="glass-card">
          <CardContent className="flex flex-col items-center justify-center py-12">
            <FileText className="h-12 w-12 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">No materials yet</h3>
            <p className="text-muted-foreground text-center mb-4">
              Upload your first material to get started
            </p>
            <Button onClick={() => setShowUploadDialog(true)}>
              <Upload className="mr-2 h-4 w-4" />
              Upload Material
            </Button>
          </CardContent>
        </Card>
      ) : (
        <Card className="glass-card">
          <CardHeader>
            <CardTitle>Materials ({materials.length})</CardTitle>
            <CardDescription>All uploaded materials for this class</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {materials.map((material) => (
                <div
                  key={material.id}
                  className="flex items-center justify-between rounded-lg border border-border bg-card p-4"
                >
                  <div className="flex items-center gap-4 flex-1">
                    <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                      {getFileIcon(material.fileType)}
                    </div>
                    <div className="flex-1">
                      <h4 className="font-medium">{material.title}</h4>
                      <p className="text-sm text-muted-foreground">
                        {material.fileName} • {formatFileSize(material.fileSize)} • 
                        Uploaded by {material.uploadedByName} on {formatDate(material.uploadedAt)}
                      </p>
                      {material.description && (
                        <p className="text-sm text-muted-foreground mt-1">{material.description}</p>
                      )}
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button 
                      variant="outline" 
                      size="sm"
                      onClick={() => handleDownload(material.id, material.fileName)}
                    >
                      <Download className="h-4 w-4 mr-2" />
                      Download
                    </Button>
                    <Button 
                      variant="outline" 
                      size="sm"
                      onClick={() => handleDelete(material.id)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Upload Dialog */}
      <Dialog open={showUploadDialog} onOpenChange={setShowUploadDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Upload Material</DialogTitle>
            <DialogDescription>
              Upload a new material for this class
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="title">Title *</Label>
              <Input
                id="title"
                value={uploadForm.title}
                onChange={(e) => setUploadForm({ ...uploadForm, title: e.target.value })}
                placeholder="e.g., Lecture Notes - Week 1"
              />
            </div>
            <div>
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                value={uploadForm.description}
                onChange={(e) => setUploadForm({ ...uploadForm, description: e.target.value })}
                placeholder="Optional description"
                rows={3}
              />
            </div>
            <div>
              <Label htmlFor="file">File *</Label>
              <Input
                id="file"
                type="file"
                onChange={(e) => setUploadForm({ ...uploadForm, file: e.target.files?.[0] || null })}
              />
              {uploadForm.file && (
                <p className="text-sm text-muted-foreground mt-2">
                  Selected: {uploadForm.file.name} ({formatFileSize(uploadForm.file.size)})
                </p>
              )}
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowUploadDialog(false)}>
              Cancel
            </Button>
            <Button onClick={handleUpload} disabled={uploading}>
              {uploading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Uploading...
                </>
              ) : (
                <>
                  <Upload className="mr-2 h-4 w-4" />
                  Upload
                </>
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

