 # Smart Classroom Management System

A JavaFX desktop application for managing students, faculty, attendance, and assignments in an educational institution.

---

## Tech Stack

| Layer        | Technology            |
|--------------|-----------------------|
| UI           | JavaFX 21 + FXML      |
| Backend      | Core Java (OOP)       |
| Database     | MySQL 8.x             |
| DB Access    | JDBC (PreparedStmt)   |
| Password     | BCrypt (jBCrypt 0.4)  |
| Build        | Maven 3.9.x           |

---

## Prerequisites

Before running the project, install all three tools below.

### 1. Java (Already Installed)
You have Java 26 installed. ✅

### 2. Install Apache Maven 3.9.x (Windows)

1. Go to: https://maven.apache.org/download.cgi
2. Download **apache-maven-3.9.x-bin.zip**
3. Extract to `C:\Program Files\Apache\maven\` (or any folder)
4. Add Maven to PATH:
   - Open **Start → Environment Variables**
   - Under **System variables**, click **Path → Edit → New**
   - Add `C:\Program Files\Apache\maven\bin`
5. Also set `JAVA_HOME`:
   - Add a new System variable: `JAVA_HOME` = `C:\Program Files\Common Files\Oracle\Java\javapath_target_253979343` (or your JDK folder)
6. Open a **new** PowerShell and verify:
   ```
   mvn -version
   ```
   You should see: `Apache Maven 3.9.x`

### 3. Install MySQL Community Server 8.x (Windows)

1. Go to: https://dev.mysql.com/downloads/installer/
2. Download **MySQL Installer for Windows** (full version ~440 MB)
3. Run the installer, choose **Custom** setup
4. Select: **MySQL Server 8.x** and **MySQL Workbench** (optional but useful)
5. During configuration:
   - Set root password (remember it — you'll need it for `db.properties`)
   - Keep port **3306** (default)
6. After install, open **MySQL Command Line Client** or **MySQL Workbench**

---

## Project Setup

### Step 1 – Configure Database Connection

Open `src/main/resources/db.properties` and set your MySQL root password:

```properties
db.url=jdbc:mysql://localhost:3306/smart_classroom_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true
db.username=root
db.password=YOUR_MYSQL_ROOT_PASSWORD
db.driver=com.mysql.cj.jdbc.Driver
```

> ⚠️ Replace `YOUR_MYSQL_ROOT_PASSWORD` with the password you set during MySQL installation.

### Step 2 – Create the Database Schema

Open MySQL Workbench or MySQL Command Line Client and run:

```sql
SOURCE path/to/SmartClassroom/src/main/resources/db/schema.sql;
```

Or from PowerShell (with MySQL on PATH):
```powershell
mysql -u root -p < src/main/resources/db/schema.sql
```

### Step 3 – Seed Sample Classes

```sql
SOURCE path/to/SmartClassroom/src/main/resources/db/seed.sql;
```

### Step 4 – Create User Accounts (Admin + Sample Data)

Run the `DatabaseSeeder` to insert admin, faculty, and student accounts with proper BCrypt-hashed passwords:

```powershell
mvn compile
mvn exec:java
```

This will create:

| Role    | Username  | Password    |
|---------|-----------|-------------|
| Admin   | admin     | Admin@123   |
| Faculty | faculty1  | Faculty@123 |
| Faculty | faculty2  | Faculty@123 |
| Student | student1  | Student@123 |
| Student | student2  | Student@123 |
| Student | student3  | Student@123 |

---

## Running the Application

```powershell
# From the SmartClassroom/ directory:
mvn clean javafx:run
```

---

## Project Structure

```
SmartClassroom/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/smartclassroom/
    │   ├── Main.java                   ← JavaFX entry point
    │   ├── model/                      ← Data model classes (OOP)
    │   ├── dao/                        ← JDBC database access
    │   ├── service/                    ← Business logic layer
    │   ├── controller/                 ← JavaFX event handlers
    │   └── util/                       ← Helpers (DB, BCrypt, Validation)
    └── resources/
        ├── db.properties               ← DB connection config
        ├── db/schema.sql               ← MySQL table definitions
        ├── db/seed.sql                 ← Sample data
        ├── fxml/                       ← UI layout files
        └── css/style.css               ← Application theme
```

---

## Architecture

```
JavaFX View (FXML)
      ↓
  Controller        ← handles UI events, calls services
      ↓
   Service          ← business rules, OOP, Collections/DSA
      ↓
    DAO             ← PreparedStatement SQL via JDBC
      ↓
  MySQL Database    ← persistent storage
```

---

## User Roles

| Role    | Key Features |
|---------|-------------|
| Admin   | Manage students, faculty, classes, subjects |
| Faculty | Mark attendance, create/evaluate assignments |
| Student | View attendance, submit assignments, view marks |

---

## DSA Highlight – Low Attendance Report

`AttendanceService.getLowAttendanceReport()` demonstrates:
1. Retrieve raw attendance rows → `List<Attendance>`
2. Build `HashMap<Integer, int[]>` keyed by `studentId` → `[present, total]`
3. Calculate percentage for each student
4. Filter students below the configurable threshold (default 75%)
5. Sort result list by ascending attendance % using `Comparator`
6. Display in JavaFX `TableView`

---

## Troubleshooting

| Problem | Solution |
|---------|---------|
| `mvn` not recognized | Restart terminal after adding Maven to PATH |
| Cannot connect to DB | Check `db.properties` password; ensure MySQL service is running |
| JavaFX not found | Use `mvn javafx:run` not `java -jar` |
| `Communications link failure` | Start MySQL service: `net start MySQL80` |

