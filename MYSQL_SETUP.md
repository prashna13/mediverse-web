# MySQL Setup Guide for Mediverse Hospital Management System

## Prerequisites

Before running the application, you need to have MySQL installed and running on your system.

### 1. Install MySQL

#### On macOS (using Homebrew):
```bash
brew install mysql
brew services start mysql
```

#### On Ubuntu/Debian:
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql
```

#### On Windows:
Download and install MySQL from the official website: https://dev.mysql.com/downloads/mysql/

### 2. MySQL Setup

#### Start MySQL service (if not already running):
```bash
# macOS
brew services start mysql

# Linux
sudo systemctl start mysql

# Windows
net start mysql
```

#### Access MySQL as root:
```bash
mysql -u root -p
```

#### Create the database (optional - will be created automatically):
```sql
CREATE DATABASE mediverse_db;
EXIT;
```

### 3. Configuration

The application is configured to use the following MySQL settings in `application.properties`:

- **Host**: localhost
- **Port**: 3306
- **Database**: mediverse_db (will be created automatically)
- **Username**: root
- **Password**: (empty by default)

#### If you have a different MySQL setup, update the following in `src/main/resources/application.properties`:

```properties
# Update these values according to your MySQL setup
spring.datasource.url=jdbc:mysql://localhost:3306/mediverse_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=your_mysql_username 
spring.datasource.password=your_mysql_password
```

### 4. Run the Application

Once MySQL is set up and running:

```bash
mvn spring-boot:run
```

The application will:
1. Automatically create the `mediverse_db` database if it doesn't exist
2. Create all necessary tables using Hibernate DDL
3. Initialize sample data including users, departments, doctors, and patients

### 5. Access the Application

- **Web Interface**: http://localhost:8080
- **Admin Login**: admin@mediverse.com / admin123
- **Sample Doctor Login**: sarah.johnson@mediverse.com / doctor123
- **Sample Patient Login**: john.doe@email.com / patient123

### 6. Database Schema

The application will automatically create the following tables:
- `users` - User authentication and basic info
- `departments` - Hospital departments
- `branches` - Hospital branch locations
- `doctors` - Doctor profiles and specializations
- `patients` - Patient medical records
- `appointments` - Appointment scheduling

### Troubleshooting

#### Connection Issues:
1. Ensure MySQL is running: `brew services list | grep mysql` (macOS) or `sudo systemctl status mysql` (Linux)
2. Check if port 3306 is available: `netstat -an | grep 3306`
3. Verify MySQL credentials are correct
4. Check firewall settings if connecting remotely

#### Permission Issues:
```sql
-- If you encounter permission issues, run these commands in MySQL:
CREATE USER 'mediverse'@'localhost' IDENTIFIED BY 'mediverse123';
GRANT ALL PRIVILEGES ON mediverse_db.* TO 'mediverse'@'localhost';
FLUSH PRIVILEGES;
```

Then update `application.properties`:
```properties
spring.datasource.username=mediverse
spring.datasource.password=mediverse123
```

#### Reset Database:
To start fresh, drop and recreate the database:
```sql
DROP DATABASE IF EXISTS mediverse_db;
CREATE DATABASE mediverse_db;
```

## Additional Notes

- The application uses Hibernate with `ddl-auto=update` which means schema changes will be applied automatically
- Sample data is initialized only on the first run when the database is empty
- All passwords are encrypted using BCrypt
- Connection pooling is configured with HikariCP for optimal performance
