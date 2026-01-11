# Fullstack Landing + Contact Backend

This archive contains:
- frontend/: Vite + React + Tailwind landing page (ready to run)
- backend/: Spring Boot contact backend (Maven + Dockerfile)

## Quick start (frontend)
cd frontend
npm ci
npm run dev

## Quick start (backend)
cd backend
./mvnw package
java -jar target/contact-backend-0.0.1-SNAPSHOT.jar

## Docker (quick)
# frontend
docker build -t martiniano-landing:latest ./frontend
docker run -p 8080:80 martiniano-landing:latest

# backend
docker build -t contact-backend:latest ./backend
docker run -p 8080:8080 -e CONTACT_RECIPIENT='you@domain.com' -e SPRING_MAIL_HOST='smtp...' contact-backend:latest
