# 🔔 Notification System Implementation Guide

## Overview
A complete real-time notification system for the e-learning platform with backend API and frontend UI components.

## ✅ What's Been Implemented

### Backend (Java/Spring Boot)

1. **Entity**: `Notification.java`
   - Stores notification data
   - Supports multiple notification types (MESSAGE, ASSIGNMENT, GRADE, etc.)
   - Tracks read/unread status
   - References related entities

2. **Repository**: `NotificationRepository.java`
   - Query notifications by user
   - Filter unread notifications
   - Count unread notifications
   - Bulk operations (mark all as read, delete old)

3. **Service**: `NotificationService.java` & `NotificationServiceImpl.java`
   - Create notifications
   - Get user notifications
   - Mark as read (single/all)
   - Delete notifications

4. **Controller**: `NotificationController.java`
   - REST API endpoints
   - Authentication required
   - Role-based access (TEACHER/STUDENT)

5. **Database**: `CREATE_NOTIFICATIONS_TABLE.sql`
   - SQL migration script
   - Indexes for performance
   - Foreign key constraints

### Frontend (React/TypeScript)

1. **API Functions**: Added to `src/services/api.ts`
   - `getNotifications()`
   - `getUnreadNotifications()`
   - `getUnreadNotificationCount()`
   - `markNotificationAsRead()`
   - `markAllNotificationsAsRead()`
   - `deleteNotification()`
   - `deleteAllReadNotifications()`

2. **Component**: `NotificationBell.tsx`
   - Bell icon with unread badge
   - Dropdown with notification list
   - Mark as read functionality
   - Delete notifications
   - Auto-refresh every 30 seconds
   - Beautiful UI with icons

3. **Integration**: Updated `DashboardHeader.tsx`
   - Replaced mock notifications with real component
   - Available in both student and teacher dashboards

## 📋 Setup Instructions

### 1. Database Setup

Run the SQL migration:

```sql
-- Execute CREATE_NOTIFICATIONS_TABLE.sql in your MySQL database
```

### 2. Backend Setup

The Java files are ready to use. Make sure they're in the correct package structure:

```
src/main/java/com/elearnhub/teacher_service/
├── entity/
│   └── Notification.java
├── repository/
│   └── NotificationRepository.java
├── service/
│   ├── NotificationService.java
│   └── NotificationServiceImpl.java
├── Controller/
│   └── NotificationController.java
└── dto/
    └── NotificationDTO.java
```

### 3. Frontend Setup

Dependencies are already installed (`date-fns`). The components are ready to use.

## 🎯 API Endpoints

### Get All Notifications
```
GET /api/notifications
Authorization: Bearer {token}
```

### Get Unread Notifications
```
GET /api/notifications/unread
Authorization: Bearer {token}
```

### Get Unread Count
```
GET /api/notifications/unread/count
Authorization: Bearer {token}
Response: { "count": 5 }
```

### Mark as Read
```
PUT /api/notifications/{id}/read
Authorization: Bearer {token}
```

### Mark All as Read
```
PUT /api/notifications/read-all
Authorization: Bearer {token}
```

### Delete Notification
```
DELETE /api/notifications/{id}
Authorization: Bearer {token}
```

### Delete All Read
```
DELETE /api/notifications/read
Authorization: Bearer {token}
```

## 🔧 How to Create Notifications

### Example: Create notification when new message is received

In your `MessageServiceImpl.java`, add:

```java
@Autowired
private NotificationService notificationService;

// After sending a message
public MessageDTO sendMessage(Long conversationId, Long senderId, String content, List<String> filePaths) {
    // ... existing code ...
    
    // Create notifications for all participants except sender
    List<ConversationParticipant> participants = conversation.getParticipants();
    for (ConversationParticipant participant : participants) {
        if (!participant.getUser().getId().equals(senderId)) {
            notificationService.createNotification(
                participant.getUser().getId(),
                "New Message",
                "You have a new message from " + sender.getName(),
                Notification.NotificationType.MESSAGE,
                message.getId(),
                "MESSAGE"
            );
        }
    }
    
    return messageDTO;
}
```

### Example: Create notification when assignment is graded

```java
notificationService.createNotification(
    studentId,
    "Assignment Graded",
    "Your assignment '" + assignment.getTitle() + "' has been graded: " + grade + "/100",
    Notification.NotificationType.GRADE,
    assignmentId,
    "ASSIGNMENT"
);
```

## 🎨 Notification Types

- **MESSAGE**: New message received
- **ASSIGNMENT**: New assignment posted
- **GRADE**: Assignment graded
- **ANNOUNCEMENT**: Class announcement
- **ENROLLMENT**: Enrolled in class
- **SUBMISSION**: Student submitted assignment
- **REMINDER**: Deadline reminder
- **SYSTEM**: System notification

## 🚀 Features

✅ Real-time unread count badge
✅ Auto-refresh every 30 seconds
✅ Mark individual notifications as read
✅ Mark all as read
✅ Delete individual notifications
✅ Beautiful UI with type-specific icons
✅ Time ago formatting (e.g., "2 hours ago")
✅ Smooth animations
✅ Mobile responsive
✅ Dark mode support

## 📝 Next Steps

1. **Run the SQL migration** to create the notifications table
2. **Restart your backend** to load the new controllers
3. **Test the notification bell** in the dashboard
4. **Add notification creation** to your existing features:
   - When messages are sent
   - When assignments are posted
   - When grades are assigned
   - When students enroll in classes

## 🎉 Testing

To test the system, you can manually insert test notifications:

```sql
INSERT INTO notifications (user_id, title, message, type, is_read, created_at) 
VALUES 
(1, 'Welcome!', 'Welcome to the notification system', 'SYSTEM', FALSE, NOW()),
(1, 'New Message', 'You have a new message from John', 'MESSAGE', FALSE, NOW());
```

Then refresh your dashboard and click the bell icon!

## 🐛 Troubleshooting

- **Bell icon not showing**: Check browser console for errors
- **No notifications loading**: Verify backend is running and SQL table exists
- **401 Unauthorized**: Check authentication token is valid
- **Count not updating**: Wait 30 seconds or refresh the page

---

**Status**: ✅ Complete and Ready to Use!
