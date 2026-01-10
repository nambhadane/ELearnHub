import { useState, useEffect, useRef, FormEvent, ChangeEvent } from "react";
import { useLocation } from "react-router-dom";
import { Send, Search, Paperclip, MoreVertical, Plus, Users, Loader2, MessageSquare, X, File, Image as ImageIcon } from "lucide-react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { DiscussionForum } from "@/components/DiscussionForum";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { useToast } from "@/hooks/use-toast";
import {
  getConversations,
  getMessages,
  sendMessage,
  createDirectConversation,
  getClassConversation,
  ConversationDTO,
  MessageDTO,
  ParticipantDTO,
  getClassStudents,
  getAllTeachers,
  getClassesByTeacher,
  getTeacherProfile,
  getStudentProfile,
  getMyClasses,
  ClassDTO,
  downloadMessageFile,
  getAuthenticatedImageUrl,
} from "@/services/api";

// Helper function to format time
const formatTime = (dateString: string) => {
  if (!dateString) return "";
  try {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return "Just now";
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays === 0) return "Today";
    if (diffDays === 1) return "Yesterday";
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString();
  } catch {
    return "";
  }
};

// Helper function to format message time
const formatMessageTime = (dateString: string) => {
  if (!dateString) return "";
  try {
    const date = new Date(dateString);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  } catch {
    return "";
  }
};

// Component for authenticated image loading
function AuthenticatedImage({ filename, alt, onDownload }: { filename: string; alt: string; onDownload: () => void }) {
  const [imageUrl, setImageUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    let mounted = true;
    let objectUrl: string | null = null;

    const loadImage = async () => {
      try {
        const url = await getAuthenticatedImageUrl(filename);
        if (mounted) {
          objectUrl = url;
          setImageUrl(url);
          setLoading(false);
        }
      } catch (err) {
        if (mounted) {
          setError(true);
          setLoading(false);
        }
      }
    };

    loadImage();

    return () => {
      mounted = false;
      if (objectUrl) {
        window.URL.revokeObjectURL(objectUrl);
      }
    };
  }, [filename]);

  if (loading) {
    return (
      <div className="flex items-center justify-center p-4 bg-background/50 rounded">
        <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (error || !imageUrl) {
    return (
      <button
        onClick={onDownload}
        className="flex items-center gap-2 p-2 rounded bg-background/50 hover:bg-background/70 transition-colors w-full text-left"
      >
        <ImageIcon className="h-4 w-4" />
        <span className="text-xs truncate flex-1">{alt}</span>
      </button>
    );
  }

  return (
    <img
      src={imageUrl}
      alt={alt}
      className="max-w-full rounded cursor-pointer hover:opacity-90 transition-opacity"
      style={{ maxHeight: '300px', objectFit: 'contain' }}
      onClick={onDownload}
    />
  );
}

export default function Messages() {
  const location = useLocation();
  const { toast } = useToast();
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const messagesContainerRef = useRef<HTMLDivElement>(null);

  // Determine role from URL path
  const isTeacher = location.pathname.includes("/teacher");
  const isStudent = location.pathname.includes("/student");

  // State
  const [conversations, setConversations] = useState<ConversationDTO[]>([]);
  const [selectedConversation, setSelectedConversation] = useState<ConversationDTO | null>(null);
  const [messages, setMessages] = useState<MessageDTO[]>([]);
  const [messageText, setMessageText] = useState("");
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [fileInputRef, setFileInputRef] = useState<HTMLInputElement | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [sending, setSending] = useState(false);

  // Dialog states (for teachers)
  const [showNewChatDialog, setShowNewChatDialog] = useState(false);
  const [availableStudents, setAvailableStudents] = useState<ParticipantDTO[]>([]);
  const [availableTeachers, setAvailableTeachers] = useState<ParticipantDTO[]>([]);
  const [teacherClasses, setTeacherClasses] = useState<ClassDTO[]>([]);
  const [selectedClassForStudents, setSelectedClassForStudents] = useState<string>("");
  const [loadingStudents, setLoadingStudents] = useState(false);
  const [newChatType, setNewChatType] = useState<"student" | "teacher" | "class">("student");
  const [studentClasses, setStudentClasses] = useState<ClassDTO[]>([]);
  const [selectedClassForDiscussion, setSelectedClassForDiscussion] = useState<number | null>(null);

  // Get current user ID from profile
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);

  // Fetch current user ID
  useEffect(() => {
    const fetchCurrentUser = async () => {
      try {
        if (isTeacher) {
          const profile = await getTeacherProfile();
          setCurrentUserId(profile.id);
        } else {
          const profile = await getStudentProfile();
          setCurrentUserId(profile.id);
        }
      } catch (err) {
        console.error("Failed to fetch current user:", err);
      }
    };
    fetchCurrentUser();
  }, [isTeacher]);

  // Fetch conversations
  useEffect(() => {
    const fetchConversations = async () => {
      try {
        setLoading(true);
        const convs = await getConversations();
        setConversations(convs);
        if (convs.length > 0 && !selectedConversation) {
          setSelectedConversation(convs[0]);
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : "Failed to load conversations";
        toast({
          title: "Error",
          description: message,
          variant: "destructive",
        });
      } finally {
        setLoading(false);
      }
    };

    fetchConversations();
  }, [toast]);

  // Fetch student classes for discussions
  useEffect(() => {
    if (isTeacher) return;

    const fetchStudentClasses = async () => {
      try {
        const classes = await getMyClasses();
        setStudentClasses(classes);
      } catch (error) {
        console.error("Failed to load student classes", error);
      }
    };

    fetchStudentClasses();
  }, [isTeacher]);

  // Fetch messages when conversation is selected
  useEffect(() => {
    if (!selectedConversation) return;

    const fetchMessages = async () => {
      try {
        setLoadingMessages(true);
        const msgs = await getMessages(selectedConversation.id);
        setMessages(msgs.reverse()); // Reverse to show oldest first
      } catch (err) {
        const message = err instanceof Error ? err.message : "Failed to load messages";
        toast({
          title: "Error",
          description: message,
          variant: "destructive",
        });
      } finally {
        setLoadingMessages(false);
      }
    };

    fetchMessages();
  }, [selectedConversation, toast]);

  // Scroll to bottom when messages change
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages]);

  // Fetch students/teachers for new chat dialog (teachers only)
  useEffect(() => {
    if (!showNewChatDialog || !isTeacher) return;

    const fetchData = async () => {
      if (newChatType === "student" && selectedClassForStudents) {
        try {
          setLoadingStudents(true);
          const students = await getClassStudents(parseInt(selectedClassForStudents));
          setAvailableStudents(students);
        } catch (err) {
          toast({
            title: "Error",
            description: "Failed to load students",
            variant: "destructive",
          });
        } finally {
          setLoadingStudents(false);
        }
      } else if (newChatType === "teacher") {
        try {
          setLoadingStudents(true);
          const teachers = await getAllTeachers();
          setAvailableTeachers(teachers);
        } catch (err) {
          toast({
            title: "Error",
            description: "Failed to load teachers",
            variant: "destructive",
          });
        } finally {
          setLoadingStudents(false);
        }
      }
    };

    fetchData();
  }, [showNewChatDialog, newChatType, selectedClassForStudents, isTeacher, toast]);

  // Fetch teacher classes for student selection
  useEffect(() => {
    if (!isTeacher || !showNewChatDialog) return;

    const fetchClasses = async () => {
      try {
        const profile = await getTeacherProfile();
        const classes = await getClassesByTeacher(profile.id);
        setTeacherClasses(classes);
      } catch (err) {
        console.error("Failed to load classes:", err);
      }
    };

    fetchClasses();
  }, [isTeacher, showNewChatDialog]);

  const handleFileSelect = (e: ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    const maxSize = 10 * 1024 * 1024; // 10MB limit
    
    const validFiles: File[] = [];
    const invalidFiles: string[] = [];
    
    files.forEach(file => {
      if (file.size > maxSize) {
        invalidFiles.push(`${file.name} (${(file.size / 1024 / 1024).toFixed(2)}MB)`);
      } else {
        validFiles.push(file);
      }
    });
    
    if (invalidFiles.length > 0) {
      toast({
        title: "File Size Limit Exceeded",
        description: `The following files exceed 10MB limit: ${invalidFiles.join(", ")}`,
        variant: "destructive",
      });
    }
    
    if (validFiles.length > 0) {
      setSelectedFiles((prev) => [...prev, ...validFiles]);
    }
    
    // Reset input to allow selecting same file again
    if (e.target) {
      e.target.value = "";
    }
  };

  const removeFile = (index: number) => {
    setSelectedFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSendMessage = async (e: FormEvent) => {
    e.preventDefault();
    if ((!messageText.trim() && selectedFiles.length === 0) || !selectedConversation || sending) return;

    const content = messageText.trim();
    const filesToSend = [...selectedFiles];
    setMessageText("");
    setSelectedFiles([]);

    try {
      setSending(true);

      const newMessage = await sendMessage({
        conversationId: selectedConversation.id,
        content: content || (filesToSend.length > 0 ? "📎 Sent files" : ""),
        files: filesToSend,
      });

      // Add message to local state
      setMessages((prev) => [...prev, newMessage]);

      // Update conversation's last message
      setConversations((prev) =>
        prev.map((conv) =>
          conv.id === selectedConversation.id
            ? { ...conv, lastMessage: newMessage, updatedAt: newMessage.createdAt }
            : conv
        )
      );

      // Scroll to bottom
      setTimeout(() => {
        if (messagesEndRef.current) {
          messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
        }
      }, 100);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to send message";
      toast({
        title: "Error",
        description: message,
        variant: "destructive",
      });
      setMessageText(content); // Restore message text on error
      setSelectedFiles(filesToSend); // Restore files on error
    } finally {
      setSending(false);
    }
  };

  const handleStartDirectChat = async (participantId: number) => {
    try {
      const conversation = await createDirectConversation(participantId);
      setConversations((prev) => [conversation, ...prev]);
      setSelectedConversation(conversation);
      setShowNewChatDialog(false);
      toast({
        title: "Chat Started",
        description: "You can now start messaging",
      });
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to start chat";
      toast({
        title: "Error",
        description: message,
        variant: "destructive",
      });
    }
  };

  const handleSelectClassGroup = async (classId: number) => {
    try {
      const conversation = await getClassConversation(classId);
      setConversations((prev) => {
        const exists = prev.find((c) => c.id === conversation.id);
        if (exists) return prev;
        return [conversation, ...prev];
      });
      setSelectedConversation(conversation);
      setShowNewChatDialog(false);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to load class chat";
      toast({
        title: "Error",
        description: message,
        variant: "destructive",
      });
    }
  };

  const filteredConversations = conversations.filter((conv) => {
    const searchLower = searchQuery.toLowerCase();
    return (
      conv.name?.toLowerCase().includes(searchLower) ||
      conv.lastMessage?.content.toLowerCase().includes(searchLower) ||
      conv.participants?.some((p) =>
        (p.name || p.username).toLowerCase().includes(searchLower)
      )
    );
  });

  // Get conversation display name
  const getConversationName = (conv: ConversationDTO) => {
    if (conv.name) return conv.name;
    if (conv.type === "DIRECT" && conv.participants) {
      const otherParticipant = conv.participants.find((p) => p.id !== currentUserId);
      return otherParticipant?.name || otherParticipant?.username || "Unknown";
    }
    return "Unnamed Conversation";
  };

  // Get conversation display role/type
  const getConversationType = (conv: ConversationDTO) => {
    if (conv.type === "GROUP") {
      if (conv.classId) return "Class Group";
      return "Group Chat";
    }
    if (conv.participants && conv.participants.length > 0) {
      const otherParticipant = conv.participants.find((p) => p.id !== currentUserId);
      return otherParticipant?.role || "User";
    }
    return "Direct Chat";
  };

  // Get other participant (for direct chats)
  const getOtherParticipant = (conv: ConversationDTO): ParticipantDTO | null => {
    if (conv.type === "DIRECT" && conv.participants) {
      return conv.participants.find((p) => p.id !== currentUserId) || null;
    }
    return null;
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
        <div className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">Messages & Discussions</h1>
          <p className="text-muted-foreground">
            {isTeacher
              ? "Chat with students and participate in class discussions"
              : "Chat with instructors and participate in class discussions"}
          </p>
        </div>
      </div>

      <Tabs defaultValue="messages" className="w-full">
        <TabsList>
          <TabsTrigger value="messages">Messages</TabsTrigger>
          <TabsTrigger value="discussions">Discussions</TabsTrigger>
        </TabsList>

        <TabsContent value="messages" className="space-y-4">
          {isTeacher && (
            <div className="flex justify-end">
              <Button onClick={() => setShowNewChatDialog(true)}>
                <Plus className="mr-2 h-4 w-4" />
                New Chat
              </Button>
            </div>
          )}

          <div className="grid gap-4 lg:grid-cols-3 h-[600px]">
        {/* Conversations List */}
        <Card className="glass-card lg:col-span-1 flex flex-col">
          <div className="p-4 border-b border-border">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="Search conversations..."
                className="pl-9"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>

          <ScrollArea className="flex-1">
            <div className="p-2 space-y-1">
              {filteredConversations.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-12 text-center">
                  <MessageSquare className="h-12 w-12 text-muted-foreground mb-4" />
                  <p className="text-sm text-muted-foreground">
                    {searchQuery ? "No conversations found" : "No conversations yet"}
                  </p>
                  {isTeacher && !searchQuery && (
                    <Button
                      variant="outline"
                      size="sm"
                      className="mt-4"
                      onClick={() => setShowNewChatDialog(true)}
                    >
                      <Plus className="mr-2 h-4 w-4" />
                      Start a Chat
                    </Button>
                  )}
                </div>
              ) : (
                filteredConversations.map((conversation) => {
                  const otherParticipant = getOtherParticipant(conversation);
                  const displayName = getConversationName(conversation);
                  const displayType = getConversationType(conversation);
                  const initials = displayName
                    .split(" ")
                    .map((n) => n[0])
                    .join("")
                    .toUpperCase()
                    .slice(0, 2);

                  return (
                    <button
                      key={conversation.id}
                      onClick={() => setSelectedConversation(conversation)}
                      className={`w-full flex items-start gap-3 rounded-lg p-3 text-left transition-all hover:bg-muted ${
                        selectedConversation?.id === conversation.id
                          ? "bg-primary/10 border border-primary"
                          : ""
                      }`}
                    >
                      <Avatar>
                        <AvatarImage src={otherParticipant?.avatar} />
                        <AvatarFallback>{initials}</AvatarFallback>
                      </Avatar>

                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between gap-2">
                          <h4 className="text-sm font-semibold truncate">{displayName}</h4>
                          {conversation.lastMessage && (
                            <span className="text-xs text-muted-foreground whitespace-nowrap">
                              {formatTime(conversation.lastMessage.createdAt)}
                            </span>
                          )}
                        </div>
                        <p className="text-xs text-muted-foreground">{displayType}</p>
                        {conversation.lastMessage && (
                          <p className="text-sm text-muted-foreground truncate mt-1">
                            {conversation.lastMessage.content}
                          </p>
                        )}
                      </div>

                      {conversation.unreadCount && conversation.unreadCount > 0 && (
                        <Badge className="bg-primary text-primary-foreground">
                          {conversation.unreadCount}
                        </Badge>
                      )}
                    </button>
                  );
                })
              )}
            </div>
          </ScrollArea>
        </Card>

        {/* Chat Window */}
        <Card className="glass-card lg:col-span-2 flex flex-col">
          {selectedConversation ? (
            <>
              {/* Chat Header */}
              <div className="p-4 border-b border-border flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <Avatar>
                    <AvatarImage
                      src={
                        getOtherParticipant(selectedConversation)?.avatar ||
                        "/placeholder.svg"
                      }
                    />
                    <AvatarFallback>
                      {getConversationName(selectedConversation)
                        .split(" ")
                        .map((n) => n[0])
                        .join("")
                        .toUpperCase()
                        .slice(0, 2)}
                    </AvatarFallback>
                  </Avatar>
                  <div>
                    <h3 className="font-semibold">
                      {getConversationName(selectedConversation)}
                    </h3>
                    <p className="text-xs text-muted-foreground">
                      {getConversationType(selectedConversation)}
                    </p>
                  </div>
                </div>
                <Button variant="ghost" size="icon">
                  <MoreVertical className="h-5 w-5" />
                </Button>
              </div>

              {/* Messages */}
              <div className="flex-1 overflow-y-auto p-4 space-y-4" ref={messagesContainerRef}>
                {loadingMessages ? (
                  <div className="flex items-center justify-center py-8">
                    <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
                  </div>
                ) : messages.length === 0 ? (
                  <div className="flex flex-col items-center justify-center py-12 text-center">
                    <MessageSquare className="h-12 w-12 text-muted-foreground mb-4" />
                    <p className="text-sm text-muted-foreground">
                      No messages yet. Start the conversation!
                    </p>
                  </div>
                ) : (
                  messages.map((message) => {
                      const isSelf = message.senderId === currentUserId;
                      return (
                        <div
                          key={message.id}
                          className={`flex ${isSelf ? "justify-end" : "justify-start"}`}
                        >
                          <div
                            className={`max-w-[70%] rounded-lg px-4 py-2 ${
                              isSelf
                                ? "bg-primary text-primary-foreground"
                                : "bg-muted"
                            }`}
                          >
                            {!isSelf && (
                              <p className="text-xs font-semibold mb-1 text-primary">
                                {message.senderName}
                              </p>
                            )}
                            {/* File Attachments */}
                            {message.attachments && message.attachments.length > 0 && (
                              <div className="space-y-2 mb-2">
                                {message.attachments.map((file, idx) => {
                                  // Extract filename from URL (e.g., "/api/messages/files/filename.pdf" -> "filename.pdf")
                                  const filename = file.url.split('/').pop() || file.name;
                                  const isImage = file.type?.startsWith("image/");
                                  
                                  return (
                                    <div key={idx}>
                                      {isImage ? (
                                        <AuthenticatedImage
                                          filename={filename}
                                          alt={file.name}
                                          onDownload={async () => {
                                            try {
                                              await downloadMessageFile(filename);
                                            } catch (err) {
                                              toast({
                                                title: "Error",
                                                description: "Failed to download file",
                                                variant: "destructive",
                                              });
                                            }
                                          }}
                                        />
                                      ) : (
                                        <button
                                          onClick={async () => {
                                            try {
                                              await downloadMessageFile(filename);
                                            } catch (err) {
                                              toast({
                                                title: "Error",
                                                description: "Failed to download file",
                                                variant: "destructive",
                                              });
                                            }
                                          }}
                                          className="flex items-center gap-2 p-2 rounded bg-background/50 hover:bg-background/70 transition-colors w-full text-left"
                                        >
                                          <File className="h-4 w-4" />
                                          <span className="text-xs truncate flex-1">{file.name}</span>
                                        </button>
                                      )}
                                    </div>
                                  );
                                })}
                              </div>
                            )}
                            {message.content && (
                              <p className="text-sm whitespace-pre-wrap">{message.content}</p>
                            )}
                            <p
                              className={`text-xs mt-1 ${
                                isSelf
                                  ? "text-primary-foreground/70"
                                  : "text-muted-foreground"
                              }`}
                            >
                              {formatMessageTime(message.createdAt)}
                            </p>
                          </div>
                        </div>
                      );
                    })
                  )}
                <div ref={messagesEndRef} />
              </div>

              {/* Message Input */}
              <div className="p-4 border-t border-border">
                {/* Selected Files Preview */}
                {selectedFiles.length > 0 && (
                  <div className="mb-2 flex flex-wrap gap-2">
                    {selectedFiles.map((file, index) => {
                      const sizeInKB = (file.size / 1024).toFixed(1);
                      const sizeInMB = (file.size / 1024 / 1024).toFixed(2);
                      const displaySize = file.size > 1024 * 1024 ? `${sizeInMB}MB` : `${sizeInKB}KB`;
                      
                      return (
                        <div
                          key={index}
                          className="flex items-center gap-2 px-3 py-2 bg-muted rounded-lg text-xs border border-border"
                        >
                          {file.type.startsWith('image/') ? (
                            <ImageIcon className="h-4 w-4 text-primary" />
                          ) : (
                            <File className="h-4 w-4 text-primary" />
                          )}
                          <div className="flex flex-col">
                            <span className="max-w-[150px] truncate font-medium">{file.name}</span>
                            <span className="text-muted-foreground">{displaySize}</span>
                          </div>
                          <button
                            type="button"
                            onClick={() => removeFile(index)}
                            className="hover:text-destructive transition-colors ml-2"
                          >
                            <X className="h-4 w-4" />
                          </button>
                        </div>
                      );
                    })}
                  </div>
                )}
                <form onSubmit={handleSendMessage}>
                  <div className="flex items-center gap-2">
                    <input
                      type="file"
                      ref={(el) => setFileInputRef(el)}
                      onChange={handleFileSelect}
                      multiple
                      className="hidden"
                      id="file-input"
                    />
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      onClick={() => fileInputRef?.click()}
                    >
                      <Paperclip className="h-5 w-5" />
                    </Button>
                    <Input
                      placeholder="Type a message..."
                      value={messageText}
                      onChange={(e) => setMessageText(e.target.value)}
                      className="flex-1"
                      disabled={sending}
                    />
                    <Button
                      type="submit"
                      size="icon"
                      disabled={(!messageText.trim() && selectedFiles.length === 0) || sending}
                    >
                      {sending ? (
                        <Loader2 className="h-5 w-5 animate-spin" />
                      ) : (
                        <Send className="h-5 w-5" />
                      )}
                    </Button>
                  </div>
                </form>
              </div>
            </>
          ) : (
            <div className="flex flex-col items-center justify-center h-full text-center">
              <MessageSquare className="h-16 w-16 text-muted-foreground mb-4" />
              <h3 className="text-lg font-semibold mb-2">Select a conversation</h3>
              <p className="text-sm text-muted-foreground">
                Choose a conversation from the list to start messaging
              </p>
            </div>
          )}
        </Card>
      </div>
        </TabsContent>

        <TabsContent value="discussions" className="space-y-4">
          {/* Class Selector for Discussions */}
          <div className="flex items-center gap-4">
            <label className="text-sm font-medium">Select Class:</label>
            <select
              className="flex h-10 w-full max-w-xs rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background"
              value={selectedClassForDiscussion || ""}
              onChange={(e) => setSelectedClassForDiscussion(Number(e.target.value))}
            >
              <option value="">Choose a class...</option>
              {studentClasses.map((cls) => (
                <option key={cls.id} value={cls.id}>
                  {cls.name}
                </option>
              ))}
            </select>
          </div>

          {selectedClassForDiscussion ? (
            <DiscussionForum classId={selectedClassForDiscussion} userRole="STUDENT" />
          ) : (
            <Card>
              <CardContent className="py-12 text-center">
                <MessageSquare className="h-12 w-12 mx-auto mb-3 text-muted-foreground opacity-50" />
                <p className="text-muted-foreground">Select a class to view discussions</p>
              </CardContent>
            </Card>
          )}
        </TabsContent>
      </Tabs>

      {/* New Chat Dialog (Teachers Only) */}
      {isTeacher && (
        <Dialog open={showNewChatDialog} onOpenChange={setShowNewChatDialog}>
          <DialogContent className="sm:max-w-[500px]">
            <DialogHeader>
              <DialogTitle>Start New Chat</DialogTitle>
              <DialogDescription>
                Choose who you want to chat with
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-4 py-4">
              {/* Chat Type Selection */}
              <div className="flex gap-2">
                <Button
                  type="button"
                  variant={newChatType === "student" ? "default" : "outline"}
                  className="flex-1"
                  onClick={() => {
                    setNewChatType("student");
                    setSelectedClassForStudents("");
                    setAvailableStudents([]);
                  }}
                >
                  <Users className="mr-2 h-4 w-4" />
                  Student
                </Button>
                <Button
                  type="button"
                  variant={newChatType === "teacher" ? "default" : "outline"}
                  className="flex-1"
                  onClick={() => {
                    setNewChatType("teacher");
                    setAvailableTeachers([]);
                  }}
                >
                  <Users className="mr-2 h-4 w-4" />
                  Teacher
                </Button>
                <Button
                  type="button"
                  variant={newChatType === "class" ? "default" : "outline"}
                  className="flex-1"
                  onClick={() => {
                    setNewChatType("class");
                  }}
                >
                  <MessageSquare className="mr-2 h-4 w-4" />
                  Class Group
                </Button>
              </div>

              {/* Student Selection */}
              {newChatType === "student" && (
                <div className="space-y-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">Select Class</label>
                    <select
                      className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                      value={selectedClassForStudents}
                      onChange={(e) => {
                        setSelectedClassForStudents(e.target.value);
                        setAvailableStudents([]);
                      }}
                    >
                      <option value="">Choose a class...</option>
                      {teacherClasses.map((classItem) => (
                        <option key={classItem.id} value={classItem.id.toString()}>
                          {classItem.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  {loadingStudents ? (
                    <div className="flex items-center justify-center py-8">
                      <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
                    </div>
                  ) : availableStudents.length > 0 ? (
                    <ScrollArea className="h-[200px]">
                      <div className="space-y-2">
                        {availableStudents.map((student) => (
                          <button
                            key={student.id}
                            onClick={() => handleStartDirectChat(student.id)}
                            className="w-full flex items-center gap-3 rounded-lg p-3 text-left hover:bg-muted transition-colors"
                          >
                            <Avatar>
                              <AvatarImage src={student.avatar} />
                              <AvatarFallback>
                                {(student.name || student.username)
                                  .split(" ")
                                  .map((n) => n[0])
                                  .join("")
                                  .toUpperCase()
                                  .slice(0, 2)}
                              </AvatarFallback>
                            </Avatar>
                            <div>
                              <p className="font-medium">
                                {student.name || student.username}
                              </p>
                              <p className="text-xs text-muted-foreground">
                                {student.role || "Student"}
                              </p>
                            </div>
                          </button>
                        ))}
                      </div>
                    </ScrollArea>
                  ) : selectedClassForStudents ? (
                    <p className="text-sm text-muted-foreground text-center py-4">
                      No students in this class
                    </p>
                  ) : (
                    <p className="text-sm text-muted-foreground text-center py-4">
                      Select a class to see students
                    </p>
                  )}
                </div>
              )}

              {/* Teacher Selection */}
              {newChatType === "teacher" && (
                <div className="space-y-4">
                  {loadingStudents ? (
                    <div className="flex items-center justify-center py-8">
                      <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
                    </div>
                  ) : availableTeachers.length > 0 ? (
                    <ScrollArea className="h-[200px]">
                      <div className="space-y-2">
                        {availableTeachers.map((teacher) => (
                          <button
                            key={teacher.id}
                            onClick={() => handleStartDirectChat(teacher.id)}
                            className="w-full flex items-center gap-3 rounded-lg p-3 text-left hover:bg-muted transition-colors"
                          >
                            <Avatar>
                              <AvatarImage src={teacher.avatar} />
                              <AvatarFallback>
                                {(teacher.name || teacher.username)
                                  .split(" ")
                                  .map((n) => n[0])
                                  .join("")
                                  .toUpperCase()
                                  .slice(0, 2)}
                              </AvatarFallback>
                            </Avatar>
                            <div>
                              <p className="font-medium">
                                {teacher.name || teacher.username}
                              </p>
                              <p className="text-xs text-muted-foreground">
                                {teacher.role || "Teacher"}
                              </p>
                            </div>
                          </button>
                        ))}
                      </div>
                    </ScrollArea>
                  ) : (
                    <p className="text-sm text-muted-foreground text-center py-4">
                      No other teachers available
                    </p>
                  )}
                </div>
              )}

              {/* Class Group Selection */}
              {newChatType === "class" && (
                <div className="space-y-4">
                  {teacherClasses.length > 0 ? (
                    <ScrollArea className="h-[200px]">
                      <div className="space-y-2">
                        {teacherClasses.map((classItem) => (
                          <button
                            key={classItem.id}
                            onClick={() => handleSelectClassGroup(classItem.id)}
                            className="w-full flex items-center gap-3 rounded-lg p-3 text-left hover:bg-muted transition-colors"
                          >
                            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                              <MessageSquare className="h-5 w-5 text-primary" />
                            </div>
                            <div>
                              <p className="font-medium">{classItem.name}</p>
                              <p className="text-xs text-muted-foreground">Class Group</p>
                            </div>
                          </button>
                        ))}
                      </div>
                    </ScrollArea>
                  ) : (
                    <p className="text-sm text-muted-foreground text-center py-4">
                      No classes available
                    </p>
                  )}
                </div>
              )}
            </div>

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => setShowNewChatDialog(false)}
              >
                Cancel
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
}
