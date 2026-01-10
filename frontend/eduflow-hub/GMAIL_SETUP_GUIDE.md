# Gmail SMTP Setup for Real Email Sending 📧

## Step 1: Gmail Account Setup

### Enable 2-Factor Authentication
1. Go to your Google Account settings
2. Navigate to **Security**
3. Enable **2-Step Verification** if not already enabled

### Generate App Password
1. In Google Account **Security** settings
2. Go to **2-Step Verification**
3. Scroll down to **App passwords**
4. Click **Generate** and select **Mail**
5. Copy the 16-character app password (e.g., `abcd efgh ijkl mnop`)

## Step 2: Update Application Properties

Update your `application.properties` file:

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-character-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Application URL (for email links)
app.base-url=http://localhost:5173
```

**Replace:**
- `your-email@gmail.com` with your actual Gmail address
- `your-16-character-app-password` with the app password from Step 1

## Step 3: Test Email Configuration

1. **Restart your Spring Boot application**
2. **Register a new user** with email verification enabled
3. **Check your email inbox** for the verification email
4. **Check console logs** for success/error messages

## Step 4: Troubleshooting

### If emails are not being sent:

1. **Check Console Logs:**
   ```
   ✅ Email sent successfully to: user@example.com  // Success
   ❌ Failed to send email to: user@example.com     // Error
   ```

2. **Common Issues:**
   - **Wrong credentials**: Double-check Gmail address and app password
   - **2FA not enabled**: Gmail requires 2-factor authentication for app passwords
   - **Firewall/Network**: Check if port 587 is blocked
   - **Gmail security**: Make sure "Less secure app access" is not needed (use app passwords instead)

3. **Test with Simple Email:**
   - Try sending to your own Gmail first
   - Check spam/junk folder
   - Verify the "From" address is your Gmail

## Step 5: Alternative Email Providers

### Outlook/Hotmail:
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=your-email@outlook.com
spring.mail.password=your-password
```

### Yahoo Mail:
```properties
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587
spring.mail.username=your-email@yahoo.com
spring.mail.password=your-app-password
```

### Custom SMTP Server:
```properties
spring.mail.host=your-smtp-server.com
spring.mail.port=587
spring.mail.username=your-username
spring.mail.password=your-password
```

## Step 6: Production Considerations

For production use, consider:
- **Environment Variables**: Store credentials in environment variables
- **Email Service Providers**: Use services like SendGrid, Mailgun, or AWS SES
- **Rate Limiting**: Implement email rate limiting
- **Email Templates**: Create professional HTML email templates
- **Monitoring**: Monitor email delivery rates and failures

## Current Email Features ✅

Once configured, your system will send:
- ✅ **Verification emails** when users register
- ✅ **Welcome emails** after verification
- ✅ **Password reset emails** (when implemented)
- ✅ **Notification emails** for assignments, quizzes, etc.

The email verification flow will work exactly as designed, but with real emails instead of console logs! 🎉