# E-Learn Hub: Classroom & Assignment Management System

[![GitHub issues](https://img.shields.io/github/issues/nambhadane/E-LearnHub)](https://github.com/nambhadane/E-LearnHub/issues)
[![GitHub forks](https://img.shields.io/github/forks/nambhadane/E-LearnHub)](https://github.com/nambhadane/E-LearnHub/network)
[![GitHub stars](https://img.shields.io/github/stars/nambhadane/E-LearnHub)](https://github.com/nambhadane/E-LearnHub/stargazers)
[![License](https://img.shields.io/github/license/nambhadane/E-LearnHub)](https://github.com/nambhadane/E-LearnHub/blob/main/LICENSE)

---

## Students
1. Namrata Bhadane (PRN: 202301040135)  
2. Nikita Burgute (PRN: 202301040057)  

---

## Introduction
**E-Learn Hub** is a web-based platform designed to simplify online teaching and learning. It provides a centralized environment for teachers and students to interact seamlessly.  

- Teachers can create virtual classrooms, upload lessons, share assignments, and evaluate submissions.  
- Students can access materials, submit assignments, view feedback, and check grades.  

**Frontend:** React.js (Interactive UI)  
**Backend:** Spring Boot (Secure business logic & data management)  
**Database:** MySQL

---

## Features
- Secure login & role-based access control (Teacher / Student)  
- Class creation, enrollment, and management  
- Assignment upload, submission, and grading  
- Notifications & announcements for updates  
- User-friendly dashboard & responsive UI  

---

## Screenshots
> Replace these with your actual screenshots  

**Teacher Dashboard:**  
![Teacher Dashboard](screenshots/teacher_dashboard.png)  

**Student Dashboard:**  
![Student Dashboard](screenshots/student_dashboard.png)  

**Assignment Submission:**  
![Assignment Submission](screenshots/assignment_submission.png)  

---

## System Architecture
```[Frontend: React] <--> [Backend: Spring Boot REST API] <--> [Database: MySQL]```



- **Frontend (React):** Handles UI and user interaction.  
- **Backend (Spring Boot):** Processes business logic and communicates with the database.  
- **Database (MySQL):** Stores user info, class details, assignments, grades, etc.

---

## Technologies Used

| Component          | Technology                   |
|-------------------|------------------------------|
| Frontend           | React.js                     |
| Backend            | Spring Boot                  |
| Database           | MySQL                        |
| Programming Lang.  | Java, JavaScript             |
| Authentication     | JWT (JSON Web Tokens)        |
| Styling            | CSS / Tailwind / Bootstrap   |
| Hosting            | AWS / Firebase / Local Server|
| Tools              | VS Code, IntelliJ IDEA, Postman, GitHub |

---

## Modules

### User Module
- Login & Registration  
- Profile Management  

### Teacher Module
- Create and manage classes  
- Upload lessons and assignments  
- View submissions & provide grades  
- Send notifications  

### Student Module
- Join classes using code/link  
- Access study materials  
- Submit assignments  
- View feedback and grades  

---

## Installation & Setup

### Backend Setup (Spring Boot)
1. Navigate to `student-service` or `teacher-service` folder in Eclipse.  
2. Ensure **MySQL** is running and create a database `elearnhub`.  
3. Update `application.properties` with database credentials.  
4. Run the project using Eclipse (`Run as Spring Boot App`).  

### Frontend Setup (React)
1. Navigate to `frontend` folder in VS Code.  
2. Install dependencies:
```bash
npm install
