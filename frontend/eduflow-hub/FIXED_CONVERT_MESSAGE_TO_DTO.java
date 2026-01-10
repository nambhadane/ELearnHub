// ============================================
// FIXED convertMessageToDTO - Convert filePaths to attachments
// ============================================

// Replace your existing convertMessageToDTO method in MessageServiceImpl with this:

private MessageDTO convertMessageToDTO(Message message) {
    MessageDTO dto = new MessageDTO();
    dto.setId(message.getId());
    
    if (message.getConversation() != null) {
        dto.setConversationId(message.getConversation().getId());
    }
    
    if (message.getSender() != null) {
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getName() != null ? 
            message.getSender().getName() : message.getSender().getUsername());
    }
    
    dto.setContent(message.getContent());
    dto.setCreatedAt(message.getCreatedAt());
    dto.setIsRead(message.getReadAt() != null);
    
    // ✅ FIX: Convert filePaths (String) to attachments (List<MessageAttachmentDTO>)
    if (message.getFilePaths() != null && !message.getFilePaths().trim().isEmpty()) {
        List<MessageAttachmentDTO> attachments = new ArrayList<>();
        String[] paths = message.getFilePaths().split(",");
        
        for (String path : paths) {
            String trimmedPath = path.trim();
            if (!trimmedPath.isEmpty()) {
                MessageAttachmentDTO attachment = new MessageAttachmentDTO();
                
                // Extract filename from path (e.g., "uploads/messages/1234567890_file.pdf" -> "1234567890_file.pdf")
                String filename = trimmedPath;
                if (trimmedPath.contains("/")) {
                    filename = trimmedPath.substring(trimmedPath.lastIndexOf("/") + 1);
                }
                
                attachment.setName(filename);
                // Set URL for downloading (adjust path as needed)
                attachment.setUrl("/api/messages/files/" + filename);
                
                // Optionally detect file type from extension
                if (filename.contains(".")) {
                    String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
                    attachment.setType(getMimeType(extension));
                }
                
                attachments.add(attachment);
            }
        }
        
        dto.setAttachments(attachments);
    }
    
    return dto;
}

// Helper method to get MIME type from file extension
private String getMimeType(String extension) {
    switch (extension.toLowerCase()) {
        case "pdf": return "application/pdf";
        case "doc": case "docx": return "application/msword";
        case "xls": case "xlsx": return "application/vnd.ms-excel";
        case "jpg": case "jpeg": return "image/jpeg";
        case "png": return "image/png";
        case "gif": return "image/gif";
        case "txt": return "text/plain";
        case "zip": return "application/zip";
        default: return "application/octet-stream";
    }
}

