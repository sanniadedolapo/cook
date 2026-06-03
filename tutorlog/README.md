# TutorLog — Spring Boot Backend

Subscription-based tutor management platform. Built with Spring Boot 3, MongoDB, and JWT auth.

## Stack
- **Java 17** + Spring Boot 3.2
- **MongoDB** via Spring Data
- **Spring Security** + JWT (jjwt 0.11)
- **Lombok** for boilerplate reduction
- **Maven** build

---

## Getting started

### Prerequisites
- Java 17+
- MongoDB running on `localhost:27017`
- Maven 3.8+

### Run
```bash
# Standard run
./mvnw spring-boot:run

# With demo data seeded (dev profile)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Server starts on **http://localhost:8080**

---

## Project structure

```
src/main/java/com/tutorlog/
├── TutorLogApplication.java        # Entry point (@EnableScheduling)
├── config/
│   ├── SecurityConfig.java         # JWT filter chain, CORS, role rules
│   ├── GlobalExceptionHandler.java # Unified error responses
│   └── DataSeeder.java             # Dev-only seed data
├── controller/
│   ├── AuthController.java         # /api/auth
│   ├── LessonController.java       # /api/lessons
│   ├── SubscriptionController.java # /api/subscriptions
│   └── UserController.java         # /api/users
├── dto/
│   ├── AuthDto.java
│   ├── LessonDto.java
│   └── SubscriptionDto.java
├── model/
│   ├── User.java                   # users collection
│   ├── Lesson.java                 # lessons collection
│   └── Subscription.java          # subscriptions collection
├── repository/
│   ├── UserRepository.java
│   ├── LessonRepository.java
│   └── SubscriptionRepository.java
├── security/
│   ├── JwtUtils.java               # Token generation & validation
│   ├── JwtAuthFilter.java          # Per-request JWT extraction
│   └── UserDetailsServiceImpl.java
└── service/
    ├── AuthService.java
    ├── LessonService.java
    ├── SubscriptionService.java    # @Scheduled expiry job lives here
    └── UserService.java
```

---

## API Reference

All protected endpoints require:
```
Authorization: Bearer <token>
```

---

### Auth  `/api/auth`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | None | Register new user |
| POST | `/api/auth/login` | None | Login and get token |

**Register**
```json
POST /api/auth/register
{
  "name": "Alex Johnson",
  "email": "alex@example.com",
  "password": "mypassword",
  "role": "student"
}
```
Role must be `"student"` or `"tutor"`.

**Login**
```json
POST /api/auth/login
{
  "email": "alex@example.com",
  "password": "mypassword"
}
```

Both return:
```json
{
  "token": "eyJhbGci...",
  "id": "64a1b...",
  "name": "Alex Johnson",
  "email": "alex@example.com",
  "role": "student"
}
```

---

### Lessons  `/api/lessons`

| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| GET | `/api/lessons` | Required | Any | All lessons (summary) |
| GET | `/api/lessons/free` | None | Any | Free lessons only |
| GET | `/api/lessons/search?q=` | Required | Any | Search by title/subject |
| GET | `/api/lessons/tutor/{tutorId}` | Required | Any | Lessons by tutor |
| GET | `/api/lessons/{id}` | Required | Any* | Full lesson content |
| POST | `/api/lessons` | Required | Tutor | Create lesson |
| PUT | `/api/lessons/{id}` | Required | Tutor (owner) | Update lesson |
| DELETE | `/api/lessons/{id}` | Required | Tutor (owner) | Delete lesson |
| POST | `/api/lessons/{id}/rate` | Required | Any | Rate a lesson |

*Premium lessons: active subscription required (or tutor role).

**Create lesson**
```json
POST /api/lessons
{
  "title": "Advanced Calculus",
  "subject": "Mathematics",
  "description": "Deep dive into integration techniques.",
  "content": "<h3>Integration by Parts</h3><p>...</p>",
  "durationMinutes": 55,
  "accessType": "PREMIUM"
}
```
`accessType` must be `"FREE"` or `"PREMIUM"`.

**Rate a lesson**
```json
POST /api/lessons/{id}/rate
{ "stars": 5 }
```

---

### Subscriptions  `/api/subscriptions`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/subscriptions/current` | Required | Current active subscription (204 if none) |
| GET | `/api/subscriptions/history` | Required | All subscriptions for current user |
| POST | `/api/subscriptions` | Required | Subscribe to a plan |
| DELETE | `/api/subscriptions/cancel` | Required | Cancel active subscription |

**Subscribe**
```json
POST /api/subscriptions
{ "plan": "PRO" }
```
Plan options: `"BASIC"` (monthly), `"PRO"` (monthly), `"ANNUAL_PRO"` (yearly).

Subscribing while already subscribed automatically cancels the old plan first.

---

### Users  `/api/users`

| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| GET | `/api/users/me` | Required | Any | Own profile |
| PUT | `/api/users/me` | Required | Any | Update own profile |
| GET | `/api/users/students` | Required | Tutor/Admin | All students |
| GET | `/api/users/tutors` | Required | Any | All tutors |

**Update profile**
```json
PUT /api/users/me
{
  "name": "New Name",
  "bio": "Updated bio",
  "password": "newpassword"
}
```
All fields are optional.

---

## Subscription expiry

`SubscriptionService` runs a scheduled job every day at midnight:

```java
@Scheduled(cron = "0 0 0 * * *")
public void expireSubscriptions() { ... }
```

It finds all `ACTIVE` subscriptions where `expiryDate < today` and sets their status to `EXPIRED`. Any subsequent request to a premium lesson will then be rejected for that user.

---

## Error responses

All errors follow this format:
```json
{
  "status": 400,
  "message": "Email is already registered",
  "timestamp": "2024-06-01T12:00:00"
}
```

Validation errors include a `details` map:
```json
{
  "status": 400,
  "message": "Validation failed",
  "details": {
    "email": "Invalid email format",
    "password": "Password must be at least 6 characters"
  }
}
```

---

## Connecting to the HTML frontend

In the frontend's JS, replace the `DB` object calls with `fetch`:

```javascript
// Login
const res = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});
const { token, role } = await res.json();
localStorage.setItem('token', token);

// Authenticated request
const lessons = await fetch('http://localhost:8080/api/lessons', {
  headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
}).then(r => r.json());
```
