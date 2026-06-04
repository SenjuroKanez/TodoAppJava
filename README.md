# 📝 TodoApp - Task Management Application

A full-stack Java Spring Boot application for managing tasks with a modern web interface. Features user authentication, task management with priorities, and real-time synchronization between frontend and backend.

## 🎯 Features

- ✅ **User Authentication** - Secure JWT-based authentication
- 📋 **Task Management** - Create, read, update, delete todos
- 🎯 **Priority Levels** - Low, Medium, High, Urgent task prioritization
- 🏷️ **Categories** - Organize todos by categories
- 🔍 **Filtering** - View all, active, or completed todos
- 💾 **Persistent Storage** - H2 in-memory database with data persistence
- 🌐 **Web Interface** - Modern, responsive HTML/JavaScript frontend
- 🔐 **Security** - Spring Security with JWT token authentication
- 📡 **REST API** - Complete RESTful API with CORS support

## 🏗️ Project Structure

```
TodoAppJava/
├── todo-backend/          # Spring Boot REST API backend
│   ├── src/main/java/
│   │   └── com/todoapp/backend/
│   │       ├── controller/    # REST endpoints
│   │       ├── service/       # Business logic
│   │       ├── entity/        # JPA entities
│   │       ├── repository/    # Data access layer
│   │       ├── security/      # JWT & Security config
│   │       └── exception/     # Exception handling
│   └── src/main/resources/
│       └── application.properties
├── todo-web/              # Web frontend server
│   ├── src/main/java/
│   │   └── com/todoapp/web/
│   │       └── TodoWebApplication.java
│   └── src/main/resources/
│       ├── application.properties
│       └── static/
│           └── index.html    # Frontend UI
├── todo-common/           # Shared DTOs and models
│   └── src/main/java/
│       └── com/todoapp/common/
│           ├── dto/         # Data Transfer Objects
│           └── model/       # Domain models
└── pom.xml               # Maven configuration

```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Git

### Installation

1. **Clone the repository**
   ```bash
   cd TodoAppJava
   ```

2. **Build the project**
   ```bash
   mvn clean install -DskipTests
   ```

3. **Start the backend server** (Port 8081)
   ```bash
   cd todo-backend
   java -jar target/todo-backend-0.0.1-SNAPSHOT.jar
   ```

4. **In a new terminal, start the web server** (Port 8082)
   ```bash
   cd todo-web
   java -jar target/todo-web-0.0.1-SNAPSHOT.jar
   ```

5. **Open in browser**
   ```
   http://localhost:8082
   ```

## 📱 Web Application Usage

### Getting Started

1. **Register a new account**
   - Click "Don't have an account? Register"
   - Enter username, email, and password
   - Click "Register"

2. **Login**
   - Enter email and password
   - Click "Login"

3. **Manage Todos**
   - **Add Todo**: Enter title, description (optional), select priority, click "Add Todo"
   - **Complete Todo**: Check the checkbox next to a todo
   - **Delete Todo**: Click the "Delete" button
   - **Filter**: Use tabs to filter by All, Active, or Completed

### Features

- **Priority Levels**: Low, Medium, High, Urgent
- **Real-time Updates**: Changes sync instantly with backend
- **Local Storage**: Auth token saved in browser
- **Responsive Design**: Works on desktop and mobile

## 🔌 API Documentation

### Base URL
```
http://localhost:8081
```

### Authentication

All protected endpoints require the `Authorization` header:
```
Authorization: Bearer {jwt_token}
```

### Endpoints

#### Authentication

**Register New User**
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123"
}

Response (201):
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "createdAt": "2026-06-04T15:45:04",
    "themePreference": "LIGHT"
  }
}
```

**Login User**
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securePassword123"
}

Response (200):
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "user": { ... }
}
```

#### Todos

**Get All Todos** (Requires Auth)
```
GET /api/todos
Authorization: Bearer {token}

Response (200):
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "title": "Fix bug",
    "description": "Backend API issue",
    "completed": false,
    "priority": "HIGH",
    "dueDate": null,
    "category": null
  }
]
```

**Get Active Todos** (Requires Auth)
```
GET /api/todos/active
Authorization: Bearer {token}
```

**Get Completed Todos** (Requires Auth)
```
GET /api/todos/completed
Authorization: Bearer {token}
```

**Get Single Todo** (Requires Auth)
```
GET /api/todos/{id}
Authorization: Bearer {token}

Response (200): { ... todo object ... }
Response (404): Not found
```

**Create Todo** (Requires Auth)
```
POST /api/todos
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Fix bug",
  "description": "Backend API issue",
  "priority": "HIGH",
  "completed": false
}

Response (201): { ... created todo object ... }
```

**Update Todo** (Requires Auth)
```
PUT /api/todos/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Updated title",
  "description": "Updated description",
  "priority": "MEDIUM",
  "completed": false
}

Response (200): { ... updated todo object ... }
```

**Toggle Todo Completion** (Requires Auth)
```
PATCH /api/todos/{id}/complete
Authorization: Bearer {token}

Response (200): { ... todo with toggled completion status ... }
```

**Delete Todo** (Requires Auth)
```
DELETE /api/todos/{id}
Authorization: Bearer {token}

Response (204): No Content
Response (404): Not found
```

**Get Todos by Category** (Requires Auth)
```
GET /api/todos/category/{categoryId}
Authorization: Bearer {token}

Response (200): [ ... todos in category ... ]
```

#### Categories

**Get All Categories** (Requires Auth)
```
GET /api/categories
Authorization: Bearer {token}

Response (200):
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Work",
    "color": "#FF5733"
  }
]
```

**Create Category** (Requires Auth)
```
POST /api/categories
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Personal",
  "color": "#33FF57"
}

Response (201): { ... created category ... }
```

## 🗄️ Database

### Schema

**Users Table**
```sql
CREATE TABLE users (
  id UUID PRIMARY KEY,
  username VARCHAR(255) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  last_login TIMESTAMP,
  theme_preference VARCHAR(10) CHECK (theme_preference IN ('LIGHT', 'DARK', 'SYSTEM'))
);
```

**Todos Table**
```sql
CREATE TABLE todos (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id),
  title VARCHAR(255) NOT NULL,
  description VARCHAR(500),
  priority VARCHAR(10) CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
  completed BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  due_date TIMESTAMP,
  category_id UUID REFERENCES categories(id)
);
```

**Categories Table**
```sql
CREATE TABLE categories (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id),
  name VARCHAR(255) NOT NULL,
  color VARCHAR(255) NOT NULL
);
```

## 🔐 Security

### Authentication Flow

1. User registers/logs in
2. Backend validates credentials and issues JWT token
3. Frontend stores token in localStorage
4. All subsequent requests include token in `Authorization` header
5. Backend validates token using JWT secret (32+ characters)

### Security Features

- ✅ BCrypt password hashing
- ✅ JWT token-based authentication
- ✅ CORS enabled with origin validation
- ✅ Stateless session management
- ✅ Password stored securely (never returned in responses)
- ✅ User data isolation (users only access own todos)

### JWT Configuration

- **Secret Key**: `myVeryLongAndSecureSecretKeyThatIsAtLeast32CharactersLongForSecurityPurposes`
- **Expiration**: 24 hours (86400000 milliseconds)
- **Algorithm**: HS512

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: H2 (in-memory)
- **ORM**: Hibernate/JPA
- **Security**: Spring Security + JWT (jjwt 0.11.5)
- **Build**: Maven
- **Server**: Embedded Tomcat

### Frontend
- **HTML5**: Semantic markup
- **CSS3**: Responsive design
- **JavaScript**: ES6+ with Fetch API
- **Storage**: Browser localStorage
- **Server**: Spring Boot static resources

## 📊 System Requirements

| Component | Minimum | Recommended |
|-----------|---------|------------|
| Java | 17 | 17+ |
| RAM | 512 MB | 1 GB |
| Disk Space | 500 MB | 1 GB |
| OS | Linux/macOS/Windows | Any |

## 🚨 Common Issues & Troubleshooting

### Port Already in Use

If port 8081 or 8082 is already in use:

**Backend** (`todo-backend/src/main/resources/application.properties`):
```properties
server.port=8081
```

**Frontend** (`todo-web/src/main/resources/application.properties`):
```properties
server.port=8082
```

### Authentication Failed

- Verify correct email/password combination
- Check that backend server is running
- Clear browser cache/localStorage and try again

### CORS Errors

- Verify frontend URL matches allowed origins in `SecurityConfig.java`
- Check backend is running on correct port
- Ensure `Content-Type: application/json` header is set for POST/PUT requests

### Database Issues

- H2 in-memory database resets on restart
- For persistent storage, switch to PostgreSQL/MySQL
- Update connection string in `application.properties`

## 📝 Development Guide

### Building from Source

```bash
# Build entire project
mvn clean install

# Build specific module
mvn clean install -pl todo-backend

# Skip tests
mvn clean install -DskipTests

# Run with debug output
mvn clean install -X
```

### Running in Development

```bash
# Terminal 1: Backend
cd todo-backend
mvn spring-boot:run

# Terminal 2: Frontend
cd todo-web
mvn spring-boot:run
```

### Code Structure

**Backend Controllers** (`todo-backend/src/main/java/com/todoapp/backend/controller/`)
- `TodoController.java` - Todo endpoints
- `AuthController.java` - Authentication endpoints
- `CategoryController.java` - Category endpoints

**Services** (`todo-backend/src/main/java/com/todoapp/backend/service/`)
- Business logic layer
- Handles validation and transformations
- Transaction management

**Repositories** (`todo-backend/src/main/java/com/todoapp/backend/repository/`)
- Spring Data JPA repository interfaces
- Database queries

**Security** (`todo-backend/src/main/java/com/todoapp/backend/security/`)
- `SecurityConfig.java` - Spring Security configuration
- `JwtTokenProvider.java` - JWT token management
- `JwtAuthenticationFilter.java` - JWT validation filter

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is open source and available under the MIT License.

## 👤 Author

**SenjuroKanez**

## 📞 Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Check existing documentation
- Review API error messages for debugging

## 🎓 Learning Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Guide](https://spring.io/guides/gs/securing-web/)
- [JWT Introduction](https://jwt.io/introduction)
- [REST API Best Practices](https://restfulapi.net/)

## 📈 Roadmap

- [ ] Add email notifications
- [ ] Implement recurring todos
- [ ] Add todo due date reminders
- [ ] Team collaboration features
- [ ] Mobile app (React Native/Flutter)
- [ ] Dark mode toggle
- [ ] Advanced filtering and search
- [ ] Attachment support
- [ ] Activity history/logs
- [ ] PostgreSQL/MySQL support

## 🎉 Changelog

### Version 1.0.0 (Current)
- Initial release
- User authentication with JWT
- Full CRUD operations for todos
- Category management
- Web frontend with HTML/JavaScript
- Responsive design
- CORS support

---

**Last Updated**: June 4, 2026  
**Status**: ✅ Production Ready
