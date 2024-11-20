# Cue Master: Your Ultimate 9-Ball Management System

Cue Master is an innovative platform designed to simplify the management of 9-ball pool games. It combines a **Spring Boot** backend with a **ReactJS + TypeScript** frontend, offering a seamless experience for players, organizers, and enthusiasts. Whether you want to manage tournaments, track matches, or explore player statistics, Cue Master has you covered.

---

## Features

- **Player Management**: Create and manage player profiles with avatars.
- **Tournament Management**: Organize and track tournaments, including match schedules and results.
- **Leaderboard**: View rankings based on player performance and tournament outcomes.
- **Match Tracking**: Record scores and details for each match.
- **Authentication**: Secure user authentication with form-based login, Google OAuth2, and enhanced security measures including Google reCAPTCHA v2 and two-factor authentication (2FA) via TOTP delivered through email.
---

## Project Structure

### Backend: Spring Boot
- **Framework**: Spring Boot (Java)
- **Database**: H2 (in-memory or file-based database, configurable for other relational databases)
- **Security**: Spring Security with support for form-based login and OAuth2.
- **REST APIs**: Backend exposes APIs for frontend integration.
- **Build Tool**: Maven

### Frontend: ReactJS + TypeScript
- **UI Library**: React with TypeScript for strong typing and robust development.
- **State Management**: React Context API for global state handling.
- **Styling**: CSS modules for scoped styles.
- **Routing**: React Router for seamless navigation.
- **HTTP Requests**: Axios for API integration.

---

## Getting Started

### Prerequisites

#### Backend
- Java 17+
- Maven

#### Frontend
- Node.js (v18+)
- npm

---

### Backend Setup

1. Clone the repository:

   ```bash
   git clone https://github.com/nehars17/CS203G5.git
   cd Project
2. Update the application.properties file with your MySQL credentials:
     ```
    server.error.include-stacktrace=never
    spring.datasource.url=jdbc:h2:mem:cuemasterdb;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER
    spring.datasource.driver-class-name=org.h2.Driver
    spring.h2.console.enabled=true
    spring.datasource.username=sa
    spring.datasource.password=
    
    ```
3. Build and run the application:
    ```
      mvn clean install
      mvn spring-boot:run
    ```
4. The backend API should now be accessible at http://localhost:8080.

### Frontend Setup
1. navigate to frontend directory
   ```
   cd cuemaster
   ```
3. Install dependencies:
   ```
   npm install
   ```
4. npm start
5. The frontend should now be accessible at http://localhost:3000.


   


