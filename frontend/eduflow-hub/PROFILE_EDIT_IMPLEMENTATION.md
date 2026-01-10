# Profile Edit Feature - Implementation Guide

## ✅ What's Been Created

### Backend Files (Copy to your backend project)

1. **ProfileDTO.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/dto/ProfileDTO.java`
   - Clean DTO for profile responses (no password)

2. **UserController.java** (Enhanced) → Already exists, now with:
   - GET /user/profile - Get current user profile
   - PUT /user/profile - Update profile info
   - POST /user/profile/picture - Upload profile picture
   - DELETE /user/profile/picture - Delete profile picture
   - PUT /user/change-password - Change password (existing)

### Frontend

3. **src/services/api.ts** - Enhanced with:
   - getCurrentProfile() - Get user profile
   - updateProfile() - Update profile (now returns updated data)
   - uploadProfilePicture() - Upload profile picture
   - deleteProfilePicture() - Delete profile picture

## 📋 Setup Steps

### 1. Backend Configuration

Add to your `application.properties`:
```properties
# Profile picture upload directory
file.upload-dir=uploads/profiles

# File upload settings (if not already set)
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### 2. Copy Backend Files

- Copy ProfileDTO.java to dto folder
- Replace UserController.java with the enhanced version

### 3. Restart Backend

Restart your Spring Boot application.

## 🎯 Features

### Profile Management:

**1. Get Profile:**
- ✅ Retrieve current user profile
- ✅ Returns clean DTO (no password)
- ✅ Includes profile picture path

**2. Update Profile:**
- ✅ Update name, email, phone, address
- ✅ Email validation
- ✅ Returns updated profile data
- ✅ Trim whitespace automatically

**3. Profile Picture:**
- ✅ Upload profile picture (images only)
- ✅ Auto-delete old picture when uploading new one
- ✅ Unique filename generation
- ✅ Delete profile picture
- ✅ File type validation

**4. Password Change:**
- ✅ Change password (existing feature)
- ✅ Current password validation
- ✅ Minimum length validation

## 🔌 API Endpoints

### Get Current Profile
```
GET /user/profile
Authorization: Bearer {token}

Response:
{
  "id": 14,
  "username": "teacher1",
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "+1234567890",
  "address": "123 Main St",
  "profilePicture": "profile_14_uuid.jpg",
  "role": "TEACHER"
}
```

### Update Profile
```
PUT /user/profile
Authorization: Bearer {token}
Content-Type: application/json

Body:
{
  "name": "John Smith",
  "email": "john.smith@example.com",
  "phoneNumber": "+1234567890",
  "address": "456 Oak Ave"
}

Response:
{
  "message": "Profile updated successfully",
  "profile": { ... }
}
```

### Upload Profile Picture
```
POST /user/profile/picture
Authorization: Bearer {token}
Content-Type: multipart/form-data

Form Data:
- file: (image file)

Response:
{
  "message": "Profile picture uploaded successfully",
  "profile": { ... },
  "profilePicture": "profile_14_uuid.jpg"
}
```

### Delete Profile Picture
```
DELETE /user/profile/picture
Authorization: Bearer {token}

Response:
{
  "message": "Profile picture deleted successfully"
}
```

### Change Password
```
PUT /user/change-password
Authorization: Bearer {token}
Content-Type: application/json

Body:
{
  "currentPassword": "oldpass",
  "newPassword": "newpass"
}

Response:
{
  "message": "Password changed successfully"
}
```

## 💻 Frontend Integration

### Example Usage:

```typescript
import { 
  getCurrentProfile, 
  updateProfile, 
  uploadProfilePicture, 
  deleteProfilePicture,
  changePassword 
} from "@/services/api";

// Load profile
const profile = await getCurrentProfile();

// Update profile
const updated = await updateProfile({
  name: "John Smith",
  email: "john@example.com"
});

// Upload profile picture
const file = event.target.files[0];
const updatedProfile = await uploadProfilePicture(file);

// Delete profile picture
await deleteProfilePicture();

// Change password
await changePassword({
  currentPassword: "old",
  newPassword: "new"
});
```

### Integration with Profile Page:

```typescript
const [profile, setProfile] = useState<ProfileData | null>(null);
const [loading, setLoading] = useState(true);

useEffect(() => {
  const loadProfile = async () => {
    try {
      const data = await getCurrentProfile();
      setProfile(data);
    } catch (error) {
      toast({ title: "Error", description: "Failed to load profile" });
    } finally {
      setLoading(false);
    }
  };
  loadProfile();
}, []);

const handleSave = async (updates: Partial<ProfileData>) => {
  try {
    const updated = await updateProfile(updates);
    setProfile(updated);
    toast({ title: "Success", description: "Profile updated" });
  } catch (error) {
    toast({ title: "Error", description: "Failed to update profile" });
  }
};

const handlePictureUpload = async (file: File) => {
  try {
    const updated = await uploadProfilePicture(file);
    setProfile(updated);
    toast({ title: "Success", description: "Picture uploaded" });
  } catch (error) {
    toast({ title: "Error", description: "Failed to upload picture" });
  }
};
```

## 🎨 Profile Picture Storage

**Storage Location:** `uploads/profiles/`

**Filename Format:** `profile_{userId}_{uuid}.{extension}`

**Example:** `profile_14_a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg`

**Features:**
- Unique filename per upload
- Auto-delete old picture on new upload
- Supports all image formats (jpg, png, gif, etc.)
- 10MB max file size

## 🔒 Security

- ✅ Users can only access/update their own profile
- ✅ JWT authentication required
- ✅ Email format validation
- ✅ Image file type validation
- ✅ Password validation (min 6 chars)
- ✅ Current password verification for password change
- ✅ No password in profile responses

## ✨ Validation

**Email:**
- Must contain @ symbol
- Trimmed automatically

**Name:**
- Cannot be empty
- Trimmed automatically

**Profile Picture:**
- Must be an image file
- Max 10MB
- Supported: jpg, png, gif, webp, etc.

**Password:**
- Minimum 6 characters
- Must provide current password
- BCrypt encrypted

## 📱 Frontend Components to Update

### Profile Page:
- Load profile on mount
- Display profile info
- Edit form for name, email, phone, address
- Profile picture upload with preview
- Delete picture button
- Save button

### Settings Page:
- Password change form
- Current password field
- New password field
- Confirm password field

### Header/Navbar:
- Display profile picture
- Display user name
- Link to profile page

## 🎉 Complete Example

```typescript
// Profile.tsx
import { useState, useEffect } from "react";
import { getCurrentProfile, updateProfile, uploadProfilePicture } from "@/services/api";

export default function Profile() {
  const [profile, setProfile] = useState(null);
  const [editing, setEditing] = useState(false);
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    phoneNumber: "",
    address: ""
  });

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    const data = await getCurrentProfile();
    setProfile(data);
    setFormData({
      name: data.name || "",
      email: data.email || "",
      phoneNumber: data.phoneNumber || "",
      address: data.address || ""
    });
  };

  const handleSave = async () => {
    const updated = await updateProfile(formData);
    setProfile(updated);
    setEditing(false);
  };

  const handlePictureUpload = async (e) => {
    const file = e.target.files[0];
    if (file) {
      const updated = await uploadProfilePicture(file);
      setProfile(updated);
    }
  };

  return (
    <div>
      {/* Profile picture */}
      <img src={`/uploads/profiles/${profile?.profilePicture}`} />
      <input type="file" onChange={handlePictureUpload} accept="image/*" />
      
      {/* Profile form */}
      <input value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} />
      <input value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})} />
      
      <button onClick={handleSave}>Save</button>
    </div>
  );
}
```

## 🚀 You're Done!

The Profile Edit backend is complete! Users can now:
- View their profile
- Update their information
- Upload/delete profile pictures
- Change their password

All changes persist to the database and are validated for security! 🎉👤
