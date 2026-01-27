# Projects Management - Especificación de Features

**Repositorio**: martinianod/portfolio_fullstack  
**Fecha**: 2026-01-27  
**Versión**: 1.0

---

## Tabla de Contenidos

1. [Resumen](#resumen)
2. [Tipos de Proyectos](#tipos-de-proyectos)
3. [Modelo de Datos](#modelo-de-datos)
4. [Estados y Workflows](#estados-y-workflows)
5. [Funcionalidades](#funcionalidades)
6. [API Endpoints](#api-endpoints)
7. [Frontend UI](#frontend-ui)
8. [Custom Fields](#custom-fields)
9. [Integraciones](#integraciones)
10. [Reporting](#reporting)

---

## Resumen

El módulo de **Projects Management** permite gestionar proyectos de dos tipos:

1. **Client Projects**: Proyectos asociados a una cuenta/cliente
2. **Internal Projects**: Proyectos internos de la empresa (portfolio, tools, etc.)

**Objetivos**:
- Centralizar toda la información de proyectos
- Seguimiento de estados, fechas, presupuesto
- Vinculación con GitHub para versionado de código
- Gestión de milestones y tasks
- Timeline de actividades
- Custom fields para extensibilidad

---

## Tipos de Proyectos

### Client Project

**Definición**: Proyecto desarrollado para un cliente específico.

**Características**:
- Asociado a un Account (requerido)
- Vinculado a contacto primario del cliente
- Facturación y presupuesto tracking
- GitHub repo con naming: `{account-slug}-{project-code}`
- Visibilidad controlada por account owner

**Ejemplos**:
- Website redesign para ACME Corp
- Mobile app para TechStart Inc
- E-commerce platform para ShopFast

### Internal Project

**Definición**: Proyecto interno de la empresa, no asociado a cliente.

**Características**:
- No tiene Account (accountId = null)
- Usado para R&D, tools internos, portfolio
- GitHub repo con naming: `internal-{project-code}`
- Visibilidad para todo el equipo

**Ejemplos**:
- Portfolio personal v2
- Internal CRM tool
- DevOps automation scripts

---

## Modelo de Datos

### Entity: Project

```java
@Entity
@Table(name = "projects")
@Data
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Basic Info
    @Column(nullable = false)
    private String name;                    // "Website Redesign"
    
    @Column(nullable = false, unique = true)
    private String code;                    // "website-redesign" (slug, unique)
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    // Type & Relationship
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectType type;               // CLIENT or INTERNAL
    
    @Column(name = "account_id")
    private Long accountId;                 // FK to Account (nullable for INTERNAL)
    
    // Status & Lifecycle
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectStatus status;           // PLANNED, ACTIVE, PAUSED, DONE, ARCHIVED
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "target_date")
    private LocalDate targetDate;
    
    @Column(name = "completion_date")
    private LocalDate completionDate;
    
    // Technical Info
    @Column(columnDefinition = "TEXT")
    private String stack;                   // "React, Spring Boot, PostgreSQL"
    
    @Column(name = "deploy_link")
    private String deployLink;              // Production URL
    
    // Budget & Hours
    @Column(name = "estimated_hours", precision = 10, scale = 2)
    private BigDecimal estimatedHours;
    
    @Column(name = "actual_hours", precision = 10, scale = 2)
    private BigDecimal actualHours;
    
    @Column(name = "budget_amount", precision = 12, scale = 2)
    private BigDecimal budgetAmount;
    
    // Team & Ownership
    @Column(name = "owner_id")
    private Long ownerId;                   // FK to User (project manager)
    
    @Column(columnDefinition = "TEXT")
    private String team;                    // JSON array: ["user1", "user2"]
    
    // Tags & Categorization
    private String tags;                    // Comma-separated: "web,react,urgent"
    
    // Extensibility
    @Column(columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private Map<String, Object> customFields;  // Flexible data
    
    @Column(columnDefinition = "TEXT")
    private String links;                   // JSON: [{label: "Design", url: "..."}]
    
    // GitHub Integration
    @Column(name = "github_repo_id")
    private Long githubRepoId;              // FK to GitHubRepo
    
    // Audit
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### Enums

```java
public enum ProjectType {
    CLIENT,      // Project for a client
    INTERNAL     // Internal company project
}

public enum ProjectStatus {
    PLANNED,     // Planning phase, not started yet
    ACTIVE,      // Currently in development
    PAUSED,      // Temporarily paused
    DONE,        // Completed successfully
    ARCHIVED     // Old project, no longer active
}
```

### Validaciones

**A nivel entity**:
```java
@AssertTrue(message = "Client projects must have an accountId")
private boolean isAccountIdValidForType() {
    if (type == ProjectType.CLIENT) {
        return accountId != null;
    }
    return true;
}

@AssertTrue(message = "Internal projects cannot have an accountId")
private boolean isAccountIdNullForInternal() {
    if (type == ProjectType.INTERNAL) {
        return accountId == null;
    }
    return true;
}
```

**A nivel service**:
- `code` debe ser único (slug format: lowercase, alphanumeric + hyphens)
- `targetDate` debe ser posterior a `startDate`
- `completionDate` solo se puede setear si status = DONE
- No se puede cambiar `type` después de creado
- No se puede eliminar si tiene milestones/tasks activos

---

## Estados y Workflows

### State Machine

```
┌─────────┐
│ PLANNED │ ────────────────────────────────────┐
└────┬────┘                                     │
     │ Start project                            │
     ▼                                          │
┌────────┐     Pause                            │
│ ACTIVE │ ◄──────────────┐                     │
└───┬──┬─┘                │                     │
    │  │                  │                     │
    │  │ Complete    ┌────┴────┐               │
    │  └────────────►│ PAUSED  │               │
    │                └────┬────┘               │
    │ Mark done           │ Resume             │
    ▼                     ▼                     │
┌───────┐           ┌────────┐                 │
│ DONE  │           │ ACTIVE │                 │
└───┬───┘           └────────┘                 │
    │                                           │
    │ Archive after 6 months                    │ Cancel/Archive
    ▼                                           │
┌──────────┐                                    │
│ ARCHIVED │ ◄──────────────────────────────────┘
└──────────┘
```

### Transiciones Permitidas

| From | To | Condition | Effect |
|------|-----|-----------|--------|
| PLANNED | ACTIVE | startDate set | Begin development |
| PLANNED | ARCHIVED | None | Cancel before start |
| ACTIVE | PAUSED | None | Stop temporarily |
| ACTIVE | DONE | All tasks completed | Mark completion_date |
| PAUSED | ACTIVE | None | Resume work |
| PAUSED | ARCHIVED | None | Cancel paused project |
| DONE | ARCHIVED | None | Archive completed project |

### Reglas de Negocio

1. **PLANNED → ACTIVE**: 
   - Debe tener `startDate` configurado
   - Debe tener al menos 1 milestone o task
   - Log activity: PROJECT_STARTED

2. **ACTIVE → PAUSED**:
   - Registrar razón en activity (opcional)
   - Email notification al owner

3. **ACTIVE → DONE**:
   - Validar que todos los milestones están COMPLETED
   - Setear `completionDate` = today
   - Calcular variance: (actualHours - estimatedHours)
   - Log activity: PROJECT_COMPLETED

4. **DONE → ARCHIVED**:
   - Solo si proyecto completado hace >6 meses (configurable)
   - Mantener datos, solo cambia visibilidad

---

## Funcionalidades

### 1. CRUD Básico

#### Create Project

**Request**:
```json
POST /api/v1/projects
{
  "name": "Website Redesign",
  "code": "website-redesign",
  "type": "CLIENT",
  "accountId": 123,
  "description": "Complete redesign of corporate website",
  "status": "PLANNED",
  "startDate": "2026-02-01",
  "targetDate": "2026-05-01",
  "stack": "React, Node.js, PostgreSQL",
  "estimatedHours": 320,
  "budgetAmount": 25000,
  "ownerId": 1,
  "team": ["user1@example.com", "user2@example.com"],
  "tags": "web,react,urgent",
  "customFields": {
    "client_priority": "high",
    "contract_number": "CNT-2026-001"
  },
  "links": [
    {"label": "Design Mockups", "url": "https://figma.com/..."},
    {"label": "Requirements Doc", "url": "https://docs.google.com/..."}
  ],
  "enableGithub": true
}
```

**Response 201**:
```json
{
  "id": 456,
  "name": "Website Redesign",
  "code": "website-redesign",
  "type": "CLIENT",
  "accountId": 123,
  "accountName": "ACME Corporation",
  "status": "PLANNED",
  "githubRepo": {
    "id": 789,
    "repoFullName": "myorg/acme-corp-website-redesign",
    "repoUrl": "https://github.com/myorg/acme-corp-website-redesign",
    "status": "ACTIVE"
  },
  "createdAt": "2026-01-27T12:00:00Z"
}
```

#### Get Project Detail

```json
GET /api/v1/projects/456

Response 200:
{
  "id": 456,
  "name": "Website Redesign",
  "code": "website-redesign",
  "type": "CLIENT",
  "account": {
    "id": 123,
    "name": "ACME Corporation",
    "slug": "acme-corp"
  },
  "status": "ACTIVE",
  "startDate": "2026-02-01",
  "targetDate": "2026-05-01",
  "completionDate": null,
  "progress": {
    "milestonesCompleted": 2,
    "milestonesTotal": 5,
    "tasksCompleted": 15,
    "tasksTotal": 40,
    "hoursSpent": 120,
    "hoursEstimated": 320,
    "percentComplete": 37.5
  },
  "githubRepo": {...},
  "milestones": [...],
  "recentActivity": [...],
  "team": [...],
  "customFields": {...}
}
```

#### Update Project

```json
PUT /api/v1/projects/456
{
  "name": "Website Redesign v2",
  "targetDate": "2026-06-01",
  "actualHours": 150
}

Response 200: (updated project)
```

#### Change Status

```json
PATCH /api/v1/projects/456/status
{
  "status": "ACTIVE",
  "reason": "Kickoff meeting completed"
}

Response 200: (updated project)
```

#### Delete Project

```json
DELETE /api/v1/projects/456

Response 204: (no content)
```

**Validaciones**:
- Solo se puede eliminar si status = PLANNED o ARCHIVED
- Si tiene milestones/tasks, preguntar confirmación
- Soft delete: marcar como deleted=true en lugar de borrar

---

### 2. Filtrado y Búsqueda

```json
GET /api/v1/projects?type=CLIENT&status=ACTIVE&accountId=123&search=website&page=0&size=20

Response 200:
{
  "content": [
    { "id": 456, "name": "Website Redesign", ... }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

**Filtros Disponibles**:
- `type`: CLIENT, INTERNAL
- `status`: PLANNED, ACTIVE, PAUSED, DONE, ARCHIVED
- `accountId`: Filter by client
- `ownerId`: Filter by project manager
- `tags`: Filter by tag (comma-separated)
- `search`: Buscar en name, description, code
- `startDateFrom`, `startDateTo`: Date range
- `sort`: Field to sort by (default: createdAt DESC)

---

### 3. Projects por Account

```json
GET /api/v1/accounts/123/projects?status=ACTIVE

Response 200:
{
  "accountId": 123,
  "accountName": "ACME Corporation",
  "projects": [
    {
      "id": 456,
      "name": "Website Redesign",
      "status": "ACTIVE",
      "progress": 37.5
    },
    {
      "id": 457,
      "name": "Mobile App",
      "status": "PLANNED",
      "progress": 0
    }
  ]
}
```

---

### 4. "My Projects" (Internal)

```json
GET /api/v1/projects?type=INTERNAL

Response 200:
{
  "content": [
    {
      "id": 500,
      "name": "Portfolio v2",
      "code": "portfolio-v2",
      "type": "INTERNAL",
      "status": "ACTIVE",
      "githubRepo": {
        "repoFullName": "myorg/internal-portfolio-v2"
      }
    }
  ]
}
```

**Sección Frontend**: `/admin/projects?tab=my-projects`

---

### 5. Milestones & Tasks

#### Crear Milestone

```json
POST /api/v1/projects/456/milestones
{
  "name": "Design Phase",
  "description": "Complete all design mockups",
  "dueDate": "2026-02-15",
  "orderIndex": 1
}

Response 201: (milestone)
```

#### Crear Task

```json
POST /api/v1/projects/456/tasks
{
  "title": "Design homepage mockup",
  "milestoneId": 789,
  "assignee": "designer@example.com",
  "priority": "HIGH",
  "estimatedHours": 8,
  "dueDate": "2026-02-10"
}

Response 201: (task)
```

**Queries**:
- `GET /api/v1/projects/456/milestones`
- `GET /api/v1/projects/456/tasks?status=TODO`
- `PATCH /api/v1/tasks/123/status` → Mark as done

---

### 6. Activity Timeline

```json
GET /api/v1/projects/456/activity

Response 200:
{
  "projectId": 456,
  "activities": [
    {
      "id": 1001,
      "activityType": "PROJECT_CREATED",
      "description": "Project created",
      "createdBy": "admin@example.com",
      "createdAt": "2026-01-27T12:00:00Z"
    },
    {
      "id": 1002,
      "activityType": "GITHUB_REPO_PROVISIONED",
      "description": "GitHub repository created: acme-corp-website-redesign",
      "payload": {
        "repoUrl": "https://github.com/myorg/acme-corp-website-redesign"
      },
      "createdAt": "2026-01-27T12:00:05Z"
    },
    {
      "id": 1003,
      "activityType": "PROJECT_STATUS_CHANGED",
      "description": "Status changed from PLANNED to ACTIVE",
      "createdBy": "admin@example.com",
      "createdAt": "2026-02-01T09:00:00Z"
    }
  ]
}
```

---

## API Endpoints

### Projects

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/projects` | Create project | ADMIN |
| GET | `/api/v1/projects` | List projects (with filters) | ADMIN |
| GET | `/api/v1/projects/{id}` | Get project detail | ADMIN |
| PUT | `/api/v1/projects/{id}` | Update project | ADMIN/Owner |
| DELETE | `/api/v1/projects/{id}` | Delete project | ADMIN/Owner |
| PATCH | `/api/v1/projects/{id}/status` | Change status | ADMIN/Owner |
| GET | `/api/v1/accounts/{accountId}/projects` | Projects by account | ADMIN |
| GET | `/api/v1/projects/{id}/activity` | Activity timeline | ADMIN |

### Milestones

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/projects/{id}/milestones` | Create milestone | ADMIN/Owner |
| GET | `/api/v1/projects/{id}/milestones` | List milestones | ADMIN |
| PUT | `/api/v1/milestones/{id}` | Update milestone | ADMIN/Owner |
| DELETE | `/api/v1/milestones/{id}` | Delete milestone | ADMIN/Owner |
| PATCH | `/api/v1/milestones/{id}/status` | Change status | ADMIN/Owner |

### Tasks

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/projects/{id}/tasks` | Create task | ADMIN/Owner |
| GET | `/api/v1/projects/{id}/tasks` | List tasks | ADMIN |
| PUT | `/api/v1/tasks/{id}` | Update task | ADMIN/Owner/Assignee |
| DELETE | `/api/v1/tasks/{id}` | Delete task | ADMIN/Owner |
| PATCH | `/api/v1/tasks/{id}/status` | Change status | ADMIN/Owner/Assignee |

---

## Frontend UI

### Pages Structure

```
/admin/projects
├── All Projects (tab)
├── Client Projects (tab)
└── My Projects (tab)

/admin/projects/:id
├── Overview (tab)
├── Milestones & Tasks (tab)
├── Timeline (tab)
└── Settings (tab)

/admin/accounts/:id
└── Projects (tab in account detail)
```

### Projects List Page

**URL**: `/admin/projects`

**Components**:
- **Tabs**: All / Client / My Projects
- **Filters**: Status, Owner, Tags, Date range
- **Search**: By name, code, description
- **Table Columns**:
  - Name (link to detail)
  - Type badge (CLIENT/INTERNAL)
  - Account (if client project)
  - Status badge
  - Progress bar
  - Start/Target dates
  - Owner
  - Actions (edit, delete)
- **Action Button**: "New Project"

**Wireframe**:
```
┌─────────────────────────────────────────────────────────┐
│ Projects                                    [+ New]      │
├─────────────────────────────────────────────────────────┤
│ [All] [Client Projects] [My Projects]                   │
│                                                         │
│ Filters: [Status ▼] [Owner ▼] [Tags ▼]   🔍 Search      │
│                                                         │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Name           Type    Account    Status  Progress  │ │
│ ├─────────────────────────────────────────────────────┤ │
│ │ Website        CLIENT  ACME       ACTIVE  ███▒▒ 37% │ │
│ │ Redesign                                            │ │
│ │                                                     │ │
│ │ Mobile App     CLIENT  TechStart  PLANNED ▒▒▒▒▒  0% │ │
│ │                                                     │ │
│ │ Portfolio v2   INTERNAL -         ACTIVE  ████▒ 65% │ │
│ └─────────────────────────────────────────────────────┘ │
│                                        [< 1 2 3 >]      │
└─────────────────────────────────────────────────────────┘
```

---

### Project Detail Page

**URL**: `/admin/projects/456`

**Tabs**:

#### 1. Overview

- Project name, code, type, status
- Account info (if client project)
- Dates: start, target, completion
- Budget & hours (estimated vs actual)
- Progress metrics
- Tech stack
- Links (design, docs, etc.)
- Custom fields (key-value display)
- **GitHub Section**:
  - If ACTIVE: Badge + link to repo
  - If FAILED: Error message + Retry button
  - If not configured: "Enable GitHub" button

#### 2. Milestones & Tasks

- List of milestones with completion %
- Expandable: show tasks under each milestone
- Kanban board view (optional)
- Add milestone/task buttons
- Drag & drop to reorder

#### 3. Timeline

- Activity log (reverse chronological)
- Filter by activity type
- Show user, timestamp, description
- Payload details (expandable)

#### 4. Settings

- Edit project fields
- Change owner
- Manage team members
- Delete project (with confirmation)

**Wireframe**:
```
┌─────────────────────────────────────────────────────────┐
│ ← Back to Projects                                       │
│                                                         │
│ Website Redesign                        [Edit] [Delete]  │
│ CLIENT • ACTIVE • acme-corp-website-redesign            │
├─────────────────────────────────────────────────────────┤
│ [Overview] [Milestones & Tasks] [Timeline] [Settings]   │
│                                                         │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ 📊 Progress                                         │ │
│ │ ███████▒▒▒▒▒▒▒▒▒ 37.5% complete                     │ │
│ │                                                     │ │
│ │ Milestones: 2/5 • Tasks: 15/40 • Hours: 120/320    │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                         │
│ ┌──────────────────┐ ┌──────────────────┐              │
│ │ Account          │ │ Dates            │              │
│ │ ACME Corporation │ │ Start: 02/01/26  │              │
│ │                  │ │ Target: 05/01/26 │              │
│ └──────────────────┘ └──────────────────┘              │
│                                                         │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ 🐙 GitHub Repository                                │ │
│ │ ✅ acme-corp-website-redesign                       │ │
│ │ 🔗 https://github.com/myorg/acme-corp-website...    │ │
│ │ Last synced: 2 hours ago         [Sync Now]        │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

---

### Project Form Modal

**Trigger**: Click "New Project" button

**Fields**:
1. **Name** (required)
2. **Code** (required, slug format, unique)
3. **Type** (radio: CLIENT / INTERNAL)
4. **Account** (dropdown, required if CLIENT)
5. **Description** (textarea)
6. **Status** (dropdown, default: PLANNED)
7. **Start Date** (date picker)
8. **Target Date** (date picker)
9. **Stack** (text input)
10. **Estimated Hours** (number)
11. **Budget** (number with currency)
12. **Owner** (user dropdown)
13. **Team** (multi-select users)
14. **Tags** (comma-separated or tag input)
15. **Links** (dynamic list: label + URL)
16. **Custom Fields** (JSON textarea or key-value editor)
17. **Enable GitHub** (checkbox, default: true)

**Validation**:
- Code: lowercase, alphanumeric + hyphens only
- If type=CLIENT, accountId required
- If type=INTERNAL, accountId must be null
- Target date >= Start date
- Estimated hours > 0

---

## Custom Fields

### Propósito

Permitir extensibilidad sin modificar schema. Cada cliente/proyecto puede tener campos específicos.

### Estructura

**DB**: Columna JSONB `custom_fields`

**Ejemplos**:
```json
{
  "contract_number": "CNT-2026-001",
  "client_priority": "high",
  "invoice_frequency": "monthly",
  "deployment_env": "AWS",
  "sla_response_time": "4h",
  "custom_field_1": "value1"
}
```

### UI

**Modo Simple**: Textarea con JSON válido
```
┌──────────────────────────────────────┐
│ Custom Fields (JSON)                 │
├──────────────────────────────────────┤
│ {                                    │
│   "contract_number": "CNT-2026-001", │
│   "client_priority": "high"          │
│ }                                    │
└──────────────────────────────────────┘
```

**Modo Avanzado (Futuro)**: Key-value editor con tipos
```
┌──────────────────────────────────────┐
│ Custom Fields                [+ Add] │
├──────────────────────────────────────┤
│ contract_number    CNT-2026-001  [x] │
│ client_priority    high          [x] │
│ sla_response_time  4h            [x] │
└──────────────────────────────────────┘
```

### Validaciones

- JSON válido al guardar
- Max 50 KB por proyecto (limitar tamaño)
- Keys: alphanumeric + underscore, max 50 chars
- Values: strings, numbers, booleans (no nested objects por ahora)

### Búsqueda

**Postgres JSONB operators**:
```sql
-- Find projects with specific custom field value
SELECT * FROM projects 
WHERE custom_fields @> '{"client_priority": "high"}';

-- Find projects with custom field key
SELECT * FROM projects 
WHERE custom_fields ? 'contract_number';
```

**Índice**:
```sql
CREATE INDEX idx_projects_custom_fields ON projects USING GIN (custom_fields);
```

---

## Integraciones

### 1. GitHub

- Auto-creación de repo al crear proyecto
- Link bidireccional: Project ↔ GitHubRepo
- Ver detalles de GitHub Integration en `/docs/github-integration.md`

### 2. Activity Log

- Cada cambio de estado registra activity
- Activity types: PROJECT_CREATED, PROJECT_UPDATED, PROJECT_STATUS_CHANGED, GITHUB_REPO_PROVISIONED, etc.
- Payload JSONB con detalles del cambio

### 3. Email Notifications (Futuro)

- Owner recibe email al cambio de status
- Team members notificados en milestones completados
- Cliente notificado al completar proyecto

### 4. Webhooks (Futuro)

- Exponer webhook endpoint para integraciones externas
- Eventos: project.created, project.status_changed, project.completed
- Payload JSON con project data

---

## Reporting

### KPIs

**Dashboard Principal**:
- Total projects by status (pie chart)
- Active projects count
- Projects completed this month
- Average completion time (days)
- Budget variance (estimated vs actual)
- Hours variance (estimated vs actual)

**Queries**:
```sql
-- Projects by status
SELECT status, COUNT(*) as count 
FROM projects 
GROUP BY status;

-- Active projects
SELECT COUNT(*) FROM projects WHERE status = 'ACTIVE';

-- Completed this month
SELECT COUNT(*) FROM projects 
WHERE status = 'DONE' 
  AND DATE_TRUNC('month', completion_date) = DATE_TRUNC('month', CURRENT_DATE);

-- Average completion time
SELECT AVG(completion_date - start_date) as avg_days
FROM projects
WHERE status = 'DONE' AND completion_date IS NOT NULL;
```

### Exports

**CSV Export**:
```
GET /api/v1/projects/export?format=csv&status=DONE&startDateFrom=2026-01-01

Response: CSV file with projects
```

**Campos en CSV**:
- ID, Name, Code, Type, Account, Status, Start Date, Target Date, Completion Date, Estimated Hours, Actual Hours, Budget, Owner, Created At

---

## Seguridad & Permisos

### Roles

| Role | Create | Read | Update | Delete |
|------|--------|------|--------|--------|
| ADMIN | ✅ All | ✅ All | ✅ All | ✅ All |
| OWNER | ❌ | ✅ Owned | ✅ Owned | ✅ Owned |
| TEAM_MEMBER | ❌ | ✅ Assigned | ⚠️ Limited | ❌ |

**Limited Update (TEAM_MEMBER)**:
- Puede actualizar: actualHours, completionDate en tasks asignadas
- No puede: cambiar status, owner, budget

### Implementación

```java
@PreAuthorize("hasRole('ADMIN') or @projectSecurity.isOwner(#id, authentication)")
public ProjectResponse updateProject(Long id, ProjectRequest request) {
    // ...
}
```

---

## Testing

### Unit Tests

- ProjectService: CRUD, validaciones, state transitions
- ProjectRepository: Custom queries, JSONB queries
- ProjectValidator: Business rules

### Integration Tests

- End-to-end: Create project → Add milestones → Change status → Complete
- GitHub integration: Create project with GitHub enabled
- Custom fields: JSONB operations

### Performance Tests

- List 1000 projects with pagination
- Filter by custom fields (JSONB queries)
- Bulk status update

---

## Futuras Mejoras

1. **Kanban Board**: Visual task management
2. **Gantt Chart**: Timeline visualization
3. **Time Tracking**: Log hours per task
4. **File Attachments**: Upload files to projects
5. **Comments**: Thread-based discussions
6. **Templates**: Project templates with pre-configured milestones/tasks
7. **Client Portal**: External access for clients to view progress
8. **Integrations**: Jira, Trello, Asana sync
9. **AI Estimates**: ML-based hour estimation
10. **Resource Management**: Team capacity planning

---

**Autor**: Staff Engineer / Tech Lead  
**Última actualización**: 2026-01-27
