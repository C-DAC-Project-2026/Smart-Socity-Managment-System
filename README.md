# Smart Society Management System
# 🏢 Smart Society Management System

A full-stack web application developed to simplify the day-to-day management of residential societies. The system allows administrators, residents, and staff members to perform their tasks through a secure and user-friendly platform.

The project consists of a **Spring Boot REST API** backend and a **React + Vite** frontend. Authentication is implemented using **JWT**, payments are handled through **Razorpay**, and data is stored in **MySQL**.

---

## ✨ Features

### Authentication

* Secure user registration and login
* JWT-based authentication
* Role-based authorization
* Forgot password using Email OTP
* Password reset functionality

### Resident Management

* Manage resident details
* View personal profile
* Access society information

### Staff Management

* Add and manage staff members
* Update staff details
* Remove staff records

### Notice Board

* Publish society notices
* Edit and delete notices
* View latest announcements

### Complaint Management

* Raise complaints
* Track complaint status
* Update complaint progress
* Manage complaint history

### Maintenance Bills

* Generate maintenance bills
* View pending and paid bills
* Manage payment records

### Online Payments

* Razorpay payment gateway integration
* Secure online payment processing
* Payment history

### Notifications

* Send notifications to residents
* View notification history

### Dashboard

* Society statistics
* Complaint summary
* Payment summary
* Resident overview

---

# 🛠 Tech Stack

### Frontend

* React
* Vite
* JavaScript
* HTML5
* CSS3
* Axios
* React Router

### Backend

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate
* JWT Authentication
* Maven

### Database

* MySQL

### Other Tools

* Razorpay Payment Gateway
* Java Mail Sender
* Swagger/OpenAPI
* Lombok

---

# 📁 Project Structure

```text
Smart-Society-Management-System
│
├── backend
│   ├── src
│   ├── pom.xml
│   └── ...
│
├── frontend
│   ├── src
│   ├── public
│   ├── package.json
│   └── ...
│
└── README.md
```

---

# 🚀 Getting Started

## Clone the repository

```bash
git clone https://github.com/your-username/Smart-Society-Management-System.git

cd Smart-Society-Management-System
```

---

## Backend Setup

```bash
cd backend
```

Configure your MySQL database credentials in:

```properties
application.properties
```

Run the application:

```bash
mvn spring-boot:run
```

The backend will start on:

```
http://localhost:8080
```

---

## Frontend Setup

```bash
cd frontend

npm install

npm run dev
```

The frontend will start on:

```
http://localhost:5173
```

---

# API Documentation

Swagger UI is available after starting the backend.

```
http://localhost:8080/swagger-ui/index.html
```

---

# Security

The application uses:

* JWT Authentication
* Spring Security
* Password Encryption
* Role-Based Access Control
* Protected REST APIs

---

# Future Improvements

* Docker support
* Microservices architecture
* Real-time notifications
* Mobile application
* Report generation
* Admin analytics dashboard
* Email notification enhancements

---

# Learning Outcomes

Through this project I gained practical experience with:

* REST API development using Spring Boot
* JWT authentication and authorization
* Spring Security configuration
* Database design using MySQL
* Payment gateway integration with Razorpay
* React frontend development
* API integration using Axios
* Exception handling
* Layered architecture
* Git and GitHub version control

---

# Screenshots

You can add screenshots of:

* Login Page
* Dashboard
* Complaint Management
* Notice Board
* Maintenance Bills
* Payment Page
* Admin Panel

inside a `screenshots/` folder.

---

# Author

**Vaibhav Bhaskar More**

* B.Tech in Computer Science & Engineering
* PG-DAC Student
* Java | Spring Boot | React | MySQL

---

If you found this project useful, feel free to star the repository.
