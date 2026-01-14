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

- **Username:** `admin`
- **Password:** `admin123`
- **Email:** `admin@martiniano.dev`

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
