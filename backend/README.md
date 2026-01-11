# Contact Backend (Spring Boot)

## Run locally (with Maven)
1. Configure SMTP in `src/main/resources/application.yml` or use env vars:
   - SPRING_MAIL_HOST, SPRING_MAIL_PORT, SPRING_MAIL_USERNAME, SPRING_MAIL_PASSWORD
2. Build: `./mvnw package`
3. Run: `java -jar target/contact-backend-0.0.1-SNAPSHOT.jar`
4. API: POST http://localhost:8080/api/contact  with JSON { name, email, message }

## Docker
Build:
  docker build -t contact-backend:latest .
Run:
  docker run -p 8080:8080 -e CONTACT_RECIPIENT='you@domain.com' -e SPRING_MAIL_HOST='smtp...' contact-backend:latest
