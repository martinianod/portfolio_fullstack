# Clients/Accounts Management - Especificación de Features

**Repositorio**: martinianod/portfolio_fullstack  
**Fecha**: 2026-01-27  
**Versión**: 1.0

---

## Tabla de Contenidos

1. [Resumen](#resumen)
2. [Evolución: Client → Account](#evolución-client--account)
3. [Modelo de Datos](#modelo-de-datos)
4. [Contacts Management](#contacts-management)
5. [Lead to Account Conversion](#lead-to-account-conversion)
6. [Funcionalidades](#funcionalidades)
7. [API Endpoints](#api-endpoints)
8. [Frontend UI](#frontend-ui)
9. [Custom Fields](#custom-fields)
10. [Integraciones](#integraciones)

---

## Resumen

El módulo de **Clients/Accounts Management** es el corazón del CRM. Permite gestionar clientes con:

- Información completa de la cuenta
- Múltiples contactos por cuenta
- Proyectos asociados
- Timeline de actividades
- Custom fields para extensibilidad
- Conversión automática desde Leads

**Terminología**:
- **Account**: Entidad que representa una empresa o cliente
- **Contact**: Persona individual dentro de una cuenta
- **Lead**: Prospecto que aún no es cliente (puede convertirse en Account)

---

## Evolución: Client → Account

### Motivación

La entidad actual `Client` tiene limitaciones:
- Solo 1 contacto primario (`primary_contact_name`)
- No tiene slug para GitHub naming
- No soporta custom fields (extensibilidad limitada)
- Naming poco claro en contexto CRM (Account es más estándar)

### Estrategia de Migración

**Flyway Migration**: Renombrar tabla + agregar campos sin downtime

```sql
-- V3__evolve_clients_to_accounts.sql

-- 1. Rename table
ALTER TABLE clients RENAME TO accounts;

-- 2. Add new columns
ALTER TABLE accounts ADD COLUMN slug VARCHAR(255);
ALTER TABLE accounts ADD COLUMN industry VARCHAR(255);
ALTER TABLE accounts ADD COLUMN website VARCHAR(500);
ALTER TABLE accounts ADD COLUMN owner_id BIGINT;
ALTER TABLE accounts ADD COLUMN custom_fields JSONB DEFAULT '{}';

-- 3. Generate slugs from existing names
UPDATE accounts SET slug = lower(regexp_replace(name, '[^a-zA-Z0-9]+', '-', 'g'));

-- 4. Add constraints
ALTER TABLE accounts ALTER COLUMN slug SET NOT NULL;
ALTER TABLE accounts ADD CONSTRAINT uk_accounts_slug UNIQUE (slug);
ALTER TABLE accounts ADD CONSTRAINT fk_accounts_owner FOREIGN KEY (owner_id) REFERENCES users(id);

-- 5. Create index on custom_fields for performance
CREATE INDEX idx_accounts_custom_fields ON accounts USING GIN (custom_fields);

-- 6. Update FK references in projects table
ALTER TABLE projects RENAME COLUMN client_id TO account_id;
```

### Backward Compatibility

- Código existente que usa `ClientRepository` será renombrado a `AccountRepository`
- Endpoints mantienen `/api/v1/clients` (alias a accounts) para compatibilidad inicial
- Frontend muestra "Clients" en UI pero usa "accounts" internamente

---

## Modelo de Datos

### Entity: Account

```java
@Entity
@Table(name = "accounts")
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Basic Info
    @Column(nullable = false)
    private String name;                    // "ACME Corporation"
    
    @Column(nullable = false, unique = true)
    private String slug;                    // "acme-corporation" (for GitHub naming)
    
    @Column(nullable = false)
    private String email;                   // Main contact email
    
    private String phone;
    
    private String company;                 // Company legal name (puede diferir de name)
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    // Categorization
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatus status;           // ACTIVE, INACTIVE, SUSPENDED
    
    private String industry;                // "Technology", "Healthcare", etc.
    
    private String website;                 // "https://acme.com"
    
    private String tags;                    // Comma-separated: "enterprise,saas,priority"
    
    // Ownership & Management
    @Column(name = "owner_id")
    private Long ownerId;                   // FK to User (account manager)
    
    // Notes & Documentation
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    // Extensibility
    @Column(columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private Map<String, Object> customFields;
    
    // Lead Conversion Tracking
    @Column(name = "created_from_lead_id")
    private Long createdFromLeadId;         // FK to Lead (nullable)
    
    // Audit
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (slug == null && name != null) {
            slug = generateSlug(name);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper method
    private String generateSlug(String name) {
        return name.toLowerCase()
                   .replaceAll("[^a-z0-9\\s-]", "")
                   .replaceAll("\\s+", "-")
                   .replaceAll("-+", "-")
                   .replaceAll("^-|-$", "");
    }
}
```

### Enum: AccountStatus

```java
public enum AccountStatus {
    ACTIVE,      // Active paying customer
    INACTIVE,    // Former customer, not currently engaged
    SUSPENDED    // Account suspended (payment issues, etc.)
}
```

### Validaciones

```java
@AssertTrue(message = "Slug must be lowercase and contain only alphanumeric characters and hyphens")
private boolean isSlugValid() {
    if (slug == null) return true;
    return slug.matches("^[a-z0-9-]+$");
}

@AssertTrue(message = "Website must be a valid URL")
private boolean isWebsiteValid() {
    if (website == null) return true;
    return website.matches("^https?://.*");
}
```

---

## Contacts Management

### Entity: Contact

```java
@Entity
@Table(name = "contacts")
@Data
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "account_id")
    private Long accountId;                 // FK to Account (CASCADE DELETE)
    
    @Column(nullable = false, name = "first_name")
    private String firstName;
    
    @Column(nullable = false, name = "last_name")
    private String lastName;
    
    @Column(nullable = false)
    private String email;
    
    private String phone;
    
    private String position;                // Job title: "CEO", "CTO", "Project Manager"
    
    @Column(nullable = false, name = "is_primary")
    private Boolean isPrimary = false;      // Only 1 primary contact per account
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(nullable = false, name = "updated_at")
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
    
    // Helper method
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
```

### Reglas de Negocio

1. **Primary Contact**:
   - Solo 1 contacto puede ser primary por account
   - Al marcar uno como primary, desmarcar el anterior
   - Al eliminar account, cascade delete todos los contacts

2. **Validaciones**:
   - Email único por account (no puede haber 2 contactos con mismo email en la misma cuenta)
   - Al menos 1 contacto por account (validar antes de eliminar)
   - firstName y lastName requeridos

3. **Migration**:
   - Migrar `primary_contact_name` de accounts a primer contact
   - Si account tiene `primary_contact_name`, crear Contact con isPrimary=true

```sql
-- V4__create_contacts_table.sql

CREATE TABLE contacts (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    position VARCHAR(255),
    is_primary BOOLEAN NOT NULL DEFAULT false,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contacts_account_id ON contacts(account_id);
CREATE INDEX idx_contacts_email ON contacts(email);
CREATE INDEX idx_contacts_is_primary ON contacts(account_id, is_primary) WHERE is_primary = true;

-- Migrate existing primary_contact_name to contacts
INSERT INTO contacts (account_id, first_name, last_name, email, is_primary, created_at, updated_at)
SELECT 
    id as account_id,
    SPLIT_PART(primary_contact_name, ' ', 1) as first_name,
    COALESCE(SPLIT_PART(primary_contact_name, ' ', 2), '') as last_name,
    email,
    true as is_primary,
    created_at,
    updated_at
FROM accounts
WHERE primary_contact_name IS NOT NULL AND primary_contact_name != '';

-- Remove old column
ALTER TABLE accounts DROP COLUMN primary_contact_name;
```

---

## Lead to Account Conversion

### Flujo de Conversión

```
Lead (stage=WON) → Convertir → Account + Contact
    ├─ Crea Account con datos del Lead
    ├─ Crea Contact con nombre y email del Lead
    ├─ Marca Contact como isPrimary=true
    ├─ Actualiza Lead.stage = WON (si no lo era)
    ├─ Vincula Account.createdFromLeadId = Lead.id
    └─ Log Activity: LEAD_CONVERTED_TO_ACCOUNT
```

### Service: LeadConversionService

```java
@Service
public class LeadConversionService {
    
    @Transactional
    public ConversionResult convertLeadToAccount(Long leadId) {
        // 1. Buscar lead
        Lead lead = leadRepository.findById(leadId)
            .orElseThrow(() -> new NotFoundException("Lead not found"));
        
        // 2. Validar que no esté ya convertido
        if (accountRepository.existsByCreatedFromLeadId(leadId)) {
            throw new BusinessException("Lead already converted");
        }
        
        // 3. Crear Account
        Account account = new Account();
        account.setName(lead.getCompany() != null ? lead.getCompany() : lead.getName());
        account.setEmail(lead.getEmail());
        account.setPhone(lead.getPhone());
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedFromLeadId(leadId);
        account.setOwnerId(lead.getAssignedTo() != null ? lead.getAssignedTo().getId() : null);
        
        // Custom fields from lead
        Map<String, Object> customFields = new HashMap<>();
        if (lead.getBudgetRange() != null) {
            customFields.put("budget_range", lead.getBudgetRange());
        }
        if (lead.getProjectType() != null) {
            customFields.put("project_type", lead.getProjectType());
        }
        account.setCustomFields(customFields);
        
        Account savedAccount = accountRepository.save(account);
        
        // 4. Crear Contact
        Contact contact = new Contact();
        contact.setAccountId(savedAccount.getId());
        
        // Parse name (simple split)
        String[] nameParts = lead.getName().split(" ", 2);
        contact.setFirstName(nameParts[0]);
        contact.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        
        contact.setEmail(lead.getEmail());
        contact.setPhone(lead.getPhone());
        contact.setIsPrimary(true);
        
        Contact savedContact = contactRepository.save(contact);
        
        // 5. Actualizar Lead stage
        if (!lead.getStage().equals("WON")) {
            lead.setStage("WON");
            leadRepository.save(lead);
        }
        
        // 6. Log Activity
        activityService.log(
            "LEAD_CONVERTED_TO_ACCOUNT",
            "Lead",
            leadId,
            "Converted to Account: " + savedAccount.getName(),
            Map.of(
                "accountId", savedAccount.getId(),
                "contactId", savedContact.getId()
            )
        );
        
        activityService.log(
            "ACCOUNT_CREATED",
            "Account",
            savedAccount.getId(),
            "Account created from Lead conversion",
            Map.of("leadId", leadId)
        );
        
        return new ConversionResult(savedAccount, savedContact);
    }
}
```

### UI: Conversion Wizard

**Trigger**: Button "Convert to Account" en Lead detail page

**Steps**:
1. **Confirm Lead Data**: Mostrar datos del lead, permitir editar antes de convertir
2. **Account Details**: Name, company, slug (auto-generated), industry, website
3. **Contact Details**: First name, last name, position
4. **Confirmation**: Review y confirm

**Validaciones**:
- Solo leads con stage=QUALIFIED, PROPOSAL, NEGOTIATION, o WON pueden convertirse
- Slug debe ser único (verificar en tiempo real)

---

## Funcionalidades

### 1. CRUD Accounts

#### Create Account

```json
POST /api/v1/accounts
{
  "name": "ACME Corporation",
  "slug": "acme-corp",
  "email": "contact@acme.com",
  "phone": "+1-555-1234",
  "company": "ACME Corp Inc.",
  "address": "123 Main St, San Francisco, CA",
  "status": "ACTIVE",
  "industry": "Technology",
  "website": "https://acme.com",
  "tags": "enterprise,saas,priority",
  "ownerId": 1,
  "notes": "Important client from 2025",
  "customFields": {
    "contract_value": 100000,
    "renewal_date": "2027-01-01",
    "payment_terms": "NET30"
  }
}

Response 201:
{
  "id": 123,
  "name": "ACME Corporation",
  "slug": "acme-corp",
  "status": "ACTIVE",
  "createdAt": "2026-01-27T12:00:00Z"
}
```

#### Get Account Detail

```json
GET /api/v1/accounts/123

Response 200:
{
  "id": 123,
  "name": "ACME Corporation",
  "slug": "acme-corp",
  "email": "contact@acme.com",
  "phone": "+1-555-1234",
  "status": "ACTIVE",
  "industry": "Technology",
  "website": "https://acme.com",
  "owner": {
    "id": 1,
    "fullName": "John Doe",
    "email": "john@example.com"
  },
  "contacts": [
    {
      "id": 456,
      "fullName": "Jane Smith",
      "email": "jane@acme.com",
      "position": "CTO",
      "isPrimary": true
    }
  ],
  "projects": [
    {
      "id": 789,
      "name": "Website Redesign",
      "status": "ACTIVE",
      "progress": 37.5
    }
  ],
  "stats": {
    "totalProjects": 3,
    "activeProjects": 1,
    "completedProjects": 2,
    "totalRevenue": 250000
  },
  "customFields": {
    "contract_value": 100000,
    "renewal_date": "2027-01-01"
  },
  "createdAt": "2025-06-15T10:00:00Z",
  "createdFromLead": {
    "id": 99,
    "name": "ACME inquiry"
  }
}
```

#### Update Account

```json
PUT /api/v1/accounts/123
{
  "name": "ACME Corporation Inc.",
  "website": "https://acme-corp.com",
  "notes": "Renewed contract for 2 more years"
}

Response 200: (updated account)
```

#### List Accounts

```json
GET /api/v1/accounts?status=ACTIVE&industry=Technology&search=acme&page=0&size=20

Response 200:
{
  "content": [
    {
      "id": 123,
      "name": "ACME Corporation",
      "slug": "acme-corp",
      "status": "ACTIVE",
      "industry": "Technology",
      "owner": "John Doe",
      "projectsCount": 3,
      "createdAt": "2025-06-15T10:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

**Filtros**:
- `status`: ACTIVE, INACTIVE, SUSPENDED
- `industry`: String match
- `ownerId`: Filter by account manager
- `tags`: Filter by tag
- `search`: Search in name, email, company, slug
- `createdFrom`, `createdTo`: Date range

---

### 2. CRUD Contacts

#### Create Contact

```json
POST /api/v1/accounts/123/contacts
{
  "firstName": "Bob",
  "lastName": "Johnson",
  "email": "bob@acme.com",
  "phone": "+1-555-5678",
  "position": "Product Manager",
  "isPrimary": false,
  "notes": "Technical POC"
}

Response 201: (contact)
```

#### Set Primary Contact

```json
PATCH /api/v1/contacts/456/set-primary

Response 200:
{
  "id": 456,
  "isPrimary": true,
  "message": "Contact set as primary. Previous primary contact updated."
}
```

**Logic**:
- Buscar account_id del contact
- Desmarcar isPrimary de todos los otros contacts de esa account
- Marcar este contact como isPrimary=true

#### Update Contact

```json
PUT /api/v1/contacts/456
{
  "position": "VP of Engineering",
  "phone": "+1-555-9999"
}

Response 200: (updated contact)
```

#### Delete Contact

```json
DELETE /api/v1/contacts/456

Response 204 (no content)
```

**Validaciones**:
- No se puede eliminar el único contacto de una account
- Si era primary, promover otro contacto automáticamente

---

### 3. Lead Conversion

```json
POST /api/v1/accounts/convert-lead/99

Response 201:
{
  "account": {
    "id": 123,
    "name": "ACME Corporation",
    "slug": "acme-corp"
  },
  "contact": {
    "id": 456,
    "fullName": "Jane Smith",
    "isPrimary": true
  },
  "lead": {
    "id": 99,
    "stage": "WON"
  }
}
```

---

## API Endpoints

### Accounts

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/accounts` | Create account | ADMIN |
| GET | `/api/v1/accounts` | List accounts (with filters) | ADMIN |
| GET | `/api/v1/accounts/{id}` | Get account detail | ADMIN/Owner |
| PUT | `/api/v1/accounts/{id}` | Update account | ADMIN/Owner |
| DELETE | `/api/v1/accounts/{id}` | Delete account | ADMIN |
| POST | `/api/v1/accounts/convert-lead/{leadId}` | Convert lead to account | ADMIN |
| GET | `/api/v1/accounts/{id}/activity` | Activity timeline | ADMIN/Owner |

### Contacts

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/accounts/{accountId}/contacts` | Create contact | ADMIN/Owner |
| GET | `/api/v1/accounts/{accountId}/contacts` | List contacts | ADMIN/Owner |
| GET | `/api/v1/contacts/{id}` | Get contact detail | ADMIN/Owner |
| PUT | `/api/v1/contacts/{id}` | Update contact | ADMIN/Owner |
| DELETE | `/api/v1/contacts/{id}` | Delete contact | ADMIN/Owner |
| PATCH | `/api/v1/contacts/{id}/set-primary` | Set as primary | ADMIN/Owner |

---

## Frontend UI

### Accounts List Page

**URL**: `/admin/clients`

**Components**:
- Search bar (name, email, company)
- Filters: Status, Industry, Owner, Tags
- Sort: Name, Created Date, # Projects
- Table columns:
  - Name (with industry icon)
  - Status badge
  - Owner
  - # Projects (active/total)
  - # Contacts
  - Created date
  - Actions (view, edit, delete)
- Pagination
- Action button: "New Account" + "Convert Lead"

**Wireframe**:
```
┌─────────────────────────────────────────────────────────┐
│ Clients                      [Convert Lead] [+ New]      │
├─────────────────────────────────────────────────────────┤
│ Filters: [Status ▼] [Industry ▼] [Owner ▼]  🔍 Search   │
│                                                         │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Name           Status   Owner    Projects  Contacts │ │
│ ├─────────────────────────────────────────────────────┤ │
│ │ 🏢 ACME Corp   ACTIVE   John     1/3       2        │ │
│ │ Technology                                          │ │
│ │                                                     │ │
│ │ 🏢 TechStart   ACTIVE   Jane     0/1       1        │ │
│ │ SaaS                                                │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

---

### Account Detail Page

**URL**: `/admin/clients/123`

**Tabs**:

#### 1. Overview
- Account name, slug, status, industry
- Owner info
- Contact info (email, phone, website)
- Notes (editable)
- Custom fields (JSON display or key-value)
- Stats: Total projects, active, completed, revenue

#### 2. Contacts
- Table of contacts with columns:
  - Name
  - Email
  - Phone
  - Position
  - Primary badge
  - Actions (edit, delete, set primary)
- Button: "Add Contact"

#### 3. Projects
- List of projects associated with this account
- Filter by status
- Link to project detail
- Button: "New Project"

#### 4. Timeline
- Activity log (reverse chronological)
- Filter by activity type
- Show: account created, updated, contact added, project created, etc.

**Wireframe**:
```
┌─────────────────────────────────────────────────────────┐
│ ← Back to Clients                                        │
│                                                         │
│ 🏢 ACME Corporation                     [Edit] [Delete]  │
│ acme-corp • Technology • ACTIVE                         │
├─────────────────────────────────────────────────────────┤
│ [Overview] [Contacts] [Projects] [Timeline]             │
│                                                         │
│ ┌──────────────────┐ ┌──────────────────┐              │
│ │ Owner            │ │ Stats            │              │
│ │ John Doe         │ │ 3 Projects       │              │
│ │ john@example.com │ │ 1 Active         │              │
│ └──────────────────┘ │ 2 Completed      │              │
│                      │ $250K Revenue    │              │
│ ┌──────────────────┐ └──────────────────┘              │
│ │ Contact          │                                   │
│ │ ✉ contact@acme   │                                   │
│ │ ☎ +1-555-1234    │                                   │
│ │ 🌐 acme.com      │                                   │
│ └──────────────────┘                                   │
│                                                         │
│ Notes:                                                  │
│ Important client from 2025. Renewed contract...         │
└─────────────────────────────────────────────────────────┘
```

---

### Account Form Modal

**Fields**:
1. Name (required)
2. Slug (auto-generated, editable)
3. Email (required)
4. Phone
5. Company (legal name)
6. Address (textarea)
7. Status (dropdown: ACTIVE/INACTIVE/SUSPENDED)
8. Industry (dropdown or text input)
9. Website (URL)
10. Tags (comma-separated or tag input)
11. Owner (user dropdown)
12. Notes (textarea)
13. Custom Fields (JSON textarea or key-value editor)

---

### Convert Lead Modal

**Trigger**: Button "Convert Lead" en Accounts list

**Steps**:
1. **Select Lead**: Dropdown with qualified leads (stage >= QUALIFIED, not converted)
2. **Review Lead Data**: Show lead info, allow edit
3. **Account Details**: Pre-filled from lead, allow edit (name, company, slug, industry)
4. **Contact Details**: Pre-filled (first/last name from lead.name, email, phone)
5. **Confirmation**: Review all data, click "Convert"

**Validation**:
- Slug must be unique
- Lead must not be already converted

---

## Custom Fields

### Ejemplos de Use Cases

**Technology Client**:
```json
{
  "tech_stack_preference": "React, Node.js",
  "deployment_platform": "AWS",
  "contract_type": "Fixed Price"
}
```

**Healthcare Client**:
```json
{
  "compliance_requirements": "HIPAA",
  "data_residency": "US",
  "security_audit_date": "2026-03-01"
}
```

**Enterprise Client**:
```json
{
  "contract_value": 500000,
  "renewal_date": "2027-12-31",
  "payment_terms": "NET60",
  "escalation_contact": "ceo@client.com"
}
```

### Búsqueda por Custom Fields

```sql
-- Find accounts with specific custom field
SELECT * FROM accounts 
WHERE custom_fields @> '{"compliance_requirements": "HIPAA"}';

-- Find accounts with high contract value
SELECT * FROM accounts 
WHERE (custom_fields->>'contract_value')::numeric > 100000;
```

**API**:
```
GET /api/v1/accounts?customFields.compliance_requirements=HIPAA
```

---

## Integraciones

### 1. Leads

- Lead puede convertirse en Account + Contact
- Vinculación bidireccional: Account.createdFromLeadId

### 2. Projects

- Account tiene muchos Projects
- Project list filtrable por accountId
- Account detail muestra proyectos

### 3. Activity Log

- Cada cambio registra activity
- Activity types: ACCOUNT_CREATED, ACCOUNT_UPDATED, CONTACT_ADDED, CONTACT_UPDATED, CONTACT_PRIMARY_CHANGED, PROJECT_CREATED, etc.

### 4. Email Notifications (Futuro)

- Owner notificado al crear nueva account
- Primary contact recibe email de bienvenida
- Alertas de renewal date (custom field)

---

## Reporting

### KPIs

**Dashboard Accounts**:
- Total accounts by status (pie chart)
- New accounts this month
- Accounts by industry (bar chart)
- Top accounts by revenue (custom field aggregation)
- Conversion rate: Leads → Accounts

**Queries**:
```sql
-- Accounts by status
SELECT status, COUNT(*) FROM accounts GROUP BY status;

-- New accounts this month
SELECT COUNT(*) FROM accounts 
WHERE DATE_TRUNC('month', created_at) = DATE_TRUNC('month', CURRENT_DATE);

-- Accounts by industry
SELECT industry, COUNT(*) FROM accounts GROUP BY industry ORDER BY COUNT(*) DESC LIMIT 10;

-- Top revenue accounts (custom field)
SELECT name, (custom_fields->>'contract_value')::numeric as value
FROM accounts
WHERE custom_fields ? 'contract_value'
ORDER BY value DESC
LIMIT 10;
```

---

## Seguridad & Permisos

### Roles

| Role | Create | Read | Update | Delete |
|------|--------|------|--------|--------|
| ADMIN | ✅ All | ✅ All | ✅ All | ✅ All |
| OWNER | ❌ | ✅ Owned | ✅ Owned | ❌ |

**Owner**: Account.ownerId == User.id

### Implementación

```java
@PreAuthorize("hasRole('ADMIN') or @accountSecurity.isOwner(#id, authentication)")
public AccountResponse updateAccount(Long id, AccountRequest request) {
    // ...
}
```

---

## Testing

### Unit Tests

- AccountService: CRUD, slug generation, conversion
- ContactService: CRUD, primary contact logic
- LeadConversionService: Conversion flow

### Integration Tests

- End-to-end: Create lead → Convert to account → Add contact → Create project
- Custom fields: JSONB queries
- Primary contact: Set primary, verify previous is unmarked

---

## Futuras Mejoras

1. **Account Hierarchies**: Parent/child accounts (subsidiaries)
2. **Contact Roles**: Multiple roles per contact (Technical, Billing, Executive)
3. **Communication History**: Log emails, calls, meetings
4. **Document Management**: Upload contracts, SOWs, NDAs
5. **Revenue Tracking**: Detailed billing, invoices, payments
6. **Opportunities**: Sales pipeline before conversion
7. **Account Scoring**: Lead scoring algorithm
8. **Integrations**: Salesforce, HubSpot sync
9. **Territory Management**: Geographic assignment of accounts
10. **Client Portal**: Self-service portal for clients

---

**Autor**: Staff Engineer / Tech Lead  
**Última actualización**: 2026-01-27
