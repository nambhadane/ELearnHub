// ============================================
// FIXED MessageServiceImpl - Remove setParticipants() calls
// Use only ConversationParticipant entities
// ============================================

// In createDirectConversation method, replace lines 172-191 with:

        User user1 = user1Opt.get();
        User user2 = user2Opt.get();
        
        // ✅ FIX: Don't use setParticipants() - save conversation first
        conversation = conversationRepository.save(conversation);
        
        // ✅ FIX: Create participant records using ConversationParticipant entity
        ConversationParticipant cp1 = new ConversationParticipant();
        cp1.setConversation(conversation);
        cp1.setUser(user1);
        cp1.setUnreadCount(0);
        cp1.setLastReadAt(null);
        participantRepository.saveAndFlush(cp1); // Use saveAndFlush to ensure ID is generated
        
        ConversationParticipant cp2 = new ConversationParticipant();
        cp2.setConversation(conversation);
        cp2.setUser(user2);
        cp2.setUnreadCount(0);
        cp2.setLastReadAt(null);
        participantRepository.saveAndFlush(cp2); // Use saveAndFlush to ensure ID is generated
        
        return convertConversationToDTO(conversation, userId1);

// In createClassConversation method, replace lines 228-260 with:

        conversation = conversationRepository.save(conversation);
        
        // Get all students in the class
        List<ParticipantDTO> students = classService.getClassStudents(classId);
        
        // Add teacher
        Optional<User> teacherOpt = userService.getUserById(classEntity.getTeacherId());
        if (teacherOpt.isEmpty()) {
            throw new RuntimeException("Teacher not found");
        }
        User teacher = teacherOpt.get();
        
        // ✅ FIX: Create participant record for teacher
        ConversationParticipant teacherCp = new ConversationParticipant();
        teacherCp.setConversation(conversation);
        teacherCp.setUser(teacher);
        teacherCp.setUnreadCount(0);
        teacherCp.setLastReadAt(null);
        participantRepository.saveAndFlush(teacherCp);
        
        // ✅ FIX: Add students using ConversationParticipant entities
        for (ParticipantDTO student : students) {
            Optional<User> studentOpt = userService.getUserById(student.getId());
            if (studentOpt.isPresent()) {
                ConversationParticipant studentCp = new ConversationParticipant();
                studentCp.setConversation(conversation);
                studentCp.setUser(studentOpt.get());
                studentCp.setUnreadCount(0);
                studentCp.setLastReadAt(null);
                participantRepository.saveAndFlush(studentCp);
            }
        }
        
        return convertConversationToDTO(conversation, null);

// In convertConversationToDTO method, replace the participants section (around line 362-375) with:

        // ✅ FIX: Get participants from ConversationParticipant entities
        List<ConversationParticipant> participantEntities = participantRepository
            .findByConversationId(conversation.getId());
        
        if (participantEntities != null && !participantEntities.isEmpty()) {
            List<ParticipantDTO> participants = participantEntities.stream()
                .map(cp -> {
                    User user = cp.getUser();
                    if (user == null) return null;
                    ParticipantDTO p = new ParticipantDTO();
                    p.setId(user.getId());
                    p.setUsername(user.getUsername());
                    p.setName(user.getName());
                    p.setRole(user.getRole());
                    p.setAvatar(user.getProfilePicture());
                    return p;
                })
                .filter(p -> p != null)
                .collect(Collectors.toList());
            dto.setParticipants(participants);
        }

// In addParticipantToConversation method, replace lines 307-312 with:

        // ✅ FIX: Don't use setParticipants() - just create ConversationParticipant
        // No need to modify conversation.getParticipants()
        
        // Create participant record
        ConversationParticipant cp = new ConversationParticipant();
        cp.setConversation(conversation);
        cp.setUser(user);
        cp.setUnreadCount(0);
        cp.setLastReadAt(null);
        participantRepository.saveAndFlush(cp);

