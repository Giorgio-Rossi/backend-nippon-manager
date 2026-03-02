# Spring Boot JWT Starter

A starter project for Spring Boot applications with JWT authentication pre-configured.

## Features

- ✅ Spring Boot 3.2.1
- ✅ Spring Security with JWT
- ✅ JPA/Hibernate with H2 database
- ✅ Validation with Bean Validation
- ✅ Password encoding with BCrypt
- ✅ Refresh token support
- ✅ Role-based authorization
- ✅ CORS configured
- ✅ RESTful API

## Project Structure

```
src/main/java/com/example/springjwtstarter/
├── config/
│   ├── SecurityConfig.java          # Spring Security configuration
│   └── DataInitializer.java         # Test data initialization
├── controller/
│   ├── AuthController.java          # Authentication endpoints
│   └── TestController.java          # Test endpoints
├── dto/
│   ├── Request/
│   │   ├── LoginRequest.java        # Login DTO
│   │   ├── RegisterRequest.java     # Registration DTO
│   └── Response/
│       ├── JwtResponse.java         # JWT response DTO
│       └── ApiResponse.java         # Generic response DTO
├── entity/
│   └── User.java                    # User entity
├── filter/
│   └── JwtAuthenticationFilter.java # JWT filter
├── repository/
│   └── UserRepository.java          # User repository
├── service/
│   └── CustomUserDetailsService.java # UserDetails service
├── util/
│   └── JwtUtil.java                 # JWT utility
└── StarterApplication.java          # Main class
```

## Quick Setup

### 1. Clone and customize the project

```bash
git clone https://github.com/Giorgio-Rossi/spring-boot-project-starter.git
cd spring-jwt-starter

# Modify the package name and group ID in pom.xml
# Modify the package name in all Java files
```

### 2. Configure the application

Modify `src/main/resources/application.yml`:

```yaml
app:
  jwt:
    secret: yourCustomSecretKey123456789012345678901234567890
    expiration: 86400000 # 24 hours
    refresh-expiration: 604800000 # 7 days
```

### 3. Run the application

```bash
mvn spring-boot:run
```

The application will be available at: `http://localhost:8080/api`

## Default Users

The system automatically creates two test users:

- **Admin**: `admin` / `admin123` (ROLE_ADMIN)
- **User**: `user` / `user123` (ROLE_USER)

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register new user |
| POST | `/auth/login` | User login |
| POST | `/auth/refresh` | Refresh token |

### Test Endpoints

| Method | Endpoint | Authorization | Description |
|--------|----------|---------------|-------------|
| GET | `/test/all` | Public | Open access |
| GET | `/test/user` | USER/ADMIN | Authenticated users only |
| GET | `/test/admin` | ADMIN | Administrators only |
| GET | `/test/profile` | USER/ADMIN | User profile |

## Usage Examples

### 1. Registration

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "admin"
}
```

### 3. Authenticated Request

```bash
curl -X GET http://localhost:8080/api/test/user \
  -H "Authorization: Bearer <your-jwt-token>"
```

## Customization

### 1. Change Database

To use MySQL instead of H2, modify the `pom.xml`:

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
```

And `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database
    username: your_username
    password: your_password
  jpa:
    database-platform: org.hibernate.dialect.MySQL8Dialect
```

### 2. Add New Roles

Modify the `Role` enum in `User.java`:

```java
public enum Role {
    USER, ADMIN
}
```

### 3. Customize JWT Claims

Modify `JwtUtil.java` to add custom claims:

```java
private String createToken(Map<String, Object> claims, String subject, Long expiration) {
    claims.put("role", userRole); 
    claims.put("customClaim", "customValue"); 
    
    return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey())
            .compact();
}
```
## Testing
Il progetto include test unitari e di integrazione:

- ✅ JUnit 5 for running test cases
- ✅ Mockito for mocking services and dependencies
- ✅ MockMvc for testing REST controllers
- ✅ Security tests using @WithMockUser

Esempio:
```java
@MockBean
private CustomUserDetailsService userDetailsService;

@WithMockUser(username = "admin", roles = {"ADMIN"})
@Test
void testAdminAccess() throws Exception {
    mockMvc.perform(get("/api/test/admin"))
           .andExpect(status().isOk());
}

```

## Security

- ⚠️ Always change the `jwt.secret` in production
- ⚠️ Use HTTPS in production
- ⚠️ Configure CORS appropriately for your domain
- ⚠️ Consider implementing rate limiting
- ⚠️ Implement logout with token blacklisting if needed

## H2 Database Console

In development, you can access the H2 console:
- URL: `http://localhost:8080/api/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

## Contributing

1. Fork the project
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

