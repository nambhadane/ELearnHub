// ============================================
// FIXED createClassConversation Method
// Replace this method in your MessageServiceImpl
// ============================================

@Override
public ConversationDTO createClassConversation(Long classId) {
    // Check if conversation already exists
    Optional<Conversation> existing = conversationRepository.findByClassId(classId);
    if (existing.isPresent()) {
        return convertConversationToDTO(existing.get(), null);
    }
    
    // ✅ FIX: Unwrap Optional first
    Optional<ClassEntity> classOpt = classService.getClassById(classId);
    if (classOpt.isEmpty()) {
        throw new RuntimeException("Class not found");
    }
    ClassEntity classEntity = classOpt.get();  // ✅ Get the actual entity from Optional
    
    // Create conversation
    Conversation conversation = new Conversation();
    conversation.setType(Conversation.ConversationType.GROUP);
    conversation.setName(classEntity.getName());  // ✅ Now we can call getName() on ClassEntity
    conversation.setClassId(classId);
    conversation.setCreatedAt(LocalDateTime.now());
    conversation.setUpdatedAt(LocalDateTime.now());
    
    conversation = conversationRepository.save(conversation);
    
    // Get all students in the class
    List<ParticipantDTO> students = classService.getClassStudents(classId);
    
    // Add teacher
    Optional<User> teacherOpt = userService.getUserById(classEntity.getTeacherId());  // ✅ Now we can call getTeacherId() on ClassEntity
    if (teacherOpt.isEmpty()) {
        throw new RuntimeException("Teacher not found");
    }
    User teacher = teacherOpt.get();
    
    List<User> participantsList = new ArrayList<>();
    participantsList.add(teacher);
    
    // Add students
    for (ParticipantDTO student : students) {
        Optional<User> studentUserOpt = userService.getUserById(student.getId());
        if (studentUserOpt.isPresent()) {
            participantsList.add(studentUserOpt.get());
        }
    }
    
    conversation.setParticipants(participantsList);
    conversation = conversationRepository.save(conversation);
    
    // Create participant records
    for (User participant : participantsList) {
        ConversationParticipant cp = new ConversationParticipant();
        cp.setConversation(conversation);
        cp.setUser(participant);
        cp.setUnreadCount(0);
        participantRepository.save(cp);
    }
    
    return convertConversationToDTO(conversation, null);
}

// ============================================
// If ClassEntity doesn't have getTeacherId() or getName() methods,
// you may need to add getters/setters to ClassEntity:
// ============================================

/*
If you get errors like "getTeacherId() is undefined", add these to ClassEntity:

public class ClassEntity {
    // ... existing fields ...
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Long getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
}
*/

