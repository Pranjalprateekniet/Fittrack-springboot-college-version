# FitTrack V2 — Spring Boot MVC & Thymeleaf Web Application

FitTrack V2 is a full-stack fitness, goal-tracking, workout, and nutrition management web application served directly by Spring Boot at `http://localhost:8080`.

## Technology Stack
- **Backend**: Java 17, Spring Boot 4.0.7, Spring MVC, Spring Security (Session + CSRF for browser pages, Stateless JWT + Refresh Tokens for `/api/**`), Spring Data JPA / Hibernate, Lombok
- **Frontend**: Thymeleaf, HTML5, CSS3, Vanilla JavaScript (served directly by Spring Boot — no Node.js/React/Expo server required)
- **Database**: MySQL (`fittrackv2`), H2 in-memory database for automated tests

## Architecture
- `com.pranjal.fittrackv2.web.controller` — Spring MVC controllers serving Thymeleaf views (`/login`, `/register`, `/dashboard`, `/goals`, `/workouts`, `/nutrition`, `/food-log`, `/profile`)
- `com.pranjal.fittrackv2.controller` & `goals` & `nutrition.controller` — REST API endpoints (`/api/**`)
- `src/main/resources/templates/` — Thymeleaf HTML templates
- `src/main/resources/static/` — CSS stylesheets (`style.css`, `auth.css`, `dashboard.css`) and `js/app.js`

## Database Setup (MySQL)
```sql
CREATE DATABASE IF NOT EXISTS fittrackv2;
```

## Environment Configuration
Copy `.env.example` to `.env` or export the environment variables in your shell:
```bash
cd fittrackv2
cp .env.example .env
export DB_PASSWORD="your_mysql_password_here"
export JWT_SECRET="your_256_bit_jwt_secret_key_here_at_least_32_characters"
```

## Startup Instructions (Single Command)
```bash
cd fittrackv2
./mvnw spring-boot:run
```
Open the website in your browser:
```text
http://localhost:8080
```
On first startup, the application automatically loads 1,324 food nutrition records and serving presets from `src/main/resources/data/`.

## Testing Instructions
Run the full automated test suite (REST API + Thymeleaf Web MVC integration tests using H2):
```bash
cd fittrackv2
./mvnw test
```
