# System Requirements Specification
## MCQ Test Application (Teacher–Student Portal)

---

## 1. Introduction

### 1.1 Purpose
This document specifies the functional and non-functional requirements for a web-based Multiple Choice Question (MCQ) test application. Teachers can create, publish, and manage tests (including image-based questions); students can attempt tests within a time window and view results.

### 1.2 Scope
The system consists of:
- A **Spring Boot** backend exposing REST APIs
- A **React** frontend (separate Teacher and Student views)
- A **PostgreSQL** database
- File/image storage for question and option images

### 1.3 Intended Audience
Developers, project evaluators, and maintainers of the system.

---

## 2. Overall Description

### 2.1 User Roles
| Role | Description |
|---|---|
| Teacher | Creates/edits/publishes tests, uploads questions (text/image), views results and analytics |
| Student | Views available tests, attempts tests within the allowed window, views own results/history |
| Admin (optional) | Manages user accounts, resets passwords, monitors system |

### 2.2 Assumptions & Constraints
- Users have a stable internet connection during test attempts.
- Each student attempts a given test only once, unless explicitly configured otherwise.
- Client-side timers are advisory only; the server is the source of truth for timing.
- Correct answers are never sent to the client during an active attempt.

---

## 3. Functional Requirements

### 3.1 Authentication & Authorization
- FR1: Users register/log in with email and password.
- FR2: Passwords are hashed (BCrypt) before storage.
- FR3: JWT issued on login; used to authorize subsequent requests.
- FR4: Role-based access control — Teacher-only endpoints (create/edit/publish/results) vs Student-only endpoints (attempt/submit).

### 3.2 Test Management (Teacher)
- FR5: Teacher can create a test with title, description, duration, start time, end time.
- FR6: Teacher can add questions to a test, each with:
  - Question text (optional if image provided)
  - Question image (optional, JPG/PNG, max size enforced)
  - 4 options (text and/or image), one marked correct
  - Marks per question
- FR7: Teacher can edit/delete a question before publishing.
- FR8: Teacher can publish/unpublish a test. Published tests become visible to students within the configured time window.
- FR9: Teacher can view a list of all attempts, per-student scores, and aggregate analytics (average score, score distribution) for a test.

### 3.3 Test Attempt (Student)
- FR10: Student can view a list of published tests currently within their attempt window.
- FR11: Student can start an attempt; server records start time and returns questions **without correct answers**.
- FR12: Student selects one option per question; answers are auto-saved periodically (partial submission).
- FR13: Student can submit the test manually, or it is auto-submitted when the duration expires.
- FR14: Server validates submission time against `start_time + duration` before accepting (prevents client clock manipulation).
- FR15: Server grades the attempt on submission and stores the score.
- FR16: Student can view their own past results and a breakdown of correct/incorrect answers (if the teacher allows review).

### 3.4 Image Handling
- FR17: Images are uploaded via multipart/form-data, validated for type and size server-side.
- FR18: Images are stored outside the database (local disk or cloud storage); only the URL/path is persisted in PostgreSQL.
- FR19: Images are resized/compressed on upload to a reasonable max dimension.

### 3.5 Question Tagging (Topics)
- FR20: While creating/editing a question, the teacher may **optionally** assign one or more topic tags (e.g. "Arrays", "OOP", "DBMS") — zero, one, or multiple tags per question are all valid.
- FR21: Tags are not shown to the student during the test attempt (FR11's "questions without correct answers" also excludes tags).
- FR22: Tags become visible to the student **only after submission**, alongside that question in the result/review screen — e.g. "You scored 3/5 in Arrays".
- FR23: Teacher-side analytics may additionally use tags to show topic-wise class performance (e.g. average score per tag) — optional, not required for v1.
- FR24: Tags are reusable across questions/tests (a normalized tag list, not free text duplicated per question) so topic-wise stats stay consistent.

---

## 4. Non-Functional Requirements

| Category | Requirement |
|---|---|
| Performance | Test list and question fetch should respond within 1–2 seconds under normal load |
| Scalability | Backend should support concurrent test attempts (target: 100+ simultaneous students for a college-scale deployment) |
| Security | JWT-based auth, HTTPS in production, correct answers never exposed to client during attempt, input validation on all endpoints |
| Reliability | Auto-save of answers to prevent data loss on refresh/network drop |
| Usability | Responsive UI (desktop + mobile browser), clear countdown timer, accessible forms |
| Maintainability | Layered architecture (Controller–Service–Repository), documented REST API |
| Availability | Target uptime suitable for scheduled exam windows; no deploys during active test windows |

---

## 5. System Architecture Overview

```
[React Frontend]  <-- REST/JSON + JWT -->  [Spring Boot Backend]  <-->  [PostgreSQL]
                                                    |
                                                    v
                                          [Image Storage: local/S3/Cloudinary]
```

- **Frontend**: React, Axios/React Query for API calls, React Router, Context/Zustand for auth state.
- **Backend**: Spring Boot, Spring Security (JWT), Spring Data JPA, Bean Validation.
- **Database**: PostgreSQL (relational schema — Users, Tests, Questions, TestAttempts, Answers).
- **File storage**: Local disk (dev) or cloud object storage (production).

---

## 6. Hardware & Software Requirements

### 6.1 Development Environment
| Component | Requirement |
|---|---|
| JDK | Java 17+ |
| Build tool | Maven or Gradle |
| Node.js | v18+ |
| Package manager | npm or yarn |
| Database | PostgreSQL 14+ |
| IDE | IntelliJ IDEA / VS Code |

### 6.2 Minimum System Requirements (Server)
| Component | Requirement |
|---|---|
| RAM | 2 GB minimum (4 GB+ recommended) |
| Storage | 10 GB+ (more if storing images locally) |
| OS | Linux (Ubuntu recommended) / Windows / macOS |

### 6.3 Client Requirements
| Component | Requirement |
|---|---|
| Browser | Latest Chrome, Firefox, or Edge |
| Internet | Stable connection during test attempts |

---

## 7. Database Entities (Summary)

- **User**: id, name, email, password_hash, role
- **Test**: id, title, description, teacher_id, duration_minutes, start_time, end_time, is_published
- **Question**: id, test_id, question_text, question_image_url, marks
- **Option**: id, question_id, option_text, option_image_url, is_correct
- **Tag**: id, name (unique) — e.g. "Arrays", "OOP", "DBMS"
- **QuestionTag**: question_id, tag_id (many-to-many join table; a question can have 0, 1, or many tags)
- **TestAttempt**: id, student_id, test_id, start_time, submitted_time, score
- **Answer**: id, attempt_id, question_id, selected_option_id

---

## 8. Future Enhancements (Out of Scope for v1)
- Question randomization/shuffling per student
- Multiple question types (MSQ, fill-in-the-blank)
- Negative marking configuration
- Bulk question upload via CSV/Excel
- Plagiarism/tab-switch detection during attempts
