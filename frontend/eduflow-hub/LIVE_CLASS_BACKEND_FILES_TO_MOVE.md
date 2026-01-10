# Live Classes Backend Files - Move Instructions

## 📁 Files to Move to Backend Project

The following Java files are currently in the root directory and need to be moved to your Spring Boot backend project:

### Entity:
- `LiveClass.java` → Move to: `src/main/java/com/elearnhub/teacher_service/entity/`

### DTO:
- `LiveClassDTO.java` → Move to: `src/main/java/com/elearnhub/teacher_service/dto/`

### Repository:
- `LiveClassRepository.java` → Move to: `src/main/java/com/elearnhub/teacher_service/repository/`

### Service:
- `LiveClassService.java` → Move to: `src/main/java/com/elearnhub/teacher_service/service/`
- `LiveClassServiceImpl.java` → Move to: `src/main/java/com/elearnhub/teacher_service/service/`

### Controller:
- `LiveClassController.java` → Move to: `src/main/java/com/elearnhub/teacher_service/Controller/`

### SQL:
- `CREATE_LIVE_CLASSES_TABLE.sql` → Run in your MySQL database

## 🔧 How to Move Files

### Option 1: Manual Move (Windows)
```cmd
REM Assuming your backend is in a folder like "teacher-service" or "backend"
move LiveClass.java path\to\backend\src\main\java\com\elearnhub\teacher_service\entity\
move LiveClassDTO.java path\to\backend\src\main\java\com\elearnhub\teacher_service\dto\
move LiveClassRepository.java path\to\backend\src\main\java\com\elearnhub\teacher_service\repository\
move LiveClassService.java path\to\backend\src\main\java\com\elearnhub\teacher_service\service\
move LiveClassServiceImpl.java path\to\backend\src\main\java\com\elearnhub\teacher_service\service\
move LiveClassController.java path\to\backend\src\main\java\com\elearnhub\teacher_service\Controller\
```

### Option 2: Copy and Paste
1. Open your backend project in your IDE
2. Copy each file from the root directory
3. Paste into the correct package folder
4. Delete the files from root directory

## ✅ Verification Checklist

After moving files:
- [ ] All files are in correct packages
- [ ] No compilation errors in backend
- [ ] Backend builds successfully
- [ ] All imports are resolved
- [ ] Database tables created (run SQL)
- [ ] Backend server restarts successfully

## 🚀 After Moving Files

1. **Run SQL Migration:**
   ```sql
   -- Execute CREATE_LIVE_CLASSES_TABLE.sql in MySQL
   ```

2. **Restart Backend:**
   - Stop your Spring Boot application
   - Rebuild the project
   - Start the application

3. **Test Endpoints:**
   - Check if `/live-classes` endpoints are accessible
   - Test with Postman or frontend

## 📝 Package Structure Should Look Like:

```
backend/
└── src/
    └── main/
        └── java/
            └── com/
                └── elearnhub/
                    └── teacher_service/
                        ├── Controller/
                        │   └── LiveClassController.java
                        ├── entity/
                        │   └── LiveClass.java
                        ├── dto/
                        │   └── LiveClassDTO.java
                        ├── repository/
                        │   └── LiveClassRepository.java
                        └── service/
                            ├── LiveClassService.java
                            └── LiveClassServiceImpl.java
```

## 🎯 What's Already Done in Frontend

✅ All frontend files are already in place:
- ✅ `src/components/JitsiMeeting.tsx`
- ✅ `src/components/LiveClassManager.tsx`
- ✅ `src/pages/LiveClassRoom.tsx`
- ✅ Routes in `src/App.tsx`
- ✅ Integration in teacher `ClassDetail.tsx`
- ✅ Integration in student `ClassDetail.tsx`
- ✅ API functions in `src/services/api.ts`

## 🔍 Where is Your Backend Project?

If you're not sure where your backend project is, look for:
- A folder with `pom.xml` (Maven) or `build.gradle` (Gradle)
- A folder structure like `src/main/java/com/elearnhub/`
- Other Java files like `ClassController.java`, `UserService.java`, etc.

Common locations:
- `../backend/`
- `../teacher-service/`
- `../elearnhub-backend/`
- Same directory but in a subfolder

## 💡 Need Help?

If you can't find your backend project location, you can:
1. Search for `ClassController.java` in your file system
2. Look for the folder containing your Spring Boot application
3. Check your IDE's project structure

---

Once files are moved and backend is restarted, follow the **LIVE_CLASS_SETUP_GUIDE.md** for testing!
