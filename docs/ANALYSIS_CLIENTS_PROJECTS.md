# ANÁLISIS: TRANSFORMACIÓN A PLATAFORMA DE GESTIÓN DE CLIENTES + PROYECTOS + GITHUB INTEGRATION

**Fecha**: 2026-01-27  
**Autor**: Staff Engineer / Tech Lead  
**Repositorio**: martinianod/portfolio_fullstack

---

## EXECUTIVE SUMMARY

Este documento analiza el estado actual del sistema de backoffice/dashboard de portfolio_fullstack y define la arquitectura target para transformarlo en una plataforma profesional de gestión de clientes (CRM) + gestión de proyectos + integración automática con GitHub.

**Objetivo de Negocio**:
- Gestionar clientes (accounts) con múltiples proyectos simultáneos
- Cada proyecto puede crear automáticamente un repositorio GitHub para versionado
- Soportar "Mis proyectos" internos (no asociados a cliente)
- Customización por cliente via configuración (feature flags, custom fields, workflows)

---

## 1. ESTADO ACTUAL DEL SISTEMA

### 1.1 Flujo Actual: Landing Page → Lead → Backoffice

```
┌─────────────────┐
│  Landing Page   │
│  (Public)       │
│                 │
│ - Hero          │
│ - Projects      │
│ - Skills        │
│ - Education     │
│ - Contact Form  │──┐
└─────────────────┘  │
                     │ POST /api/v1/leads/public
                     │ {name, email, phone, company,
                     │  budgetRange, projectType, message}
                     ▼
              ┌──────────────────┐
              │  LeadController  │
              │  (Public)        │
              └─────────┬────────┘
                        │
                        ▼
              ┌──────────────────┐
              │   LeadService    │
              │  - createLead()  │
              │  - stage: NEW    │
              │  - priority: MED │
              └─────────┬────────┘
                        │
                        ├─────────────────┐
                        │                 │
                        ▼                 ▼
              ┌──────────────┐  ┌──────────────────┐
              │  PostgreSQL  │  │  EmailService    │
              │  leads table │  │  (notification)  │
              └──────────────┘  └──────────────────┘
                        │
                        │ Activity log
                        ▼
              ┌──────────────────┐
              │ activities table │
              │ (audit trail)    │
              └──────────────────┘

Admin accede vía:
/admin/login → /admin/leads
- Lista de leads con filtros (stage, priority, search)
- Detalle de lead (edición, cambio de stage, asignación)
```

**Endpoints Actuales (Lead)**:
- `POST /api/v1/leads/public` → Crear lead (público, sin auth)
- `GET /api/v1/leads` → Listar leads (admin)
- `GET /api/v1/leads/{id}` → Detalle lead
- `PUT /api/v1/leads/{id}` → Actualizar lead
- `PATCH /api/v1/leads/{id}/stage` → Cambiar stage
- `DELETE /api/v1/leads/{id}` → Eliminar lead
- `GET /api/v1/leads/stats` → Estadísticas de leads

**Payload Creación Lead**:
```json
{
  "name": "string (required)",
  "email": "string (required, email format)",
  "phone": "string (optional)",
  "company": "string (optional)",
  "budgetRange": "string (optional)",
  "projectType": "string (optional)",
  "message": "string (required, min 10 chars)",
  "source": "string (default: web)"
}
```

**Almacenamiento**:
- Tabla `leads` con campos: id, name, email, phone, company, budget_range, project_type, message, source, stage (NEW/CONTACTED/QUALIFIED/PROPOSAL/NEGOTIATION/WON/LOST), priority, assigned_to (FK users), created_at, updated_at
- Actividades registradas en tabla `activities` con payload JSONB

### 1.2 Entidades Existentes

| Entidad | Tabla | Propósito | Relaciones |
|---------|-------|-----------|------------|
| **User** | `users` | Autenticación de admins | 1-to-many → leads.assigned_to |
| **Lead** | `leads` | Contactos desde landing | FK: assigned_to → users |
| **Client** | `clients` | Clientes convertidos | FK: created_from_lead_id → leads |
| **Project** | `projects` | Proyectos de cliente | FK: client_id → clients (CASCADE) |
| **Milestone** | `milestones` | Hitos de proyecto | FK: project_id → projects (CASCADE) |
| **Task** | `tasks` | Tareas de proyecto/milestone | FK: project_id, milestone_id |
| **Activity** | `activities` | Audit log genérico | entity_type + entity_id (polymorphic) |
| **Reminder** | `reminders` | Recordatorios | entity_type + entity_id (polymorphic) |

**Campos Clave Actuales**:

**Lead**:
- stage: NEW, CONTACTED, QUALIFIED, PROPOSAL, NEGOTIATION, WON, LOST
- priority: LOW, MEDIUM, HIGH
- assigned_to: FK to User

**Client**:
- status: ACTIVE, INACTIVE
- created_from_lead_id: FK to Lead (conversión)
- tags: VARCHAR(255) - delimited list

**Project**:
- client_id: FK to Client (NOT NULL, CASCADE DELETE)
- status: DISCOVERY, PLANNING, IN_PROGRESS, COMPLETED
- repo_link, deploy_link: VARCHAR(500)
- estimated_hours, actual_hours, budget_amount: DECIMAL

**Observaciones**:
- ✅ Ya existe relación Lead → Client → Project
- ❌ Project siempre requiere client_id (no soporta proyectos internos)
- ❌ No hay custom_fields (JSONB) para extensibilidad
- ❌ No hay metadata de repositorio GitHub (url, default_branch, created_at, status)
- ❌ No hay entidad Contact separada (Client tiene un solo contacto primario)
- ❌ No hay soporte para multi-tenancy (tenant_id)

### 1.3 Arquitectura Actual

**Backend (Spring Boot 3.2.0 + Java 21)**:
```
src/main/java/com/martiniano/crm/
├── config/
│   ├── SecurityConfig.java       → JWT + CORS + RBAC
│   ├── GlobalExceptionHandler    → Manejo de errores centralizado
│   └── DataInitializer           → Seed admin user
├── controller/
│   ├── AuthController            → POST /auth/login, GET /auth/me
│   ├── LeadController            → CRUD leads
│   ├── ClientController          → CRUD clients
│   ├── ProjectController         → CRUD projects
│   ├── ActivityController        → Audit logs
│   └── DashboardController       → GET /dashboard/kpis
├── service/
│   ├── AuthService               → JWT generation, validation
│   ├── LeadService               → Business logic + activity logging
│   ├── ClientService
│   ├── ProjectService
│   ├── ActivityService
│   ├── EmailService              → Contact form notifications
│   └── DashboardService          → KPI aggregation
├── repository/
│   ├── UserRepository
│   ├── LeadRepository
│   ├── ClientRepository
│   ├── ProjectRepository
│   ├── ActivityRepository
│   ├── TaskRepository
│   ├── MilestoneRepository
│   └── ReminderRepository
├── entity/
│   ├── User, Lead, Client, Project, Task, Milestone, Activity, Reminder
├── dto/
│   ├── LeadCreateRequest, LeadUpdateRequest, LeadResponse
│   ├── ClientRequest
│   ├── ProjectRequest
│   ├── LoginRequest, LoginResponse
│   └── ActivityRequest
├── security/
│   ├── JwtUtil                   → Token creation/validation
│   ├── JwtRequestFilter          → Filter for extracting JWT
│   └── CustomUserDetailsService  → Load user by username
└── PortfolioCrmApplication.java
```

**Dependencias**:
- Spring Boot Starter Web, Data JPA, Security, Mail, Validation, Actuator
- PostgreSQL 42.7.9
- Flyway 9.22.3
- JWT (jjwt) 0.12.3
- Lombok 1.18.30
- H2 (test scope)

**Seguridad**:
- JWT-based auth
- Roles: ADMIN (hardcoded, único rol existente)
- Endpoints públicos: `/auth/**`, `/leads/public`, actuator
- Endpoints protegidos: `/api/v1/**` (requiere ADMIN role)
- CORS habilitado con origins configurables
- Session: STATELESS
- Password: BCrypt
- Default admin: `admin@martiniano.dev` / `admin123`

**Frontend (React + Vite + Tailwind CSS)**:
```
src/
├── App.jsx                    → Public portfolio (Hero, Projects, Skills, Contact)
├── main.jsx                   → Router: / (public), /admin/* (protected)
├── admin/
│   ├── pages/
│   │   ├── Login.jsx          → /admin/login
│   │   ├── Dashboard.jsx      → /admin/dashboard (KPIs)
│   │   ├── Leads.jsx          → /admin/leads (list + search + filters)
│   │   ├── LeadDetail.jsx     → /admin/leads/:id
│   │   ├── Clients.jsx        → /admin/clients (list)
│   │   └── Projects.jsx       → /admin/projects (list)
│   ├── layout/
│   │   └── AdminLayout.jsx    → Sidebar + TopNav (Outlet for nested routes)
│   ├── components/
│   │   └── ProtectedRoute.jsx → Auth guard
│   ├── contexts/
│   │   └── AuthContext.jsx    → Global auth state
│   └── services/
│       ├── api.service.js     → Axios + JWT interceptor
│       ├── auth.service.js    → login, token storage
│       ├── leads.service.js
│       └── dashboard.service.js
├── components/
│   └── layout/                → Navbar, Footer for public site
├── features/
│   ├── hero/, projects/, services/, education/, contact/
└── contexts/
    └── ThemeContext.jsx       → Dark mode toggle
```

**Rutas Actuales**:
- `/` → Public portfolio
- `/admin/login` → Admin login
- `/admin/dashboard` → KPIs
- `/admin/leads` → Leads management
- `/admin/leads/:id` → Lead detail
- `/admin/clients` → Clients management
- `/admin/projects` → Projects management

**Observaciones**:
- ✅ Ya existe estructura admin completa con auth
- ✅ Layout reutilizable (AdminLayout con sidebar)
- ❌ No hay sección "Mis Proyectos" (internal projects)
- ❌ No hay vista de detalle de cliente con proyectos asociados
- ❌ No hay UI para conversión Lead → Account+Contact
- ❌ No hay UI para creación automática de repo GitHub

### 1.4 Migraciones Flyway

**V1__initial_schema.sql**:
- Crea 8 tablas con relaciones FK y cascadas
- Índices en: stage, created_at, email, client_id, project_id, entity tracking
- Campo `payload JSONB` en activities (flexible audit)

**V2__seed_data.sql**:
- Admin user: admin / admin@martiniano.dev
- 3 leads de ejemplo (NEW, CONTACTED, QUALIFIED)

**Observaciones**:
- ✅ Flyway configurado correctamente
- ✅ Índices de performance en campos clave
- ❌ No hay tabla `accounts` (Client es equivalente pero sin extensibilidad)
- ❌ No hay tabla `contacts` (Client tiene solo primary_contact_name)
- ❌ No hay tabla `github_repos` para metadata de repos
- ❌ No hay tabla `custom_field_definitions` (opcional pero útil)
- ❌ No hay campo `tenant_id` para multi-tenancy futuro

---

## 2. ARQUITECTURA TARGET

### 2.1 Flujo Target: Landing → Lead → Account + Contact → Project (con GitHub)

```
┌─────────────────┐
│  Landing Page   │
│  (Contact Form) │
└────────┬────────┘
         │ POST /api/v1/leads/public
         ▼
┌─────────────────────────────────────────────────────────┐
│                    BACKOFFICE ADMIN                     │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  1. LEADS MANAGEMENT                                    │
│     - Lista de leads (filtros, search, stage)           │
│     - Detalle de lead (edición, notas, actividades)     │
│     - Conversión: "Convert to Account + Contact"        │
│       ┌────────────────────────────────┐                │
│       │ ConvertLeadToAccountService    │                │
│       │ - Crea Account (from lead)     │                │
│       │ - Crea Contact (from lead)     │                │
│       │ - Marca lead como WON          │                │
│       │ - Registra Activity            │                │
│       └────────────────────────────────┘                │
│                                                         │
│  2. ACCOUNTS/CLIENTS MANAGEMENT                         │
│     - Lista de cuentas (filtros, search, status)        │
│     - Detalle de cuenta:                                │
│       * Contacts (1-to-many)                            │
│       * Projects (1-to-many)                            │
│       * Activities/Timeline                             │
│       * Custom Fields (JSONB)                           │
│       * Notes                                           │
│     - Crear/editar Account                              │
│     - Crear/editar Contact asociado                     │
│                                                         │
│  3. PROJECTS MANAGEMENT                                 │
│     - Vista "Client Projects" (por account)             │
│     - Vista "My Projects" (internal, no account)        │
│     - Crear proyecto:                                   │
│       ┌────────────────────────────────┐                │
│       │ ProjectService.createProject() │                │
│       │ - Valida datos                 │                │
│       │ - Persiste Project             │                │
│       │ - Si enableGithub=true:        │                │
│       │   ┌──────────────────────────┐ │                │
│       │   │ GitHubProvisioningService│ │                │
│       │   │ - createRepository()     │ │                │
│       │   │ - Naming: {slug}-{code}  │ │                │
│       │   │ - Template repo (opt)    │ │                │
│       │   │ - README + topics        │ │                │
│       │   │ - Branch protection      │ │                │
│       │   │ - Persist GitHubRepo     │ │                │
│       │   │ - Link to Project        │ │                │
│       │   └──────────────────────────┘ │                │
│       │ - Registra Activity            │                │
│       └────────────────────────────────┘                │
│     - Detalle de proyecto:                              │
│       * GitHub repo status (link, last_sync, status)    │
│       * Milestones & Tasks                              │
│       * Timeline                                        │
│       * Custom Fields                                   │
│                                                         │
│  4. GITHUB INTEGRATION STATUS                           │
│     - /admin/integrations/github                        │
│       * Health check                                    │
│       * Config display (org, template, enabled)         │
│       * Test connection                                 │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 2.2 Dominio Target

#### A. CRM Domain

**Account (evolución de Client)**:
```java
@Entity
@Table(name = "accounts")
public class Account {
    Long id;
    String name;                     // Company or person name
    String slug;                     // URL-friendly identifier for GitHub naming
    String email;
    String phone;
    String address;
    String status;                   // ACTIVE, INACTIVE, SUSPENDED
    String industry;                 // Optional sector
    String website;
    Long ownerId;                    // FK to User (account manager)
    Map<String, Object> customFields; // JSONB for client-specific data
    String notes;
    Long createdFromLeadId;          // FK to Lead (conversion tracking)
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

**Contact (asociado a Account)**:
```java
@Entity
@Table(name = "contacts")
public class Contact {
    Long id;
    Long accountId;                  // FK to Account (CASCADE)
    String firstName;
    String lastName;
    String email;
    String phone;
    String position;                 // Job title
    Boolean isPrimary;               // Primary contact flag
    String notes;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

**Lead (mantener pero mejorar conversión)**:
- Mismo schema actual
- Agregar método de conversión: `convertToAccountAndContact()`

#### B. Project Management Domain

**Project (mejorado)**:
```java
@Entity
@Table(name = "projects")
public class Project {
    Long id;
    Long accountId;                  // FK to Account (nullable for internal projects)
    String name;
    String code;                     // Project code for GitHub naming (e.g., "portfolio-v2")
    String description;
    String status;                   // PLANNED, ACTIVE, PAUSED, DONE, ARCHIVED
    String type;                     // CLIENT, INTERNAL
    LocalDate startDate;
    LocalDate targetDate;
    LocalDate completionDate;
    String stack;
    String deployLink;
    BigDecimal estimatedHours;
    BigDecimal actualHours;
    BigDecimal budgetAmount;
    Long ownerId;                    // FK to User (project manager)
    String team;                     // JSON array of user IDs or names
    String tags;                     // Comma-separated tags
    Map<String, Object> customFields; // JSONB
    String links;                    // JSON array of {label, url}
    Long githubRepoId;               // FK to GitHubRepo
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

**GitHubRepo (nueva entidad)**:
```java
@Entity
@Table(name = "github_repos")
public class GitHubRepo {
    Long id;
    String repoFullName;             // e.g., "myorg/client-project-code"
    String repoUrl;                  // e.g., "https://github.com/myorg/client-project-code"
    String defaultBranch;            // e.g., "main"
    String status;                   // PROVISIONING, ACTIVE, FAILED, ARCHIVED
    String provisioningError;        // Error message if status=FAILED
    LocalDateTime provisionedAt;
    LocalDateTime lastSyncAt;        // For future webhook sync
    Map<String, Object> metadata;    // JSONB for additional GitHub data
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

#### C. Workflows & State Machines (mínimo)

**Lead Stages**:
- NEW → CONTACTED → QUALIFIED → PROPOSAL → NEGOTIATION → WON/LOST

**Project Status**:
- PLANNED → ACTIVE → PAUSED ⇄ ACTIVE → DONE → ARCHIVED

**GitHub Repo Status**:
- PROVISIONING → ACTIVE | FAILED

#### D. Tenancy & RBAC (preparación futura)

**User (sin cambios mayores)**:
- role: ADMIN (único por ahora)
- En futuro: agregar `tenant_id` si se necesita multi-tenancy real

**Permissions (simplificado)**:
- Dado que solo hay ADMIN, usar ownership-based access:
  - Account.ownerId
  - Project.ownerId
- En futuro: implementar tabla `permissions` y relación many-to-many con roles

---

## 3. GAP ANALYSIS

| Capacidad | Estado Actual | Estado Target | Gap | Prioridad |
|-----------|---------------|---------------|-----|-----------|
| **Gestión de Leads** | ✅ Completo | ✅ Mantener + mejorar conversión | Agregar wizard de conversión | 🟡 Medium |
| **Gestión de Accounts** | ⚠️ Parcial (Client sin extensibilidad) | Evolucionar a Account con custom fields | Renombrar + agregar campos JSONB + slug | 🔴 High |
| **Gestión de Contacts** | ❌ No existe (solo primary contact en Client) | Entidad Contact 1-to-many con Account | Crear entidad + CRUD + UI | 🔴 High |
| **Lead → Account Conversion** | ❌ No automatizado | Servicio de conversión automático | Implementar service + UI wizard | 🟡 Medium |
| **Proyectos de Cliente** | ✅ Existe (FK client_id NOT NULL) | ✅ Mantener con mejoras | Agregar custom fields, team, links | 🟡 Medium |
| **Proyectos Internos** | ❌ No soportado | Proyectos sin accountId (type=INTERNAL) | Hacer accountId nullable + type enum | 🔴 High |
| **GitHub Integration** | ❌ No existe | Auto-crear repo al crear proyecto | Módulo completo + entity + service | 🔴 High |
| **Custom Fields** | ❌ No existe | JSONB en Account + Project | Agregar columnas + DTO support | 🟡 Medium |
| **Multi-tenancy** | ❌ No existe | Preparar modelo (sin implementar aún) | Documentar estrategia para futuro | 🟢 Low |
| **RBAC Avanzado** | ⚠️ Solo ADMIN role | Owner-based access + capacidades | Implementar owner checks en services | 🟡 Medium |
| **Audit Log** | ✅ Activities table completo | ✅ Mantener | Agregar logs para nuevas entidades | 🟢 Low |
| **UI Clients Detail** | ❌ Solo lista | Detalle con contacts + projects + timeline | Crear página de detalle | 🔴 High |
| **UI My Projects** | ❌ No existe | Sección separada para internal projects | Agregar ruta + filtro | 🟡 Medium |
| **UI GitHub Status** | ❌ No existe | Mostrar repo status en project detail | Componente de GitHub badge | 🟡 Medium |
| **Health Checks** | ⚠️ Actuator básico | Health check para GitHub integration | Custom health indicator | 🟡 Medium |
| **Tests** | ⚠️ Mínimos | Unit + integration tests para nuevas features | Escribir tests | 🟡 Medium |
| **Documentación** | ⚠️ README básico | Docs en /docs con env vars + setup | Crear markdown files | 🟡 Medium |

---

## 4. INTEGRACIÓN CON GITHUB

### 4.1 Estrategia de Integración

**Opciones Evaluadas**:
1. **GitHub Personal Access Token (PAT)** → ✅ SELECCIONADA PARA MVP
   - Pros: Simple, rápido setup, suficiente para single org
   - Cons: No escala para múltiples orgs, requiere regenerar tokens
2. **GitHub App**
   - Pros: Mejor seguridad, permissions granulares, multi-org
   - Cons: Setup más complejo, requiere webhook endpoint público

**Decisión**: Comenzar con PAT, preparar código para soportar App en futuro.

### 4.2 Flujo de Provisión de Repositorio

```
ProjectService.createProject(request):
├── Validar request (name, code, accountId si type=CLIENT)
├── Persistir Project entity
├── IF request.enableGithub == true:
│   ├── GitHubProvisioningService.provisionRepository(project):
│   │   ├── Generar nombre: {accountSlug}-{projectCode} o internal-{projectCode}
│   │   ├── Verificar si repo ya existe (idempotencia)
│   │   ├── IF exists:
│   │   │   └── Registrar GitHubRepo con status=ACTIVE
│   │   ├── ELSE:
│   │   │   ├── GitHub API: POST /orgs/{org}/repos o POST /user/repos
│   │   │   │   Body: {name, description, private, auto_init, template}
│   │   │   ├── IF template_repo configured:
│   │   │   │   └── GitHub API: POST /repos/{template}/generate
│   │   │   ├── Agregar topics/tags
│   │   │   ├── Actualizar README con project metadata
│   │   │   ├── IF branch protection enabled:
│   │   │   │   └── GitHub API: PUT /repos/{owner}/{repo}/branches/{branch}/protection
│   │   │   └── Persistir GitHubRepo con status=ACTIVE
│   │   └── CATCH ApiException:
│   │       └── Persistir GitHubRepo con status=FAILED + error message
│   └── Actualizar Project.githubRepoId
└── Registrar Activity (PROJECT_CREATED + GITHUB_REPO_PROVISIONED/FAILED)
```

### 4.3 Configuración Requerida

**Environment Variables**:
```bash
# GitHub Integration
GITHUB_ENABLED=true                                    # Enable/disable GitHub features
GITHUB_PROVIDER=PAT                                    # PAT or APP
GITHUB_TOKEN=ghp_xxxxxxxxxxxx                          # PAT token
GITHUB_ORG=myorganization                              # GitHub org or username
GITHUB_TEMPLATE_REPO=myorganization/project-template   # Optional template
GITHUB_DEFAULT_PRIVATE=true                            # Create private repos by default
GITHUB_AUTO_INIT=true                                  # Initialize with README
GITHUB_BRANCH_PROTECTION_ENABLED=false                 # Enable branch protection rules

# Future: GitHub App support
# GITHUB_APP_ID=123456
# GITHUB_APP_PRIVATE_KEY=-----BEGIN RSA PRIVATE KEY-----...
# GITHUB_INSTALLATION_ID=987654
```

**Seguridad**:
- Tokens almacenados en variables de entorno (NUNCA en código o DB)
- Validar que sistema arranca aunque GITHUB_ENABLED=false
- Logs de errores sin exponer tokens
- Rate limiting awareness (GitHub API tiene límites)

### 4.4 Metadata de Repositorio

**GitHubRepo entity** almacena:
- `repoFullName`: `owner/repo`
- `repoUrl`: Link directo
- `defaultBranch`: `main` o `master`
- `status`: PROVISIONING, ACTIVE, FAILED, ARCHIVED
- `provisioningError`: Mensaje de error si falla
- `provisionedAt`: Timestamp de creación
- `lastSyncAt`: Para futura sincronización vía webhooks
- `metadata`: JSONB para datos extra (stars, forks, last_commit, etc.)

### 4.5 Naming Convention

**Client Projects**:
- Format: `{account.slug}-{project.code}`
- Example: `acme-corp-website-redesign`
- Validación: slug debe ser lowercase, alphanumeric + hyphens

**Internal Projects**:
- Format: `internal-{project.code}`
- Example: `internal-portfolio-v2`

**Generación de slug**:
- Account.name → lowercase → replace spaces/special chars with `-`
- Example: "ACME Corporation" → "acme-corporation"

### 4.6 Error Handling

**Escenarios**:
1. **GitHub API Down**: Project se crea igual, GitHubRepo.status=FAILED
2. **Repo ya existe**: Idempotencia - registrar GitHubRepo con datos existentes
3. **Token inválido**: Log error, marcar FAILED, continuar
4. **Rate limit exceeded**: Retry con exponential backoff o marcar FAILED
5. **Permission denied**: Log error, marcar FAILED con mensaje específico

**UI Indicators**:
- ✅ ACTIVE: Mostrar link verde al repo
- ⏳ PROVISIONING: Spinner "Creating repository..."
- ❌ FAILED: Badge rojo + tooltip con error + botón "Retry"
- 📦 ARCHIVED: Badge gris "Archived"

---

## 5. PLAN DE IMPLEMENTACIÓN POR PRs

### PR1: Analysis + Domain Modeling + Migrations (SIN UI, SIN GITHUB)

**Objetivo**: Sentar las bases del modelo de datos sin implementar lógica de negocio ni UI.

**Scope**:
1. **Documentación**: ✅
   - `/docs/ANALYSIS_CLIENTS_PROJECTS.md` (este documento)
   - `/docs/github-integration.md` (spec detallado)
   - `/docs/projects.md` (spec de features de proyectos)
   - `/docs/clients.md` (spec de features de clientes/accounts)
   
2. **Migraciones Flyway**:
   - `V3__evolve_clients_to_accounts.sql`: Renombrar clients → accounts, agregar campos
   - `V4__create_contacts_table.sql`: Crear tabla contacts
   - `V5__improve_projects_table.sql`: Hacer account_id nullable, agregar campos
   - `V6__create_github_repos_table.sql`: Crear tabla github_repos
   
3. **Entities**:
   - Renombrar Client.java → Account.java
   - Crear Contact.java
   - Actualizar Project.java
   - Crear GitHubRepo.java
   
4. **Repositories**:
   - Renombrar ClientRepository → AccountRepository
   - Crear ContactRepository
   - Actualizar ProjectRepository
   - Crear GitHubRepoRepository
   
5. **DTOs**:
   - AccountRequest, AccountResponse, AccountDetailResponse
   - ContactRequest, ContactResponse
   - Actualizar ProjectRequest
   - GitHubRepoResponse
   
6. **Tests**: Smoke tests

**No incluir**: Controllers, Services, UI, GitHub logic

---

### PR2: CRUD Accounts/Contacts + UI Clients

**Objetivo**: Implementar gestión completa de cuentas y contactos con UI funcional.

**Scope Backend**:
- AccountService: CRUD + convertLeadToAccount()
- ContactService: CRUD + setPrimary()
- AccountController: REST endpoints
- ContactController: REST endpoints
- Activity logging
- Unit + integration tests

**Scope Frontend**:
- `/admin/clients`: Lista con filtros
- `/admin/clients/:id`: Detalle con tabs (Overview, Contacts, Projects, Timeline)
- AccountForm, ContactForm, ConvertLeadModal components
- accounts.service.js, contacts.service.js
- Screenshots requeridos

---

### PR3: CRUD Projects (Client + Internal) + UI Projects

**Objetivo**: Implementar gestión de proyectos (cliente + internos) sin GitHub.

**Scope Backend**:
- ProjectService: CRUD con validación type=CLIENT/INTERNAL
- ProjectController: REST endpoints con filtros
- Activity logging
- Tests con validaciones

**Scope Frontend**:
- `/admin/projects`: Lista con tabs (All, Client, My Projects)
- `/admin/projects/:id`: Detalle con tabs
- ProjectForm: type selector, code field, custom fields
- Sidebar update con submenu Projects
- Screenshots requeridos

---

### PR4: GitHub Integration (Backend + UI)

**Objetivo**: Implementar integración completa con GitHub.

**Scope Backend**:
- GitHubConfigProperties
- GitHubClient: Wrapper HTTP con retry logic
- GitHubProvisioningService: provisionRepository(), retryProvisioning(), syncMetadata()
- ProjectService.create(): Integrar GitHub provisioning
- GitHubIntegrationController: health, test, provision, retry, sync endpoints
- GitHubHealthIndicator
- Tests con mock HTTP

**Scope Frontend**:
- `/admin/projects/:id`: Sección GitHub con status badge
- `/admin/integrations/github`: Health page
- GitHubRepoStatusBadge, GitHubHealthCard components
- ProjectForm: Enable GitHub checkbox
- Environment variables en .env.example
- Screenshots requeridos

---

### PR5: Hardening, Quality & Final Touches

**Objetivo**: Robustecer sistema con RBAC, audit, tests, docs.

**Scope Backend**:
- RBAC: @PreAuthorize con ownership checks
- Audit: userId, ipAddress, userAgent en Activity
- Health checks: GitHubHealthIndicator, DatabaseHealthIndicator
- Structured logging: JSON format, MDC tracing
- Error handling: ProblemDetail (RFC 7807)
- Tests: >70% coverage
- OpenAPI/Swagger: SpringDoc

**Scope Frontend**:
- RBAC UI: Permission-based rendering
- Loading states: Skeleton loaders
- Error boundaries
- Toast notifications
- Responsive design
- Dark mode verification

**Scope Docs**:
- README: Env vars, GitHub setup, architecture
- DEPLOYMENT_GUIDE: Docker Compose, production
- CONTRIBUTING: PR template, style guide

**Scope Docker**:
- docker-compose.yml: Health checks, volumes
- Multi-stage Dockerfiles
- Nginx frontend

---

## 6. ASSUMPTIONS & DECISIONS

### 6.1 Assumptions

1. GitHub Organization disponible o cuenta personal
2. Single tenant (multi-tenancy preparado pero no implementado)
3. Solo rol ADMIN por ahora
4. PostgreSQL como DB única
5. Deployment con Docker Compose
6. Public landing page sin cambios
7. No real-time (sync manual)
8. GitHub PAT inicial (App es futuro)

### 6.2 Decisiones Técnicas

| Decisión | Razón |
|----------|-------|
| Client → Account | Mejor naming CRM, extensibilidad |
| Contact separado | 1-to-many flexible |
| Project.accountId NULLABLE | Proyectos internos |
| GitHubRepo separado | Separación de concerns |
| Custom fields JSONB | Evita ALTER TABLE, flexible |
| GitHub PAT inicial | Simplicidad MVP |
| Flyway migrations | Versionado schema seguro |
| Structured logging | Debugging, integración futura |
| ProblemDetail RFC 7807 | Estándar error responses |
| Owner-based RBAC | Preparar roles futuros |

### 6.3 Decisiones de Alcance

**Incluido**:
- ✅ CRM básico (Accounts, Contacts, conversión)
- ✅ Project management (client + internal)
- ✅ GitHub integration (auto repo)
- ✅ Audit log
- ✅ RBAC baseline
- ✅ Health checks
- ✅ Structured logging
- ✅ OpenAPI/Swagger

**Excluido (futuro)**:
- ❌ Multi-tenancy real
- ❌ GitHub App
- ❌ GitHub webhooks
- ❌ Real-time WebSockets
- ❌ Kanban/Gantt
- ❌ Time tracking avanzado
- ❌ Invoicing/Billing
- ❌ Email campaigns
- ❌ Client portal
- ❌ Mobile app
- ❌ Advanced reporting/BI
- ❌ CI/CD config desde UI
- ❌ GitHub Actions templates

---

## 7. RISKS & MITIGATIONS

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| GitHub API Rate Limits | Alta | Medio | Caching, retry backoff, error UX |
| Token expiration | Media | Alto | Documentar rotación, warnings |
| Repo naming conflicts | Media | Medio | Validar uniqueness, idempotencia |
| Data migration errors | Baja | Alto | Backups, rollback plan, tests |
| Breaking frontend changes | Media | Medio | Feature flags, API versioning, tests |
| Performance JSONB | Baja | Medio | Índices GIN, límites tamaño |
| Security: token leak | Baja | Crítico | No logear tokens, env vars, secrets mgmt |
| User confusion Account/Client | Media | Bajo | Docs, tooltips, migración gradual |

---

## 8. SUCCESS CRITERIA

### 8.1 Funcional
- ✅ Lead flow sin cambios
- ✅ Conversión Lead → Account + Contact
- ✅ CRUD Accounts con custom fields
- ✅ Múltiples Contacts por Account
- ✅ Projects de cliente (con Account)
- ✅ Projects internos (sin Account)
- ✅ Auto-creación repo GitHub
- ✅ UI muestra status repo
- ✅ Project se crea aunque GitHub falle
- ✅ Health check refleja GitHub status
- ✅ Swagger UI completo

### 8.2 Técnico
- ✅ Migraciones sin errores
- ✅ Backward compatible
- ✅ Tests >70% coverage
- ✅ docker-compose up funciona
- ✅ Logs JSON estructurados
- ✅ Zero secrets hardcoded
- ✅ API RESTful
- ✅ ProblemDetail errors

### 8.3 Documentación
- ✅ README actualizado
- ✅ /docs con 4+ markdown
- ✅ Swagger accesible
- ✅ DEPLOYMENT_GUIDE
- ✅ Screenshots en PRs

---

## 9. NEXT STEPS (POST-MVP)

1. GitHub App Integration
2. Webhooks GitHub
3. Multi-tenancy real
4. Advanced RBAC (más roles)
5. Real-time notifications (WebSockets)
6. Kanban board
7. Time tracking + billing
8. Client portal
9. Advanced reporting/BI
10. CI/CD templates
11. Email campaigns
12. Mobile app

---

## 10. CONCLUSIÓN

Proyecto transforma backoffice en plataforma CRM + PM con GitHub integration. Enfoque iterativo por PRs minimiza riesgos. Arquitectura extensible, preparada para multi-tenancy y features futuras.

**Timeline Estimado** (1 dev senior):
- PR1: 2-3 días
- PR2: 3-4 días
- PR3: 3-4 días
- PR4: 4-5 días
- PR5: 2-3 días
- **Total**: 15-20 días (3-4 semanas)

**Key Principles**:
- 🔒 Security first
- 📊 Observability
- 🧪 Testability
- 📝 Documentation
- 🎯 Incremental delivery
- ♻️ Extensibility

---

_Generado por Staff Engineer / Tech Lead - martinianod/portfolio_fullstack_
_Fecha: 2026-01-27_
