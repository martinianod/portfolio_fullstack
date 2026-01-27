# GitHub Integration - Especificación Técnica

**Repositorio**: martinianod/portfolio_fullstack  
**Fecha**: 2026-01-27  
**Versión**: 1.0

---

## Tabla de Contenidos

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Objetivos](#objetivos)
3. [Arquitectura](#arquitectura)
4. [Componentes](#componentes)
5. [Flujos de Trabajo](#flujos-de-trabajo)
6. [Configuración](#configuración)
7. [API Endpoints](#api-endpoints)
8. [Seguridad](#seguridad)
9. [Manejo de Errores](#manejo-de-errores)
10. [Testing](#testing)
11. [Deployment](#deployment)

---

## Resumen Ejecutivo

La integración con GitHub permite la creación automática de repositorios al crear proyectos en el sistema. Esta funcionalidad es fundamental para:

- **Automatización**: Eliminar pasos manuales en la creación de repos
- **Estandarización**: Aplicar naming conventions y estructura consistente
- **Trazabilidad**: Vincular proyectos con su código desde el inicio
- **Eficiencia**: Reducir tiempo de setup de 30 minutos a segundos

**Alcance MVP**: GitHub Personal Access Token (PAT) con provisión básica de repos.

**Futuro**: GitHub App, webhooks, CI/CD templates, branch sync.

---

## Objetivos

### Funcionales

1. **Auto-creación de Repositorios**: Al crear un proyecto con flag `enableGithub=true`, crear repo automáticamente
2. **Naming Estándar**: Aplicar convención `{account-slug}-{project-code}` o `internal-{project-code}`
3. **Template Support**: Opcionalmente inicializar desde template repo
4. **Metadata Tracking**: Almacenar URL, branch, status en DB
5. **Error Resilience**: Si GitHub falla, el proyecto se crea igual con status FAILED
6. **Retry Mechanism**: Permitir reintentar provisión fallida
7. **Health Check**: Endpoint para verificar conectividad con GitHub API

### No Funcionales

1. **Seguridad**: Tokens en env vars, nunca en código o DB
2. **Idempotencia**: Llamadas múltiples no crean repos duplicados
3. **Performance**: Provisión en <5 segundos (95th percentile)
4. **Observability**: Logs estructurados con tracing
5. **Graceful Degradation**: Sistema funciona aunque GitHub esté down

---

## Arquitectura

### Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────┐
│                    ProjectService                       │
│                                                         │
│  createProject(request):                                │
│    ├─ Validate input                                    │
│    ├─ Persist Project entity                            │
│    ├─ IF enableGithub:                                  │
│    │   └─ Call GitHubProvisioningService               │
│    └─ Log Activity                                      │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│           GitHubProvisioningService                     │
│                                                         │
│  provisionRepository(project, account):                 │
│    ├─ Generate repo name                                │
│    ├─ Check if repo exists (idempotency)                │
│    ├─ Create repo via GitHubClient                      │
│    ├─ Apply template (if configured)                    │
│    ├─ Add topics/tags                                   │
│    ├─ Update README                                     │
│    ├─ Set branch protection (if enabled)                │
│    ├─ Persist GitHubRepo entity                         │
│    └─ Update Project.githubRepoId                       │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│                  GitHubClient                           │
│                                                         │
│  HTTP Client wrapper para GitHub REST API v3            │
│    ├─ createRepository()                                │
│    ├─ createFromTemplate()                              │
│    ├─ getRepository()                                   │
│    ├─ updateRepository()                                │
│    ├─ addTopics()                                       │
│    ├─ setBranchProtection()                             │
│    └─ Retry logic + rate limit handling                 │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
            ┌──────────────────┐
            │   GitHub API     │
            │  REST API v3     │
            └──────────────────┘
```

### Modelo de Datos

**Entidad GitHubRepo**:
```sql
CREATE TABLE github_repos (
    id BIGSERIAL PRIMARY KEY,
    repo_full_name VARCHAR(255) NOT NULL UNIQUE,  -- 'owner/repo'
    repo_url VARCHAR(500) NOT NULL,
    default_branch VARCHAR(100) DEFAULT 'main',
    status VARCHAR(50) NOT NULL,  -- PROVISIONING, ACTIVE, FAILED, ARCHIVED
    provisioning_error TEXT,
    provisioned_at TIMESTAMP,
    last_sync_at TIMESTAMP,
    metadata JSONB,  -- {stars, forks, language, topics, etc}
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_github_repos_status ON github_repos(status);
CREATE INDEX idx_github_repos_full_name ON github_repos(repo_full_name);
```

**Relación con Project**:
```sql
ALTER TABLE projects ADD COLUMN github_repo_id BIGINT;
ALTER TABLE projects ADD CONSTRAINT fk_projects_github_repo 
    FOREIGN KEY (github_repo_id) REFERENCES github_repos(id) ON DELETE SET NULL;
```

---

## Componentes

### 1. GitHubConfigProperties

**Propósito**: Configuración centralizada desde env vars.

```java
@Configuration
@ConfigurationProperties(prefix = "github")
@Data
public class GitHubConfigProperties {
    private Boolean enabled = false;
    private String provider = "PAT";  // PAT or APP
    private String token;
    private String org;  // Organization or username
    private String templateRepo;  // Optional: 'owner/repo'
    private Boolean defaultPrivate = true;
    private Boolean autoInit = true;
    private Boolean branchProtectionEnabled = false;
}
```

**Validaciones**:
- Si `enabled=true`, `token` y `org` son requeridos
- `templateRepo` debe tener formato `owner/repo` si está presente

---

### 2. GitHubClient

**Propósito**: Wrapper HTTP para GitHub REST API v3.

**Métodos Principales**:

```java
@Service
public class GitHubClient {
    
    /**
     * Crea un repositorio nuevo.
     * @throws GitHubApiException si falla la creación
     */
    public GitHubRepository createRepository(CreateRepoRequest request);
    
    /**
     * Crea un repositorio desde template.
     * @throws GitHubApiException si falla
     */
    public GitHubRepository createFromTemplate(
        String templateOwner, 
        String templateRepo, 
        String newRepoName, 
        boolean isPrivate
    );
    
    /**
     * Obtiene información de un repo existente.
     * @throws GitHubApiException si no existe o no hay acceso
     */
    public GitHubRepository getRepository(String owner, String repo);
    
    /**
     * Agrega topics al repositorio.
     */
    public void addTopics(String owner, String repo, List<String> topics);
    
    /**
     * Configura branch protection.
     */
    public void setBranchProtection(
        String owner, 
        String repo, 
        String branch, 
        BranchProtectionRule rule
    );
}
```

**Manejo de Errores**:
- `401 Unauthorized`: Token inválido o expirado
- `403 Forbidden`: Permisos insuficientes
- `404 Not Found`: Repo o template no existe
- `422 Unprocessable`: Validación falla (ej: nombre duplicado)
- `429 Too Many Requests`: Rate limit excedido → retry con backoff

**Retry Logic**:
```java
@Retryable(
    value = {GitHubRateLimitException.class, GitHubServerException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 2000, multiplier = 2)
)
```

---

### 3. GitHubProvisioningService

**Propósito**: Orquesta la creación de repos y registro en DB.

**Método Principal**:

```java
@Service
public class GitHubProvisioningService {
    
    @Transactional
    public GitHubRepo provisionRepository(Project project, Account account) {
        // 1. Generar nombre del repo
        String repoName = generateRepoName(project, account);
        
        // 2. Verificar si ya existe (idempotencia)
        Optional<GitHubRepository> existing = checkIfExists(repoName);
        if (existing.isPresent()) {
            return registerExistingRepo(existing.get(), project);
        }
        
        // 3. Crear repo
        try {
            GitHubRepository ghRepo = createNewRepo(repoName, project);
            
            // 4. Aplicar configuraciones
            if (configProperties.getTemplateRepo() != null) {
                applyTemplate(ghRepo);
            }
            addTopicsFromProject(ghRepo, project);
            updateReadme(ghRepo, project, account);
            
            if (configProperties.getBranchProtectionEnabled()) {
                setBranchProtection(ghRepo);
            }
            
            // 5. Persistir en DB
            GitHubRepo entity = new GitHubRepo();
            entity.setRepoFullName(ghRepo.getFullName());
            entity.setRepoUrl(ghRepo.getHtmlUrl());
            entity.setDefaultBranch(ghRepo.getDefaultBranch());
            entity.setStatus("ACTIVE");
            entity.setProvisionedAt(LocalDateTime.now());
            
            GitHubRepo saved = githubRepoRepository.save(entity);
            
            // 6. Vincular con proyecto
            project.setGithubRepoId(saved.getId());
            projectRepository.save(project);
            
            // 7. Log activity
            activityService.log("GITHUB_REPO_PROVISIONED", project, saved);
            
            return saved;
            
        } catch (GitHubApiException e) {
            // Crear registro con status FAILED
            return registerFailedProvisioning(project, repoName, e);
        }
    }
    
    /**
     * Reintentar provisión fallida.
     */
    public GitHubRepo retryProvisioning(Long projectId);
    
    /**
     * Sincronizar metadata desde GitHub (stars, forks, etc).
     */
    public GitHubRepo syncMetadata(Long projectId);
}
```

**Naming Convention**:
```java
private String generateRepoName(Project project, Account account) {
    if (project.getType().equals("INTERNAL")) {
        return "internal-" + project.getCode();
    } else {
        String accountSlug = account.getSlug();
        String projectCode = project.getCode();
        return accountSlug + "-" + projectCode;
    }
}
```

**Generación de Slug**:
```java
public static String generateSlug(String name) {
    return name.toLowerCase()
               .replaceAll("[^a-z0-9\\s-]", "")  // Remove special chars
               .replaceAll("\\s+", "-")           // Replace spaces with hyphens
               .replaceAll("-+", "-")             // Collapse multiple hyphens
               .replaceAll("^-|-$", "");          // Trim hyphens
}
// Example: "ACME Corporation!" → "acme-corporation"
```

---

### 4. GitHubIntegrationController

**Propósito**: Endpoints REST para gestionar integración.

```java
@RestController
@RequestMapping("/api/v1/integrations/github")
public class GitHubIntegrationController {
    
    /**
     * Health check de la integración.
     * GET /api/v1/integrations/github/health
     */
    @GetMapping("/health")
    public ResponseEntity<GitHubHealthResponse> health();
    
    /**
     * Test de conexión con GitHub API.
     * POST /api/v1/integrations/github/test
     */
    @PostMapping("/test")
    public ResponseEntity<GitHubTestResponse> testConnection();
    
    /**
     * Provisionar repo manualmente para un proyecto.
     * POST /api/v1/projects/{id}/github/provision
     */
    @PostMapping("/projects/{id}/github/provision")
    public ResponseEntity<GitHubRepoResponse> provisionRepo(@PathVariable Long id);
    
    /**
     * Reintentar provisión fallida.
     * POST /api/v1/projects/{id}/github/retry
     */
    @PostMapping("/projects/{id}/github/retry")
    public ResponseEntity<GitHubRepoResponse> retryProvisioning(@PathVariable Long id);
    
    /**
     * Sincronizar metadata del repo.
     * POST /api/v1/projects/{id}/github/sync
     */
    @PostMapping("/projects/{id}/github/sync")
    public ResponseEntity<GitHubRepoResponse> syncMetadata(@PathVariable Long id);
}
```

---

### 5. GitHubHealthIndicator

**Propósito**: Custom health indicator para Actuator.

```java
@Component
public class GitHubHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        if (!configProperties.getEnabled()) {
            return Health.unknown()
                         .withDetail("status", "disabled")
                         .build();
        }
        
        try {
            // Intentar llamada simple a GitHub API
            gitHubClient.testConnection();
            return Health.up()
                         .withDetail("status", "connected")
                         .withDetail("org", configProperties.getOrg())
                         .build();
        } catch (Exception e) {
            return Health.down()
                         .withDetail("status", "connection_failed")
                         .withDetail("error", e.getMessage())
                         .build();
        }
    }
}
```

Accesible en: `GET /actuator/health/github`

---

## Flujos de Trabajo

### Flujo 1: Creación de Proyecto con GitHub

```
Usuario → Frontend: Crea proyecto con "Enable GitHub" checked
    │
    ▼
Frontend → Backend: POST /api/v1/projects
    {
      "name": "Website Redesign",
      "code": "website-redesign",
      "type": "CLIENT",
      "accountId": 123,
      "enableGithub": true,
      ...
    }
    │
    ▼
ProjectService:
    ├─ Valida request
    ├─ Persiste Project (status=PLANNED, githubRepoId=null)
    ├─ Llama GitHubProvisioningService.provisionRepository()
    │   │
    │   ▼
    │   GitHubProvisioningService:
    │       ├─ Genera nombre: "acme-corp-website-redesign"
    │       ├─ Verifica existencia (idempotencia)
    │       ├─ Crea repo via GitHub API
    │       ├─ Aplica template (si configurado)
    │       ├─ Agrega topics: ["client-project", "website"]
    │       ├─ Actualiza README con metadata
    │       ├─ Persiste GitHubRepo (status=ACTIVE)
    │       └─ Actualiza Project.githubRepoId
    │
    └─ Log Activity: PROJECT_CREATED + GITHUB_REPO_PROVISIONED
    │
    ▼
Backend → Frontend: 201 Created
    {
      "id": 456,
      "name": "Website Redesign",
      "githubRepo": {
        "id": 789,
        "repoFullName": "myorg/acme-corp-website-redesign",
        "repoUrl": "https://github.com/myorg/acme-corp-website-redesign",
        "status": "ACTIVE"
      }
    }
    │
    ▼
Frontend: Muestra success toast + redirige a project detail
Frontend: Badge verde "✅ Repository Created" con link
```

### Flujo 2: Provisión Falla (GitHub Down)

```
ProjectService.create() → GitHubProvisioningService
    │
    ▼
GitHubClient.createRepository()
    │
    └─ GitHub API Error: 503 Service Unavailable
    │
    ▼
GitHubProvisioningService catches exception:
    ├─ Crea GitHubRepo entity con status=FAILED
    ├─ Guarda error en provisioningError field
    ├─ NO hace rollback del Project (continúa)
    └─ Log Activity: GITHUB_REPO_FAILED
    │
    ▼
Backend → Frontend: 201 Created (proyecto creado igual)
    {
      "id": 456,
      "githubRepo": {
        "status": "FAILED",
        "provisioningError": "GitHub API unavailable: 503 Service Unavailable"
      }
    }
    │
    ▼
Frontend: Badge rojo "❌ Repository Creation Failed"
Frontend: Botón "Retry" visible
```

### Flujo 3: Retry Provisión

```
Usuario → Frontend: Click "Retry" en project detail
    │
    ▼
Frontend → Backend: POST /api/v1/projects/456/github/retry
    │
    ▼
GitHubProvisioningService.retryProvisioning(456):
    ├─ Busca GitHubRepo con status=FAILED
    ├─ Reintenta creación de repo
    ├─ Si éxito: actualiza status=ACTIVE, limpia error
    ├─ Si falla nuevamente: mantiene FAILED, actualiza error
    └─ Log Activity
    │
    ▼
Backend → Frontend: 200 OK con nuevo status
    │
    ▼
Frontend: Actualiza badge según resultado
```

---

## Configuración

### Environment Variables

```bash
# GitHub Integration (Requeridas si GITHUB_ENABLED=true)
GITHUB_ENABLED=true
GITHUB_PROVIDER=PAT
GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
GITHUB_ORG=myorganization

# GitHub Integration (Opcionales)
GITHUB_TEMPLATE_REPO=myorganization/project-template
GITHUB_DEFAULT_PRIVATE=true
GITHUB_AUTO_INIT=true
GITHUB_BRANCH_PROTECTION_ENABLED=false
```

### application.properties / application.yml

```yaml
github:
  enabled: ${GITHUB_ENABLED:false}
  provider: ${GITHUB_PROVIDER:PAT}
  token: ${GITHUB_TOKEN:}
  org: ${GITHUB_ORG:}
  template-repo: ${GITHUB_TEMPLATE_REPO:}
  default-private: ${GITHUB_DEFAULT_PRIVATE:true}
  auto-init: ${GITHUB_AUTO_INIT:true}
  branch-protection-enabled: ${GITHUB_BRANCH_PROTECTION_ENABLED:false}
```

### Setup de Token PAT

**Scopes Requeridos**:
- `repo` (full control of private repositories)
- `admin:org` (si se crean repos en organization)

**Pasos**:
1. GitHub → Settings → Developer settings → Personal access tokens
2. Generate new token (classic)
3. Seleccionar scopes: `repo`, `admin:org` (si aplica)
4. Copiar token (se muestra solo una vez)
5. Agregar a `.env`: `GITHUB_TOKEN=ghp_xxxxx`

**Rotación de Tokens**:
- Tokens PAT no expiran si no se configura
- Recomendado: configurar expiración de 90 días
- Documentar proceso de rotación en docs/DEPLOYMENT_GUIDE.md

---

## API Endpoints

### Health Check

```
GET /api/v1/integrations/github/health
```

**Response 200 OK**:
```json
{
  "status": "UP",
  "enabled": true,
  "provider": "PAT",
  "org": "myorganization",
  "templateRepo": "myorganization/template",
  "defaultPrivate": true,
  "lastCheck": "2026-01-27T12:00:00Z"
}
```

**Response 200 OK (disabled)**:
```json
{
  "status": "UNKNOWN",
  "enabled": false
}
```

**Response 503 Service Unavailable (connection failed)**:
```json
{
  "status": "DOWN",
  "enabled": true,
  "error": "Failed to connect to GitHub API: 401 Unauthorized"
}
```

---

### Test Connection

```
POST /api/v1/integrations/github/test
```

**Response 200 OK**:
```json
{
  "success": true,
  "message": "Successfully connected to GitHub API",
  "user": "myorganization",
  "scopes": ["repo", "admin:org"]
}
```

---

### Manual Provisioning

```
POST /api/v1/projects/{projectId}/github/provision
```

**Use Case**: Proyecto creado sin GitHub, admin decide agregarlo después.

**Response 201 Created**:
```json
{
  "id": 789,
  "repoFullName": "myorg/acme-corp-project",
  "repoUrl": "https://github.com/myorg/acme-corp-project",
  "status": "ACTIVE",
  "provisionedAt": "2026-01-27T12:30:00Z"
}
```

---

### Retry Provisioning

```
POST /api/v1/projects/{projectId}/github/retry
```

**Response 200 OK**:
```json
{
  "id": 789,
  "status": "ACTIVE",
  "provisioningError": null,
  "retriedAt": "2026-01-27T12:35:00Z"
}
```

---

### Sync Metadata

```
POST /api/v1/projects/{projectId}/github/sync
```

**Response 200 OK**:
```json
{
  "id": 789,
  "metadata": {
    "stars": 42,
    "forks": 5,
    "openIssues": 3,
    "language": "Java",
    "topics": ["spring-boot", "crm"]
  },
  "lastSyncAt": "2026-01-27T12:40:00Z"
}
```

---

## Seguridad

### Principios

1. **Nunca almacenar tokens en DB**: Solo en env vars
2. **Nunca logear tokens**: Sanitizar logs
3. **Rate limiting**: Respetar límites de GitHub API
4. **Least privilege**: Scopes mínimos necesarios
5. **Secrets management**: Usar soluciones enterprise en prod (AWS Secrets Manager, Vault)

### Validaciones

**En startup**:
```java
@PostConstruct
public void validateConfig() {
    if (configProperties.getEnabled() && 
        StringUtils.isBlank(configProperties.getToken())) {
        throw new IllegalStateException(
            "GITHUB_TOKEN is required when GITHUB_ENABLED=true"
        );
    }
}
```

**En logs**:
```java
public void logGitHubCall(String endpoint) {
    // ❌ MAL: log.info("Calling {} with token {}", endpoint, token);
    // ✅ BIEN:
    log.info("Calling GitHub API endpoint: {}", endpoint);
}
```

### Rate Limiting

GitHub API limits:
- **Authenticated (PAT)**: 5,000 requests/hour
- **Unauthenticated**: 60 requests/hour

**Manejo**:
- Cachear respuestas cuando sea posible
- Monitorear header `X-RateLimit-Remaining`
- Si `429 Too Many Requests`, esperar hasta `X-RateLimit-Reset`

---

## Manejo de Errores

### Exceptions Personalizadas

```java
public class GitHubApiException extends RuntimeException {
    private final int statusCode;
    private final String githubMessage;
    
    // Constructor, getters...
}

public class GitHubRateLimitException extends GitHubApiException {}
public class GitHubAuthException extends GitHubApiException {}
public class GitHubNotFoundException extends GitHubApiException {}
```

### Global Exception Handler

```java
@ExceptionHandler(GitHubApiException.class)
public ResponseEntity<ProblemDetail> handleGitHubApiException(
    GitHubApiException ex
) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_GATEWAY,
        "GitHub API error: " + ex.getMessage()
    );
    problem.setProperty("githubStatusCode", ex.getStatusCode());
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problem);
}
```

### Escenarios de Error

| Código | Escenario | Acción |
|--------|-----------|--------|
| 401 | Token inválido/expirado | Log error, marcar FAILED, alertar admin |
| 403 | Permisos insuficientes | Log error, marcar FAILED con mensaje específico |
| 404 | Template no existe | Log error, crear sin template |
| 422 | Nombre duplicado | Verificar idempotencia, registrar existente |
| 429 | Rate limit | Retry con backoff exponencial |
| 5xx | GitHub down | Log error, marcar FAILED, permitir retry |

---

## Testing

### Unit Tests

**GitHubClient** (mock HTTP responses):
```java
@Test
void createRepository_success() {
    // Mock RestTemplate
    when(restTemplate.exchange(...))
        .thenReturn(ResponseEntity.ok(mockRepoResponse));
    
    GitHubRepository repo = gitHubClient.createRepository(request);
    
    assertThat(repo.getFullName()).isEqualTo("myorg/test-repo");
}

@Test
void createRepository_rateLimitExceeded_retriesWithBackoff() {
    // Mock 429 response
    when(restTemplate.exchange(...))
        .thenThrow(new GitHubRateLimitException())
        .thenReturn(ResponseEntity.ok(mockRepoResponse));
    
    GitHubRepository repo = gitHubClient.createRepository(request);
    
    verify(restTemplate, times(2)).exchange(...);
}
```

**GitHubProvisioningService** (mock GitHubClient):
```java
@Test
void provisionRepository_clientProject_createsWithCorrectName() {
    Account account = createAccount("ACME Corp", "acme-corp");
    Project project = createProject("Website", "website", "CLIENT");
    
    when(gitHubClient.createRepository(any()))
        .thenReturn(mockGitHubRepo("myorg/acme-corp-website"));
    
    GitHubRepo result = service.provisionRepository(project, account);
    
    assertThat(result.getRepoFullName()).isEqualTo("myorg/acme-corp-website");
    assertThat(result.getStatus()).isEqualTo("ACTIVE");
}

@Test
void provisionRepository_apiFailure_marksAsFailed() {
    when(gitHubClient.createRepository(any()))
        .thenThrow(new GitHubApiException(503, "Service Unavailable"));
    
    GitHubRepo result = service.provisionRepository(project, account);
    
    assertThat(result.getStatus()).isEqualTo("FAILED");
    assertThat(result.getProvisioningError()).contains("Service Unavailable");
}
```

### Integration Tests

**Con Mock Server** (WireMock):
```java
@SpringBootTest
@AutoConfigureWireMock
class GitHubIntegrationTest {
    
    @Test
    void endToEnd_createProjectWithGitHub_success() {
        // Setup mock GitHub API
        stubFor(post("/orgs/myorg/repos")
            .willReturn(aResponse()
                .withStatus(201)
                .withBody(mockRepoJson)));
        
        // Call API
        ProjectRequest request = new ProjectRequest();
        request.setEnableGithub(true);
        // ...
        
        ResponseEntity<ProjectResponse> response = 
            restTemplate.postForEntity("/api/v1/projects", request, ...);
        
        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getGithubRepo().getStatus())
            .isEqualTo("ACTIVE");
        
        // Verify GitHub API was called
        verify(postRequestedFor(urlEqualTo("/orgs/myorg/repos")));
    }
}
```

---

## Deployment

### Docker Compose

```yaml
services:
  backend:
    environment:
      - GITHUB_ENABLED=true
      - GITHUB_TOKEN=${GITHUB_TOKEN}
      - GITHUB_ORG=myorganization
      - GITHUB_TEMPLATE_REPO=myorganization/template
```

### Kubernetes Secrets

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: github-integration
type: Opaque
stringData:
  token: ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
---
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
      - name: backend
        env:
        - name: GITHUB_TOKEN
          valueFrom:
            secretKeyRef:
              name: github-integration
              key: token
```

### Health Checks

```yaml
# docker-compose.yml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health/github"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 40s
```

---

## Futuras Mejoras

1. **GitHub App**: Migrar de PAT a GitHub App para mejor seguridad
2. **Webhooks**: Recibir eventos (push, PR, issues) y sincronizar
3. **CI/CD Templates**: Generar workflows de GitHub Actions
4. **Branch Sync**: Sincronizar branches y PRs en DB
5. **Issue Tracking**: Vincular GitHub Issues con Tasks en CRM
6. **Deployment Status**: Tracking de deployments desde GitHub
7. **Code Metrics**: Integración con GitHub Insights (commits, contributors)
8. **Automated PR Review**: Bot de review automático con reglas configurables

---

**Autor**: Staff Engineer / Tech Lead  
**Última actualización**: 2026-01-27
