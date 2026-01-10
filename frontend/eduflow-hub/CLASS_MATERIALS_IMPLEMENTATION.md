# Class Materials Feature - Implementation Guide

## ✅ What's Been Created

### Backend Files (Copy to your backend project)

1. **Material.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/entity/Material.java`
   - Entity for storing material metadata

2. **MaterialRepository.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/repository/MaterialRepository.java`
   - Database repository for materials

3. **MaterialDTO.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/dto/MaterialDTO.java`
   - Data transfer object for API responses

4. **MaterialService.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/service/MaterialService.java`
   - Service interface

5. **MaterialServiceImpl.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/service/MaterialServiceImpl.java`
   - Service implementation with file upload/download logic

6. **MaterialController.java** → `teacher-service/src/main/java/com/elearnhub/teacher_service/Controller/MaterialController.java`
   - REST API endpoints

### Database

7. **CREATE_MATERIALS_TABLE.sql**
   - Run this SQL to create the materials table

### Frontend Files (Already in place)

8. **src/services/api.ts** - Material API functions added
9. **src/pages/teacher/ClassMaterials.tsx** - Full UI with upload/download
10. **src/pages/teacher/MyClasses.tsx** - Already has Materials button

## 📋 Setup Steps

### 1. Database Setup
```sql
-- Run this in your MySQL database
CREATE TABLE IF NOT EXISTS materials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT,
    class_id BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (class_id) REFERENCES class_entity(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_class_id (class_id),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_uploaded_at (uploaded_at)
);
```

### 2. Backend Configuration

Add to your `application.properties`:
```properties
# File upload settings
file.upload-dir=uploads/materials
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

### 3. Copy Backend Files

Copy all Java files to your backend project:
- Material.java → entity folder
- MaterialRepository.java → repository folder
- MaterialDTO.java → dto folder
- MaterialService.java → service folder
- MaterialServiceImpl.java → service folder
- MaterialController.java → Controller folder

### 4. Restart Backend

Restart your Spring Boot application.

## 🎯 Features

### For Teachers:
- ✅ Upload materials (PDF, DOC, PPT, videos, etc.)
- ✅ Add title and description
- ✅ View all materials for a class
- ✅ Download materials
- ✅ Delete materials (only their own)
- ✅ See file size, type, and upload date
- ✅ Access via "Materials" button in My Classes

### For Students:
- ✅ View all materials for their enrolled classes
- ✅ Download materials
- ✅ See who uploaded and when

## 🔌 API Endpoints

### Upload Material
```
POST /materials
Content-Type: multipart/form-data
Authorization: Bearer {token}

Form Data:
- file: (file)
- title: string
- description: string (optional)
- classId: number
```

### Get Materials by Class
```
GET /materials/class/{classId}
Authorization: Bearer {token}
```

### Download Material
```
GET /materials/{id}/download
Authorization: Bearer {token}
```

### Delete Material
```
DELETE /materials/{id}
Authorization: Bearer {token}
```

## 📁 File Storage

Files are stored in: `uploads/materials/`
- Each file gets a unique UUID filename
- Original filename is preserved in database
- Automatic directory creation

## 🎨 UI Features

- Drag & drop file upload dialog
- File type icons (PDF, video, document, etc.)
- File size display (KB/MB)
- Upload date formatting
- Search and filter (coming soon)
- Responsive design

## 🧪 Testing

1. Go to My Classes
2. Click "Materials" button on any class
3. Click "Upload Material"
4. Fill in title, description, select file
5. Click Upload
6. Material appears in list
7. Click Download to test download
8. Click trash icon to delete

## 🔒 Security

- Only teachers can upload/delete materials
- Students can only view/download
- Teachers can only delete their own materials
- File validation by extension
- JWT authentication required

## ✨ Next Steps (Optional Enhancements)

- [ ] Add file preview for PDFs/images
- [ ] Add categories/tags for materials
- [ ] Add search and filter
- [ ] Add bulk upload
- [ ] Add material versioning
- [ ] Add view/download analytics
- [ ] Add student access logs

## 🎉 You're Done!

The Materials feature is now fully functional. Teachers can upload notes, PDFs, videos, and any other files for their classes!
