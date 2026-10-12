# SkillGap - Skill Gap Analysis and Career Alignment System

A Java full stack web application that shows students how their skills compare with
real career roles, what they are missing, and what to learn next.

## Features
- Student profile with skills rated 0 to 5
- Skill gap report with missing, weak and strong skills, a radar chart and a readiness score
- Career ranking using cosine similarity between skill vectors
- Personalised learning roadmap ordered by importance and prerequisites
- What-if simulator, progress timeline, XP, levels and badges
- Printable career report (save as PDF)
- Admin panel: manage skills, roles, skill weights and learning resources, and view gap analytics
- JWT authentication with role-based access (STUDENT, ADMIN)

## Tech stack
- Backend: Java, Spring Boot, Spring Data JPA / Hibernate, Spring Security + JWT
- Database: MySQL
- Frontend: React (Vite), React Router, Axios, Recharts

## How the intelligence works
- Readiness = sum(weight x min(have, need) / need) / sum(weight) x 100
- Alignment = cosine similarity between the student vector and the role vector,
  where the role vector value is requiredLevel x weight
- Roadmap = skills below the required level, ranked by weight x gap,
  with prerequisites placed first

## Run it locally
1. Create the database in MySQL:  CREATE DATABASE skillgap_db;
2. In backend/src/main/resources, copy application.properties.example to
   application.properties and fill in your MySQL password and port.

3. Start the backend (port 8081):
       cd backend
       .\mvnw.cmd spring-boot:run
4. Start the frontend (port 5173):
       cd frontend
       npm install
       npm run dev
5. Open http://localhost:5173

Sample data (24 skills, 5 career roles, learning links) and the admin account are
created automatically on first start.

## Default admin account (demo only)
- Email: admin@skillgap.com
- Password: Admin@123

Students register themselves on the Sign up tab.

## Project structure
    backend/   Spring Boot API (entity, repository, service, controller, security, config)
    frontend/  React app (pages, components)

## Tests
    cd backend
    .\mvnw.cmd test "-Dtest=EngineTest"
