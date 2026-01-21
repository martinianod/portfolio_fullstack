# Portfolio Full-Stack Application with CRM

A modern, production-ready full-stack application combining a professional portfolio website with an integrated CRM system for lead management, client tracking, and project delivery.

## 🏗️ Architecture

### Tech Stack

**Frontend:**
- React 18 + Vite
- Tailwind CSS for styling
- React Router v6 for navigation
- Framer Motion for animations
- Axios for API communication
- i18next for internationalization

**Backend:**
- Java 21 + Spring Boot 3.2
- Spring Security + JWT for authentication
- Spring Data JPA with PostgreSQL
- Flyway for database migrations
- Spring Mail for email notifications
- Spring Actuator for health checks

**Infrastructure:**
- Docker & Docker Compose
- PostgreSQL 16
- Maven for backend build
- npm for frontend build

## 📁 Project Structure

```
portfolio_fullstack/
├── frontend/                 # React application
│   ├── src/
│   │   ├── admin/           # Admin CRM interface
│   │   │   ├── components/  # Reusable admin components
│   │   │   ├── contexts/    # Auth context
│   │   │   ├── layout/      # Admin layout with sidebar
│   │   │   ├── pages/       # Dashboard, Leads, Clients, Projects
│   │   │   └── services/    # API services for admin
│   │   ├── components/      # Public site components
│   │   ├── features/        # Public site feature modules
│   │   ├── services/        # Public API services
│   │   └── main.jsx         # App entry point with routing
│   └── package.json
│
├── backend/                 # Spring Boot application
│   ├── src/main/java/com/martiniano/crm/
│   │   ├── config/          # Security, CORS config
│   │   ├── controller/      # REST API controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # Data repositories
│   │   ├── security/        # JWT utilities, filters
│   │   └── service/         # Business logic
│   ├── src/main/resources/
│   │   ├── db/migration/    # Flyway SQL migrations
│   │   └── application.yml  # Configuration
│   └── pom.xml
│
└── docker-compose.yml       # Multi-container orchestration
```

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose (recommended)
- OR: Java 21, Maven, Node 20+, PostgreSQL 16

### Option 1: Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   git clone https://github.com/martinianod/portfolio_fullstack.git
   cd portfolio_fullstack
   ```

2. **Configure environment variables**
   ```bash
   # Backend configuration
   cp backend/.env.example backend/.env
   
   # Frontend configuration
   cp frontend/.env.example frontend/.env
   ```

3. **Start all services**
   ```bash
   docker-compose up -d
   ```

4. **Access the applications**
   - Public Portfolio: http://localhost:5173
   - Admin CRM: http://localhost:5173/admin/login
   - Backend API: http://localhost:8080
   - Health Check: http://localhost:8080/actuator/health

### Option 2: Manual Setup

#### Backend Setup

1. **Configure PostgreSQL**
   ```bash
   # Create database
   psql -U postgres
   CREATE DATABASE portfolio_crm;
   CREATE USER portfolio WITH PASSWORD 'portfolio_dev';
   GRANT ALL PRIVILEGES ON DATABASE portfolio_crm TO portfolio;
   ```

2. **Configure environment**
   ```bash
   cd backend
   cp .env.example .env
   # Edit .env with your database credentials
   ```

3. **Run backend**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

#### Frontend Setup

1. **Install dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env to point to your backend (default: http://localhost:8080)
   ```

3. **Run frontend**
   ```bash
   npm run dev
   ```

## 🔐 Default Admin Credentials

**⚠️ CHANGE IN PRODUCTION!**

- **Email:** `admin@martiniano.dev` (use this for login)
- **Password:** `admin123`
- **Role:** ADMIN

> **Important:** The API accepts **email** (not username) in the login request. Use `{"email":"admin@martiniano.dev","password":"admin123"}` format.

## 🔍 Troubleshooting

### Backend Health Check Shows DOWN

1. **Check database connection**
   ```bash
   # Verify PostgreSQL is running
   docker-compose ps postgres
   
   # Check health details with components
   curl http://localhost:8080/actuator/health
   ```

2. **Verify environment variables**
   - Ensure `DATABASE_URL`, `DATABASE_USER`, and `DATABASE_PASSWORD` are correct
   - Check that database exists and migrations ran successfully

3. **Mail service issues**
   - The mail health check is disabled by default in development
   - If enabled, ensure SMTP credentials are correct

4. **Check application logs**
   ```bash
   docker-compose logs backend | tail -100
   ```

### Login Returns 400 Bad Request

1. **IMPORTANT: Use email field, not username**
   ```bash
   # ✅ CORRECT - API expects email field
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@martiniano.dev","password":"admin123"}'
   
   # ❌ WRONG - This will return validation error
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'
   ```

2. **Check CORS configuration**
   - Ensure `CORS_ORIGINS` includes your frontend URL
   - Default: `http://localhost:5173,http://localhost:3000`

3. **Verify credentials**
   - Email: `admin@martiniano.dev` (required - use email, not username)
   - Password: `admin123` (case-sensitive)

### Login Returns 401 Unauthorized

1. **Password incorrect**
   - Ensure you're using exactly `admin123` (case-sensitive)
   - BCrypt hash in database must match

2. **User not found**
   - DataInitializer creates admin user automatically on startup
   - Check logs for "Creating default admin user" message
   
3. **Verify user exists in database**
   ```bash
   docker-compose exec postgres psql -U portfolio -d portfolio_crm \
     -c "SELECT username, email, role, enabled FROM users WHERE email='admin@martiniano.dev';"
   ```

4. **Force recreate database if needed**
   ```bash
   docker-compose down -v
   docker-compose up -d
   # Wait for backend to start and check logs
   docker-compose logs backend | grep "admin"
   ```

### Frontend Cannot Connect to Backend

1. **Check API URL configuration**
   ```bash
   # In frontend/.env
   VITE_API_URL=http://localhost:8080
   ```

2. **Verify backend is running and healthy**
   ```bash
   curl http://localhost:8080/actuator/health
   # Should return: {"status":"UP",...}
   ```

3. **Check browser console for errors**
   - CORS errors indicate backend CORS configuration issue
   - Network errors indicate backend is not accessible
   - Check Network tab in browser DevTools

### Database Migration Fails

1. **Clean database and retry**
   ```bash
   docker-compose down -v  # -v removes volumes
   docker-compose up -d
   ```

2. **Check migration files**
   - Located in `backend/src/main/resources/db/migration/`
   - V1__initial_schema.sql creates tables
   - V2__seed_data.sql creates admin user (with ON CONFLICT for idempotency)

3. **Check migration status**
   ```bash
   docker-compose exec postgres psql -U portfolio -d portfolio_crm \
     -c "SELECT version, description, installed_on, success FROM flyway_schema_history;"
   ```

### Verification Checklist

After starting the application with `docker-compose up -d`, verify:

- [ ] **PostgreSQL is running and healthy**
  ```bash
  docker-compose ps postgres
  # Should show: Up (healthy)
  ```

- [ ] **Backend health is UP**
  ```bash
  curl http://localhost:8080/actuator/health
  # Should return: {"status":"UP","components":{"db":{"status":"UP"},...}}
  ```

- [ ] **Admin user exists**
  ```bash
  docker-compose logs backend | grep -i "admin"
  # Should see: "Admin user already exists" or "Default admin user created"
  ```

- [ ] **Backend responds to login (use email field)**
  ```bash
  curl -X POST http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@martiniano.dev","password":"admin123"}'
  # Should return: {"token":"eyJ...","username":"admin","email":"admin@martiniano.dev","role":"ADMIN"}
  ```

- [ ] **Frontend is accessible**
  ```bash
  curl -I http://localhost:5173
  # Should return: HTTP/1.1 200 OK
  ```

- [ ] **Admin login works from UI**
  - Open `http://localhost:5173/admin/login`
  - Enter Email: `admin@martiniano.dev`
  - Enter Password: `admin123`
  - Click Login
  - Should redirect to dashboard

- [ ] **Dashboard loads with data**
  - After login, verify dashboard shows KPIs
  - Navigate to Leads, Clients, Projects pages

### Common Fixes

| Issue | Solution |
|-------|----------|
| "version is obsolete" warning | ✅ Fixed - removed `version:` from docker-compose.yml |
| Validation error "username is required" | ✅ Fixed - API now uses `email` field |
| Authentication failed despite correct credentials | ✅ Fixed - BCrypt hash updated, DataInitializer ensures admin exists |
| Health shows DOWN | Check specific component in health details, verify DB connection |

## 📚 API Documentation

### Public Endpoints

- `POST /api/v1/leads/public` - Submit contact form (no auth required)

### Admin Endpoints (Requires JWT Token)

#### Authentication
- `POST /api/v1/auth/login` - Admin login
- `GET /api/v1/auth/me` - Verify token

#### Dashboard
- `GET /api/v1/dashboard/kpis` - Get dashboard metrics

#### Leads Management
- `GET /api/v1/leads` - List all leads (with pagination, search, filters)
- `GET /api/v1/leads/{id}` - Get lead details
- `PUT /api/v1/leads/{id}` - Update lead
- `PATCH /api/v1/leads/{id}/stage` - Update lead stage
- `DELETE /api/v1/leads/{id}` - Delete lead
- `GET /api/v1/leads/stats` - Get lead statistics by stage

#### Clients Management
- `GET /api/v1/clients` - List all clients
- `POST /api/v1/clients` - Create client
- `GET /api/v1/clients/{id}` - Get client details
- `PUT /api/v1/clients/{id}` - Update client
- `DELETE /api/v1/clients/{id}` - Delete client

#### Projects Management
- `GET /api/v1/projects` - List all projects
- `POST /api/v1/projects` - Create project
- `GET /api/v1/projects/{id}` - Get project details
- `GET /api/v1/projects/client/{clientId}` - Get projects by client
- `PUT /api/v1/projects/{id}` - Update project
- `DELETE /api/v1/projects/{id}` - Delete project

#### Activities (Timeline)
- `GET /api/v1/activities/{entityType}/{entityId}` - Get activity timeline
- `POST /api/v1/activities` - Create activity

## 🗄️ Database Schema

### Core Entities

- **Users** - Admin users with authentication
- **Leads** - Incoming contacts with pipeline stages
- **Clients** - Converted leads or existing clients
- **Projects** - Client projects with status tracking
- **Milestones** - Project milestones
- **Tasks** - Project tasks
- **Activities** - Audit log / timeline
- **Reminders** - Follow-up reminders

### Lead Pipeline Stages

NEW → CONTACTED → QUALIFIED → PROPOSAL → NEGOTIATION → WON/LOST

## 🎨 Features

### Public Portfolio Site

- ✅ Modern, responsive design (mobile-first)
- ✅ Dark mode support
- ✅ Multi-language support
- ✅ Portfolio/Projects showcase
- ✅ Enhanced contact form with budget, project type, phone/WhatsApp
- ✅ WhatsApp direct contact button
- ✅ Anti-spam protection (honeypot)
- ✅ Email notifications

### Admin CRM

- ✅ JWT-based authentication
- ✅ Dashboard with KPIs
- ✅ Leads Management with pipeline
- ✅ Clients Management
- ✅ Projects Management
- ✅ Activity Timeline
- ✅ Responsive design with dark mode

## 🔧 Configuration

See `.env.example` files in `backend/` and `frontend/` directories for all configuration options.

## 📦 Building for Production

### Backend

```bash
cd backend
mvn clean package
```

### Frontend

```bash
cd frontend
npm run build
```

## 🚢 Deployment

**Recommended hosting:**
- Frontend: Vercel, Netlify
- Backend: Railway, Render, Fly.io
- Database: Railway PostgreSQL, AWS RDS

## 🔒 Security

- ⚠️ **IMPORTANT:** Change default admin password
- ⚠️ **IMPORTANT:** Use strong JWT secret in production
- ⚠️ **IMPORTANT:** Enable HTTPS in production

## 📝 Next Steps

See complete list of future enhancements in the full documentation above.

## 👤 Author

**Martiniano D'Ambrosio**
- Email: contacto@martiniano.dev

---

Built with ❤️ using React, Spring Boot, and PostgreSQL
