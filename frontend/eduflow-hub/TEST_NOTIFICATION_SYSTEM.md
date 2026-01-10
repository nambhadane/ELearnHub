# 🧪 Testing the Notification System

## Step 1: Create the Database Table

Run this SQL in your MySQL database:

```sql
-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    type VARCHAR(50) NOT NULL,
    reference_id BIGINT,
    reference_type VARCHAR(50),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at),
    INDEX idx_user_unread (user_id, is_read)
);
```

## Step 2: Verify Table Creation

```sql
-- Check if table exists
SHOW TABLES LIKE 'notifications';

-- Check table structure
DESCRIBE notifications;
```

## Step 3: Restart Backend

Restart your Spring Boot application to load the new NotificationController and updated MessageServiceImpl.

## Step 4: Test Manually (Optional)

Insert a test notification:

```sql
-- Replace user_id with an actual user ID from your users table
INSERT INTO notifications (user_id, title, message, type, is_read, created_at) 
VALUES 
(1, 'Test Notification', 'This is a test notification', 'SYSTEM', FALSE, NOW());
```

## Step 5: Test with Messages

1. **Login as Student**
2. **Send a message to a Teacher**
3. **Login as Teacher**
4. **Check the notification bell** - You should see:
   - Red badge with count "1"
   - Notification in dropdown: "Student Name: message preview"

## Step 6: Verify Notification API

Test the API endpoints:

```bash
# Get unread count
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8082/notifications/unread/count

# Get all notifications
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8082/notifications

# Get unread notifications
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8082/notifications/unread
```

## Expected Behavior

### When Student sends message to Teacher:
1. ✅ Message is sent successfully
2. ✅ Teacher receives notification
3. ✅ Notification bell shows red badge with count
4. ✅ Clicking bell shows notification with message preview
5. ✅ Clicking notification marks it as read
6. ✅ Badge count decreases

### When Teacher sends message to Student:
1. ✅ Message is sent successfully
2. ✅ Student receives notification
3. ✅ Same behavior as above

## Troubleshooting

### Issue: No notifications appearing

**Check 1: Table exists**
```sql
SHOW TABLES LIKE 'notifications';
```

**Check 2: Backend logs**
Look for errors in Spring Boot console when sending messages.

**Check 3: User IDs**
```sql
-- Verify user IDs exist
SELECT id, username, name FROM users;
```

**Check 4: Notifications created**
```sql
-- Check if notifications are being created
SELECT * FROM notifications ORDER BY created_at DESC LIMIT 10;
```

### Issue: 404 Not Found on /notifications

- Restart Spring Boot backend
- Check NotificationController is in the correct package
- Verify @RestController annotation is present

### Issue: 401 Unauthorized

- Check authentication token is valid
- Verify token is being sent in Authorization header

### Issue: Frontend not showing notifications

- Open browser console (F12)
- Check for JavaScript errors
- Verify API calls are being made
- Check network tab for API responses

## Success Indicators

✅ SQL table created successfully
✅ Backend starts without errors
✅ Sending message creates notification in database
✅ Notification bell shows unread count
✅ Clicking bell shows notification list
✅ Mark as read works
✅ Delete notification works

## Quick Database Check

```sql
-- See all notifications
SELECT 
    n.id,
    n.title,
    n.message,
    n.type,
    n.is_read,
    u.username as recipient,
    n.created_at
FROM notifications n
JOIN users u ON n.user_id = u.id
ORDER BY n.created_at DESC
LIMIT 20;
```

---

**If everything works, you should see notifications appearing in real-time when messages are sent!** 🎉
