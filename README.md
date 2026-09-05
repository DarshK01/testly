# Testly

An MCQ (multiple-choice) test application. Teachers create and publish timed tests —
including image-based questions and optional topic tags — and students attempt them
within a set window. Grading happens server-side; correct answers and tags are never
sent to the student until after they submit.

**Stack:** Spring Boot 3 (Java 17) · React 18 (Vite) · PostgreSQL

---

## Screenshots

<table>
<tr>
<td width="50%">

**Register**
<br/>Pick a role — Teacher or Student — at signup.
<br/><img src="screenshots/1.png" alt="Register page" width="100%"/>

</td>
<td width="50%">

**Teacher dashboard (first login)**
<br/>Empty state before any tests exist.
<br/><img src="screenshots/4.png" alt="Teacher dashboard empty state" width="100%"/>

</td>
</tr>
<tr>
<td width="50%">

**Classrooms with join codes**
<br/>Each classroom gets a short code students use to join.
<br/><img src="screenshots/2.png" alt="Teacher classrooms with join codes" width="100%"/>

</td>
<td width="50%">

**Restricting a test to a classroom**
<br/>Optional — leave as "Open to every student" to keep old behavior.
<br/><img src="screenshots/8.png" alt="Assigning a test to a classroom" width="100%"/>

</td>
</tr>
<tr>
<td width="50%">

**Managing questions — text-based**
<br/>Add options, mark the correct one, no tags required.
<br/><img src="screenshots/6.png" alt="Managing text-based questions" width="100%"/>

</td>
<td width="50%">

**Managing questions — image-based + tags**
<br/>Questions can be an image, with optional topic tags (hidden from students until after they submit).
<br/><img src="screenshots/9.png" alt="Managing image-based questions with tags" width="100%"/>

</td>
</tr>
<tr>
<td width="50%">

**Student — before joining a classroom**
<br/>Nothing shows up until a join code is entered (or a test is open to everyone).
<br/><img src="screenshots/3.png" alt="Student dashboard before joining a classroom" width="100%"/>

</td>
<td width="50%">

**Student — after joining**
<br/>Classroom-restricted tests appear once the student has joined.
<br/><img src="screenshots/7.png" alt="Student dashboard after joining a classroom" width="100%"/>

</td>
</tr>
<tr>
<td width="50%">

**Taking a test**
<br/>Countdown timer top-right; answers autosave as options are selected.
<br/><img src="screenshots/11.png" alt="Taking a test with a live timer" width="100%"/>

</td>
<td width="50%">

**Result — score & review**
<br/>Correct/incorrect coloring appears only after submission.
<br/><img src="screenshots/12.png" alt="Result page showing score and answer review" width="100%"/>

</td>
</tr>
<tr>
<td width="50%" colspan="2">

**Result — topic-wise performance breakdown**
<br/>This is the screenshot that proves the tagging feature end-to-end: tags (e.g. "division and classes", "seed") were completely invisible during the attempt, and only appear here, in the post-submit result, aggregated per topic.
<br/><img src="screenshots/14.png" alt="Result page with topic-wise score breakdown" width="70%"/>

</td>
</tr>
<tr>
<td width="50%">

**Teacher's test list**
<br/>Each test shows its classroom restriction (or "open to everyone"), question count, and status.
<br/><img src="screenshots/tests.png" alt="Teacher's list of tests with classroom labels" width="100%"/>

</td>
<td width="50%">

**Teacher viewing a student's result**
<br/>Per-student score and submission timestamp for a given test.
<br/><img src="screenshots/16.png" alt="Teacher viewing a student's result" width="100%"/>

</td>
</tr>
</table>

<sub>Additional screenshots not shown above: `5.png`, `10.png`, `13.png`, `15.png` — same flows for a second demo student/classroom.</sub>

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