import { useState, useEffect } from "react";
import { MessageSquare, ThumbsUp, Pin, Lock, CheckCircle, Plus, Send, MoreVertical, Trash2, Edit, Award } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { useToast } from "@/hooks/use-toast";
import {
  DiscussionTopicDTO,
  DiscussionReplyDTO,
  getDiscussionTopicsByClass,
  createDiscussionTopic,
  createDiscussionReply,
  toggleTopicLike,
  toggleReplyLike,
  pinDiscussionTopic,
  lockDiscussionTopic,
  markTopicAsSolved,
  markReplyAsSolution,
  deleteDiscussionTopic,
  deleteDiscussionReply,
  getDiscussionReplies,
} from "@/services/api";

interface DiscussionForumProps {
  classId: number;
  userRole: "TEACHER" | "STUDENT";
}

export function DiscussionForum({ classId, userRole }: DiscussionForumProps) {
  const [topics, setTopics] = useState<DiscussionTopicDTO[]>([]);
  const [selectedTopic, setSelectedTopic] = useState<DiscussionTopicDTO | null>(null);
  const [replies, setReplies] = useState<DiscussionReplyDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [showTopicDialog, setShowTopicDialog] = useState(false);
  const [replyContent, setReplyContent] = useState("");
  const { toast } = useToast();

  const currentUser = JSON.parse(localStorage.getItem("user") || "{}");

  useEffect(() => {
    loadTopics();
  }, [classId]);

  const loadTopics = async () => {
    try {
      setLoading(true);
      const data = await getDiscussionTopicsByClass(classId, currentUser.id);
      setTopics(data);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load discussions",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const loadReplies = async (topicId: number) => {
    try {
      const data = await getDiscussionReplies(topicId, currentUser.id);
      setReplies(data);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load replies",
        variant: "destructive",
      });
    }
  };

  const handleCreateTopic = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);

    try {
      const topic: DiscussionTopicDTO = {
        classId,
        createdBy: currentUser.id,
        title: formData.get("title") as string,
        content: formData.get("content") as string,
        isPinned: false,
      };

      await createDiscussionTopic(topic);
      toast({
        title: "Success",
        description: "Discussion topic created",
      });
      setShowCreateDialog(false);
      loadTopics();
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to create topic",
        variant: "destructive",
      });
    }
  };

  const handleOpenTopic = async (topic: DiscussionTopicDTO) => {
    setSelectedTopic(topic);
    setShowTopicDialog(true);
    await loadReplies(topic.id!);
  };

  const handleCreateReply = async () => {
    if (!selectedTopic || !replyContent.trim()) return;

    try {
      const reply: DiscussionReplyDTO = {
        topicId: selectedTopic.id!,
        userId: currentUser.id,
        content: replyContent,
      };

      await createDiscussionReply(reply);
      setReplyContent("");
      await loadReplies(selectedTopic.id!);
      loadTopics(); // Refresh to update reply count
      toast({
        title: "Success",
        description: "Reply posted",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to post reply",
        variant: "destructive",
      });
    }
  };

  const handleToggleTopicLike = async (topicId: number) => {
    try {
      await toggleTopicLike(topicId, currentUser.id);
      loadTopics();
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to like topic",
        variant: "destructive",
      });
    }
  };

  const handleToggleReplyLike = async (replyId: number) => {
    try {
      await toggleReplyLike(replyId, currentUser.id);
      if (selectedTopic) {
        await loadReplies(selectedTopic.id!);
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to like reply",
        variant: "destructive",
      });
    }
  };

  const handlePinTopic = async (topicId: number, isPinned: boolean) => {
    try {
      await pinDiscussionTopic(topicId, !isPinned);
      loadTopics();
      toast({
        title: "Success",
        description: isPinned ? "Topic unpinned" : "Topic pinned",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to pin topic",
        variant: "destructive",
      });
    }
  };

  const handleLockTopic = async (topicId: number, isLocked: boolean) => {
    try {
      await lockDiscussionTopic(topicId, !isLocked);
      loadTopics();
      toast({
        title: "Success",
        description: isLocked ? "Topic unlocked" : "Topic locked",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to lock topic",
        variant: "destructive",
      });
    }
  };

  const handleMarkAsSolved = async (topicId: number, isSolved: boolean) => {
    try {
      await markTopicAsSolved(topicId, !isSolved);
      loadTopics();
      if (selectedTopic) {
        setSelectedTopic({ ...selectedTopic, isSolved: !isSolved });
      }
      toast({
        title: "Success",
        description: isSolved ? "Marked as unsolved" : "Marked as solved",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to update topic",
        variant: "destructive",
      });
    }
  };

  const handleMarkAsSolution = async (replyId: number, isSolution: boolean) => {
    try {
      await markReplyAsSolution(replyId, !isSolution);
      if (selectedTopic) {
        await loadReplies(selectedTopic.id!);
        setSelectedTopic({ ...selectedTopic, isSolved: !isSolution });
      }
      loadTopics();
      toast({
        title: "Success",
        description: isSolution ? "Unmarked as solution" : "Marked as solution",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to mark solution",
        variant: "destructive",
      });
    }
  };

  const handleDeleteTopic = async (topicId: number) => {
    if (!confirm("Are you sure you want to delete this topic?")) return;

    try {
      await deleteDiscussionTopic(topicId);
      loadTopics();
      setShowTopicDialog(false);
      toast({
        title: "Success",
        description: "Topic deleted",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete topic",
        variant: "destructive",
      });
    }
  };

  const handleDeleteReply = async (replyId: number) => {
    if (!confirm("Are you sure you want to delete this reply?")) return;

    try {
      await deleteDiscussionReply(replyId);
      if (selectedTopic) {
        await loadReplies(selectedTopic.id!);
      }
      loadTopics();
      toast({
        title: "Success",
        description: "Reply deleted",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete reply",
        variant: "destructive",
      });
    }
  };

  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return "Just now";
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString();
  };

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h3 className="text-lg font-semibold">Discussion Forum</h3>
          <p className="text-sm text-muted-foreground">
            {topics.length} topic{topics.length !== 1 ? "s" : ""}
          </p>
        </div>
        <Button onClick={() => setShowCreateDialog(true)}>
          <Plus className="h-4 w-4 mr-2" />
          New Topic
        </Button>
      </div>

      {/* Topics List */}
      {loading ? (
        <Card>
          <CardContent className="py-8 text-center">
            <p className="text-muted-foreground">Loading discussions...</p>
          </CardContent>
        </Card>
      ) : topics.length === 0 ? (
        <Card>
          <CardContent className="py-8 text-center">
            <MessageSquare className="h-12 w-12 mx-auto mb-3 text-muted-foreground opacity-50" />
            <p className="text-muted-foreground">No discussions yet</p>
            <p className="text-sm text-muted-foreground">Start a new topic to begin the conversation</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {topics.map((topic) => (
            <Card
              key={topic.id}
              className="cursor-pointer hover:bg-muted/50 transition-colors"
              onClick={() => handleOpenTopic(topic)}
            >
              <CardContent className="p-4">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <h4 className="font-medium">{topic.title}</h4>
                      {topic.isPinned && (
                        <Badge variant="secondary" className="text-xs">
                          <Pin className="h-3 w-3 mr-1" />
                          Pinned
                        </Badge>
                      )}
                      {topic.isLocked && (
                        <Badge variant="outline" className="text-xs">
                          <Lock className="h-3 w-3 mr-1" />
                          Locked
                        </Badge>
                      )}
                      {topic.isSolved && (
                        <Badge className="bg-green-500 text-xs">
                          <CheckCircle className="h-3 w-3 mr-1" />
                          Solved
                        </Badge>
                      )}
                    </div>
                    <p className="text-sm text-muted-foreground line-clamp-2 mb-2">
                      {topic.content}
                    </p>
                    <div className="flex items-center gap-4 text-xs text-muted-foreground">
                      <span className="flex items-center gap-1">
                        <Avatar className="h-5 w-5">
                          <AvatarFallback className="text-xs">
                            {topic.createdByName?.charAt(0) || "?"}
                          </AvatarFallback>
                        </Avatar>
                        {topic.createdByName}
                      </span>
                      <span>{formatTime(topic.createdAt!)}</span>
                      <span className="flex items-center gap-1">
                        <MessageSquare className="h-3 w-3" />
                        {topic.repliesCount || 0}
                      </span>
                      <span className="flex items-center gap-1">
                        <ThumbsUp className="h-3 w-3" />
                        {topic.likesCount || 0}
                      </span>
                    </div>
                  </div>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleToggleTopicLike(topic.id!);
                    }}
                    className={topic.isLikedByCurrentUser ? "text-primary" : ""}
                  >
                    <ThumbsUp className="h-4 w-4" />
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Create Topic Dialog */}
      <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create New Discussion Topic</DialogTitle>
            <DialogDescription>Start a new conversation with your class</DialogDescription>
          </DialogHeader>
          <form onSubmit={handleCreateTopic} className="space-y-4">
            <div>
              <label className="text-sm font-medium">Title</label>
              <Input name="title" placeholder="Enter topic title" required />
            </div>
            <div>
              <label className="text-sm font-medium">Content</label>
              <Textarea
                name="content"
                placeholder="Describe your topic or question..."
                rows={5}
                required
              />
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setShowCreateDialog(false)}>
                Cancel
              </Button>
              <Button type="submit">Create Topic</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Topic Detail Dialog */}
      <Dialog open={showTopicDialog} onOpenChange={setShowTopicDialog}>
        <DialogContent className="max-w-3xl max-h-[80vh] overflow-y-auto">
          {selectedTopic && (
            <>
              <DialogHeader>
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <DialogTitle className="text-xl">{selectedTopic.title}</DialogTitle>
                    <div className="flex items-center gap-2 mt-2">
                      {selectedTopic.isPinned && (
                        <Badge variant="secondary" className="text-xs">
                          <Pin className="h-3 w-3 mr-1" />
                          Pinned
                        </Badge>
                      )}
                      {selectedTopic.isLocked && (
                        <Badge variant="outline" className="text-xs">
                          <Lock className="h-3 w-3 mr-1" />
                          Locked
                        </Badge>
                      )}
                      {selectedTopic.isSolved && (
                        <Badge className="bg-green-500 text-xs">
                          <CheckCircle className="h-3 w-3 mr-1" />
                          Solved
                        </Badge>
                      )}
                    </div>
                  </div>
                  {(userRole === "TEACHER" || selectedTopic.createdBy === currentUser.id) && (
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="sm">
                          <MoreVertical className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        {userRole === "TEACHER" && (
                          <>
                            <DropdownMenuItem onClick={() => handlePinTopic(selectedTopic.id!, selectedTopic.isPinned!)}>
                              <Pin className="h-4 w-4 mr-2" />
                              {selectedTopic.isPinned ? "Unpin" : "Pin"} Topic
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => handleLockTopic(selectedTopic.id!, selectedTopic.isLocked!)}>
                              <Lock className="h-4 w-4 mr-2" />
                              {selectedTopic.isLocked ? "Unlock" : "Lock"} Topic
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => handleMarkAsSolved(selectedTopic.id!, selectedTopic.isSolved!)}>
                              <CheckCircle className="h-4 w-4 mr-2" />
                              Mark as {selectedTopic.isSolved ? "Unsolved" : "Solved"}
                            </DropdownMenuItem>
                          </>
                        )}
                        {(userRole === "TEACHER" || selectedTopic.createdBy === currentUser.id) && (
                          <DropdownMenuItem
                            onClick={() => handleDeleteTopic(selectedTopic.id!)}
                            className="text-red-600"
                          >
                            <Trash2 className="h-4 w-4 mr-2" />
                            Delete Topic
                          </DropdownMenuItem>
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
                  )}
                </div>
              </DialogHeader>

              {/* Topic Content */}
              <div className="space-y-4">
                <Card>
                  <CardContent className="p-4">
                    <div className="flex items-start gap-3 mb-3">
                      <Avatar>
                        <AvatarFallback>
                          {selectedTopic.createdByName?.charAt(0) || "?"}
                        </AvatarFallback>
                      </Avatar>
                      <div className="flex-1">
                        <div className="flex items-center gap-2">
                          <span className="font-medium">{selectedTopic.createdByName}</span>
                          <Badge variant="outline" className="text-xs">
                            {selectedTopic.createdByRole}
                          </Badge>
                          <span className="text-xs text-muted-foreground">
                            {formatTime(selectedTopic.createdAt!)}
                          </span>
                        </div>
                      </div>
                    </div>
                    <p className="text-sm whitespace-pre-wrap">{selectedTopic.content}</p>
                    <div className="flex items-center gap-4 mt-3 pt-3 border-t">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleToggleTopicLike(selectedTopic.id!)}
                        className={selectedTopic.isLikedByCurrentUser ? "text-primary" : ""}
                      >
                        <ThumbsUp className="h-4 w-4 mr-1" />
                        {selectedTopic.likesCount || 0}
                      </Button>
                    </div>
                  </CardContent>
                </Card>

                {/* Replies */}
                <div className="space-y-3">
                  <h4 className="font-medium text-sm">
                    {replies.length} {replies.length === 1 ? "Reply" : "Replies"}
                  </h4>
                  {replies.map((reply) => (
                    <Card key={reply.id}>
                      <CardContent className="p-4">
                        <div className="flex items-start gap-3">
                          <Avatar className="h-8 w-8">
                            <AvatarFallback className="text-xs">
                              {reply.userName?.charAt(0) || "?"}
                            </AvatarFallback>
                          </Avatar>
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-1">
                              <span className="font-medium text-sm">{reply.userName}</span>
                              <Badge variant="outline" className="text-xs">
                                {reply.userRole}
                              </Badge>
                              {reply.isSolution && (
                                <Badge className="bg-green-500 text-xs">
                                  <Award className="h-3 w-3 mr-1" />
                                  Solution
                                </Badge>
                              )}
                              <span className="text-xs text-muted-foreground">
                                {formatTime(reply.createdAt!)}
                              </span>
                            </div>
                            <p className="text-sm whitespace-pre-wrap">{reply.content}</p>
                            <div className="flex items-center gap-2 mt-2">
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleToggleReplyLike(reply.id!)}
                                className={reply.isLikedByCurrentUser ? "text-primary" : ""}
                              >
                                <ThumbsUp className="h-3 w-3 mr-1" />
                                {reply.likesCount || 0}
                              </Button>
                              {userRole === "TEACHER" && !reply.isSolution && (
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  onClick={() => handleMarkAsSolution(reply.id!, reply.isSolution!)}
                                >
                                  <Award className="h-3 w-3 mr-1" />
                                  Mark as Solution
                                </Button>
                              )}
                              {(userRole === "TEACHER" || reply.userId === currentUser.id) && (
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  onClick={() => handleDeleteReply(reply.id!)}
                                  className="text-red-600"
                                >
                                  <Trash2 className="h-3 w-3" />
                                </Button>
                              )}
                            </div>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>

                {/* Reply Input */}
                {!selectedTopic.isLocked && (
                  <div className="flex gap-2">
                    <Textarea
                      placeholder="Write a reply..."
                      value={replyContent}
                      onChange={(e) => setReplyContent(e.target.value)}
                      rows={3}
                      className="flex-1"
                    />
                    <Button onClick={handleCreateReply} disabled={!replyContent.trim()}>
                      <Send className="h-4 w-4" />
                    </Button>
                  </div>
                )}
              </div>
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
