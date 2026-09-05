# Testly

An MCQ (multiple-choice) test application. Teachers create and publish timed tests —
including image-based questions and optional topic tags — and students attempt them
within a set window. Grading happens server-side; correct answers and tags are never
sent to the student until after they submit.

**Stack:** Spring Boot 3 (Java 17) · React 18 (Vite) · PostgreSQL

---
## 📸 Demo & Screenshots

### 1. User Registration & Role Selection
Users choose their primary role (**Teacher** or **Student**) at signup, which dynamically tailors their dashboard view and application permissions.
![Register Page](./screenshots/1.png)

### 2. Teacher Dashboard (First Login)
Clean initial empty state for educators prior to creating classrooms or publishing test modules.
![Teacher Dashboard Empty State](./screenshots/4.png)

### 3. Classroom Management & Join Codes
Teachers can organize students into dedicated classrooms, each auto-generating a unique short join code for student enrollment.
![Classrooms with Join Codes](./screenshots/2.png)

### 4. Classroom-Restricted Test Assignment
Tests can be targeted to a specific classroom or marked as **Open to every student** for maximum flexibility.
![Assigning Test to Classroom](./screenshots/8.png)

### 5. Text-Based Question Authoring
Intuitive question creator supporting rich text, multiple-choice options, and instant answer key designation.
![Managing Text-Based Questions](./screenshots/6.png)

### 6. Image Questions & Hidden Topic Tags
Questions support image attachments alongside custom topic tags. Tags remain hidden from students during the test attempt and are revealed only post-submission.
![Managing Image Questions with Tags](./screenshots/9.png)

### 7. Student Dashboard (Pre-Enrollment)
A clean student view prior to joining a classroom; only open-access tests are listed until a classroom join code is entered.
![Student Dashboard Before Joining](./screenshots/3.png)

### 8. Student Dashboard (Post-Enrollment)
After entering a join code, classroom-specific tests automatically populate on the student's active test feed.
![Student Dashboard After Joining](./screenshots/7.png)

### 9. Timed Test Attempt Interface
Distraction-free assessment screen featuring a live countdown timer with autosaving responses as students make selections.
![Taking a Test with Timer](./screenshots/11.png)

### 10. Instant Answer Review & Results
Post-submission view highlighting correct and incorrect selections only after the assessment is submitted.
![Result Page Answer Review](./screenshots/12.png)

### 11. Topic-Wise Analytics & Performance Breakdown
End-to-end proof of the tagging system: topic tags (e.g., *"division and classes"*, *"seed"*) remain hidden during the test and are aggregated here to provide detailed performance insights.
![Topic-Wise Performance Breakdown](./screenshots/14.png)

### 12. Teacher Test Overview
Comprehensive administrative view displaying test availability, question counts, and assigned classroom scopes.
![Teacher List of Tests](./screenshots/tests.png)

### 13. Per-Student Result Auditing
Teachers can inspect detailed individual submission scores, timestamps, and answer breakdowns for any given test.
![Teacher Viewing Student Result](./screenshots/16.png)
---

## Features

- **Auth**: JWT-based, role-based (TEACHER / STUDENT)
- **Teacher**: create tests, add MCQ questions (text and/or image, exactly 4 options),
  optionally tag questions by topic (zero, one, or many tags per question), publish/unpublish,
  view per-student results
- **Classrooms**: teachers create classrooms with a short join code; students join with that
  code; a test can optionally be restricted to one classroom, or left open to everyone
- **Student**: browse published tests open right now, attempt within a countdown timer,
  answers autosave as you go, server enforces the time limit independent of the client clock,
  view results afterward — including per-question topic tags and topic-wise score breakdown
  (tags are hidden during the attempt, revealed only in the post-submit result)
- **Images**: stored on local disk under `backend/uploads/`, served at `/uploads/...`; swap
  `FileStorageService` for an S3/Cloudinary client if you deploy this for real

---

## Project structure

```
testly/
├── backend/     Spring Boot API (Java 17, Maven)
├── frontend/    React app (Vite)
├── docs/        System requirements doc
└── screenshots/ Demo screenshots referenced above
```

---

## Prerequisites

- JDK 17+
- Maven (or use your IDE's built-in Maven)
- Node.js 18+ and npm
- PostgreSQL 14+ running locally (or a connection string to one)

---

## 1. Database setup

Create a database and (optionally) a dedicated user:

```sql
CREATE DATABASE testly;
```

The backend will create/update tables automatically on startup (`ddl-auto: update`
in `application.yml`) — no manual schema/migration step needed for local dev.

---

## 2. Backend setup

```bash
cd backend
```

Set environment variables (or edit `src/main/resources/application.yml` directly):

| Variable | Default | Purpose |
|---|---|---|
| `DB_USERNAME` | `postgres` | Postgres user |
| `DB_PASSWORD` | `postgres` | Postgres password |
| `JWT_SECRET` | (placeholder) | **Change this** — long random string, used to sign JWTs |
| `JWT_EXPIRATION_MS` | `86400000` (24h) | Token lifetime |
| `UPLOAD_DIR` | `uploads` | Where question/option images are stored |
| `CORS_ORIGINS` | `http://localhost:5173` | Allowed frontend origin(s), comma-separated |

Run it:

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

---

## 3. Frontend setup

```bash
cd frontend
npm install
npm run dev
```

The app starts on `http://localhost:5173` and proxies `/api` and `/uploads` requests
to `http://localhost:8080` (see `vite.config.js`).

---

## 4. Try it out

1. Register two accounts — one as **Teacher**, one as **Student**.
2. As the teacher: create a classroom (optional), create a test, add a few questions
   (try one with an image, and tag at least one question with a topic), then publish it.
   Set the test's open/close window to include right now.
3. As the student: join the classroom with its code (if the test is restricted to one),
   then start the test, answer, and submit (or let the timer run out).
4. Check the student's result page — tags and correct answers appear there, but were
   never visible during the attempt itself.
5. Back on the teacher side, open "View results" to see the student's score.

---

## Key design decisions

- **Grading is server-side only.** The `/tests/{id}/attempt` endpoint returns questions
  without a `correct` flag or `tags`; the answer key never reaches the browser during
  the attempt.
- **Timing is enforced server-side.** Each attempt's deadline is `min(startTime + duration, test.endTime)`,
  checked on every autosave and on submit (with a small grace window for network lag) —
  a manipulated client clock can't extend the test.
- **Tags are optional and normalized.** A `Tag` table plus a `question_tags` join table
  means the same topic name is reused across questions instead of being duplicated as
  free text, which is what makes topic-wise scoring in the result view possible.
- **Classrooms are optional and backward-compatible.** A `Test.classroom` foreign key is
  nullable — a test with no classroom stays open to every student, exactly as it worked
  before classrooms existed.
- **Images live outside Postgres.** Only the URL is stored in the DB; files are validated
  (type + 2MB size cap) and saved to disk via `FileStorageService`.

---

## Possible next steps

- Flyway/Liquibase migrations instead of `ddl-auto: update` for production
- Question/option shuffling per student
- Bulk question import (CSV)
- Cloud image storage (S3/Cloudinary) with resizing on upload
- Tab-switch / focus-loss detection during attempts