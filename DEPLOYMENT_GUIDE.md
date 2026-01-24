# 🚀 GUÍA DE DEPLOYMENT EN LA NUBE

**Sistema:** Portfolio CRM Full-Stack  
**Fecha:** Enero 2026  
**Plataformas Soportadas:** Railway, Render, Fly.io, AWS, GCP, Azure

---

## 📋 TABLA DE CONTENIDOS

1. [Pre-requisitos](#pre-requisitos)
2. [Variables de Entorno](#variables-de-entorno)
3. [Deployment en Railway (Recomendado)](#deployment-railway)
4. [Deployment en Render](#deployment-render)
5. [Deployment en AWS/ECS](#deployment-aws)
6. [Deployment en Kubernetes](#deployment-kubernetes)
7. [Base de Datos y Migraciones](#base-de-datos)
8. [SSL/HTTPS](#ssl-https)
9. [Monitoring y Logs](#monitoring)
10. [Troubleshooting](#troubleshooting)

---

## 🔧 PRE-REQUISITOS

### Antes de Deployar

- [ ] Todas las correcciones de seguridad implementadas (ver ANALYSIS.md)
- [ ] Tests pasando
- [ ] Variables de entorno documentadas
- [ ] Base de datos PostgreSQL disponible
- [ ] Dominio (opcional pero recomendado)
- [ ] SSL certificate (o usar Let's Encrypt)

### Herramientas Necesarias

```bash
# Instalar CLI tools
npm install -g vercel    # Para frontend
brew install railway     # Para backend (Mac)
# o
curl -fsSL https://railway.app/install.sh | sh

# Docker (para testing local)
docker --version
docker-compose --version

# Git
git --version
```

---

## 🔐 VARIABLES DE ENTORNO

### Backend (.env)

```bash
# ==========================================
# CRÍTICO: CAMBIAR EN PRODUCCIÓN
# ==========================================

# Database (provisto por Railway/Render)
DATABASE_URL=jdbc:postgresql://production-host:5432/portfolio_crm
DATABASE_USER=production_user
DATABASE_PASSWORD=STRONG_PASSWORD_HERE

# JWT Configuration
# GENERAR CON: openssl rand -base64 64
JWT_SECRET=TU_SECRET_GENERADO_AQUI_MIN_64_CARACTERES_BASE64
JWT_EXPIRATION=86400000           # 24 horas
JWT_REFRESH_EXPIRATION=604800000  # 7 días

# Admin User (solo para inicialización)
ADMIN_EMAIL=admin@tudominio.com
ADMIN_PASSWORD=PASSWORD_SEGURO_AQUI_NO_admin123

# Email Configuration
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=TU_SENDGRID_API_KEY
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
CONTACT_RECIPIENT=contacto@tudominio.com

# CORS (separar con comas)
CORS_ORIGINS=https://tudominio.com,https://www.tudominio.com

# Application
SPRING_PROFILES_ACTIVE=prod
JPA_SHOW_SQL=false
JPA_HIBERNATE_DDL_AUTO=validate  # NUNCA usar 'create' en producción

# Flyway
FLYWAY_ENABLED=true
FLYWAY_BASELINE_ON_MIGRATE=false  # Cambiar a false en producción
```

### Frontend (.env.production)

```bash
# API Backend
VITE_API_URL=https://api.tudominio.com

# Analytics
VITE_GA_TRACKING_ID=G-XXXXXXXXXX

# WhatsApp
VITE_WHATSAPP_NUMBER=+54911XXXXXXXX

# Environment
VITE_ENV=production
```

---

## 🚂 DEPLOYMENT EN RAILWAY (Recomendado)

Railway ofrece deploy gratuito con PostgreSQL incluido.

### Paso 1: Crear Proyecto

```bash
# Instalar Railway CLI
railway login

# Crear nuevo proyecto
railway init
railway link
```

### Paso 2: Añadir PostgreSQL

```bash
# En Railway Dashboard
1. Click "New" -> "Database" -> "PostgreSQL"
2. Railway automáticamente crea DATABASE_URL
```

### Paso 3: Configurar Backend

```bash
# Crear railway.json en /backend
cat > backend/railway.json << 'JSON'
{
  "build": {
    "builder": "NIXPACKS",
    "buildCommand": "mvn clean package -DskipTests"
  },
  "deploy": {
    "startCommand": "java -jar target/contact-backend-0.0.1-SNAPSHOT.jar",
    "healthcheckPath": "/actuator/health",
    "healthcheckTimeout": 300
  }
}
JSON
```

### Paso 4: Configurar Variables

```bash
railway variables set JWT_SECRET=$(openssl rand -base64 64)
railway variables set ADMIN_PASSWORD="TuPasswordSeguro123!"
railway variables set MAIL_HOST="smtp.sendgrid.net"
# ... resto de variables
```

### Paso 5: Deploy Backend

```bash
cd backend
railway up
```

### Paso 6: Deploy Frontend (Vercel)

```bash
cd frontend
vercel

# Configurar variables en Vercel Dashboard
# VITE_API_URL=https://tu-backend.railway.app
```

### Paso 7: Configurar Dominio

```bash
# En Railway Dashboard
Settings -> Domains -> Generate Domain
# O añadir custom domain
```

---

## 🎨 DEPLOYMENT EN RENDER

### Backend

1. **Crear Web Service:**
   - Conectar repositorio GitHub
   - Build Command: `cd backend && mvn clean package -DskipTests`
   - Start Command: `java -jar backend/target/contact-backend-0.0.1-SNAPSHOT.jar`
   - Environment: `Docker` o `Java 21`

2. **Crear PostgreSQL Database:**
   - Dashboard -> New -> PostgreSQL
   - Copiar Internal Database URL

3. **Configurar Variables:**
   ```
   DATABASE_URL = [Internal Database URL]
   JWT_SECRET = [generado con openssl]
   ADMIN_PASSWORD = [password seguro]
   CORS_ORIGINS = https://tu-frontend.onrender.com
   ```

4. **Health Check:**
   - Path: `/actuator/health`
   - Timeout: 300s

### Frontend

1. **Crear Static Site:**
   - Build Command: `cd frontend && npm install && npm run build`
   - Publish Directory: `frontend/dist`

2. **Environment Variables:**
   ```
   VITE_API_URL = https://tu-backend.onrender.com
   ```

---

## ☁️ DEPLOYMENT EN AWS

### Opción 1: Elastic Beanstalk (Más Simple)

```bash
# Instalar EB CLI
pip install awsebcli

# Inicializar
cd backend
eb init -p corretto-21 portfolio-crm
eb create portfolio-crm-prod

# Deploy
eb deploy
```

### Opción 2: ECS + Fargate (Más Escalable)

```yaml
# docker-compose.production.yml
version: '3.8'
services:
  backend:
    image: tu-registro.dkr.ecr.region.amazonaws.com/portfolio-backend:latest
    environment:
      - DATABASE_URL=${RDS_DATABASE_URL}
      - JWT_SECRET=${JWT_SECRET}
    ports:
      - "8080:8080"
```

**Task Definition:**
```json
{
  "family": "portfolio-backend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "containerDefinitions": [
    {
      "name": "backend",
      "image": "tu-imagen:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [],
      "secrets": [
        {
          "name": "JWT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:..."
        }
      ]
    }
  ]
}
```

---

## ⚓ DEPLOYMENT EN KUBERNETES

### Secrets

```yaml
# secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: portfolio-secrets
type: Opaque
stringData:
  jwt-secret: "TU_JWT_SECRET_AQUI"
  admin-password: "TU_ADMIN_PASSWORD"
  database-url: "jdbc:postgresql://postgres:5432/portfolio_crm"
  mail-password: "TU_MAIL_PASSWORD"
```

### Deployment - Backend

```yaml
# backend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: portfolio-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: portfolio-backend
  template:
    metadata:
      labels:
        app: portfolio-backend
    spec:
      containers:
      - name: backend
        image: tu-registry/portfolio-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: portfolio-secrets
              key: jwt-secret
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: portfolio-secrets
              key: database-url
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
---
apiVersion: v1
kind: Service
metadata:
  name: portfolio-backend-service
spec:
  selector:
    app: portfolio-backend
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

### Deployment - Frontend

```yaml
# frontend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: portfolio-frontend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: portfolio-frontend
  template:
    metadata:
      labels:
        app: portfolio-frontend
    spec:
      containers:
      - name: frontend
        image: tu-registry/portfolio-frontend:latest
        ports:
        - containerPort: 80
        env:
        - name: VITE_API_URL
          value: "https://api.tudominio.com"
---
apiVersion: v1
kind: Service
metadata:
  name: portfolio-frontend-service
spec:
  selector:
    app: portfolio-frontend
  ports:
  - port: 80
    targetPort: 80
  type: LoadBalancer
```

### Ingress (Nginx)

```yaml
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: portfolio-ingress
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - tudominio.com
    - api.tudominio.com
    secretName: portfolio-tls
  rules:
  - host: tudominio.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: portfolio-frontend-service
            port:
              number: 80
  - host: api.tudominio.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: portfolio-backend-service
            port:
              number: 80
```

---

## 🗄️ BASE DE DATOS Y MIGRACIONES

### Estrategia de Migraciones en Producción

1. **NUNCA usar `hibernate.ddl-auto=create`** en producción
2. **Usar Flyway** para migraciones controladas
3. **Backup antes de migrar**

### Procedimiento de Migración

```bash
# 1. Backup de DB
pg_dump -h production-host -U user portfolio_crm > backup_$(date +%Y%m%d).sql

# 2. Revisar migraciones pendientes
flyway info -url=jdbc:postgresql://host/db -user=user -password=pass

# 3. Aplicar migraciones
flyway migrate -url=jdbc:postgresql://host/db -user=user -password=pass

# 4. Verificar
flyway validate
```

### Migraciones Zero-Downtime

```sql
-- V9__add_column_with_default.sql
-- BIEN: Añadir columna con default (no bloquea)
ALTER TABLE leads ADD COLUMN priority VARCHAR(20) DEFAULT 'MEDIUM';

-- MAL: Añadir columna NOT NULL sin default (bloquea tabla)
-- ALTER TABLE leads ADD COLUMN priority VARCHAR(20) NOT NULL;
```

---

## 🔒 SSL/HTTPS

### Let's Encrypt (Gratuito)

```bash
# Con Certbot
sudo certbot --nginx -d tudominio.com -d www.tudominio.com

# O en K8s con cert-manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml
```

### Configuración Nginx

```nginx
server {
    listen 443 ssl http2;
    server_name tudominio.com;

    ssl_certificate /etc/letsencrypt/live/tudominio.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/tudominio.com/privkey.pem;
    
    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;

    location / {
        proxy_pass http://frontend:5173;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 📊 MONITORING Y LOGS

### Application Performance Monitoring

```yaml
# Spring Boot Actuator endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Logging Centralizado

**Opción 1: Papertrail / Logtail**
```yaml
# logback-spring.xml
<appender name="PAPERTRAIL" class="ch.qos.logback.classic.net.SyslogAppender">
    <syslogHost>logs.papertrailapp.com</syslogHost>
    <port>12345</port>
</appender>
```

**Opción 2: ELK Stack**
```bash
# Filebeat config
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /var/log/portfolio/*.log
output.elasticsearch:
  hosts: ["elasticsearch:9200"]
```

### Alerts

```yaml
# Prometheus Alert Rules
groups:
- name: portfolio_alerts
  rules:
  - alert: HighErrorRate
    expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
    annotations:
      summary: "High 5xx error rate"
  
  - alert: DatabaseDown
    expr: up{job="postgres"} == 0
    annotations:
      summary: "PostgreSQL is down"
```

---

## 🔍 TROUBLESHOOTING

### Backend no Arranca

```bash
# 1. Verificar logs
railway logs
# o
kubectl logs -f deployment/portfolio-backend

# 2. Verificar variables
railway variables
# o
kubectl get secret portfolio-secrets -o yaml

# 3. Verificar conectividad DB
kubectl run -it --rm debug --image=postgres:16 --restart=Never --   psql postgresql://user:pass@host:5432/portfolio_crm
```

### Migraciones Fallan

```bash
# 1. Ver estado de migraciones
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

# 2. Reparar (si es necesario)
flyway repair -url=... -user=... -password=...

# 3. Re-intentar
flyway migrate
```

### Health Check Falla

```bash
# Verificar endpoint
curl https://api.tudominio.com/actuator/health

# Ver detalles
curl https://api.tudominio.com/actuator/health | jq .

# Si DB está DOWN:
# - Verificar DATABASE_URL
# - Verificar firewall rules
# - Verificar que DB está UP
```

---

## ✅ CHECKLIST PRE-GO-LIVE

### Seguridad
- [ ] JWT_SECRET generado con openssl (min 64 chars)
- [ ] ADMIN_PASSWORD cambiado (no "admin123")
- [ ] CORS configurado solo para dominios producción
- [ ] HTTPS habilitado
- [ ] Rate limiting activo
- [ ] Logs no exponen passwords

### Base de Datos
- [ ] PostgreSQL 16 en producción
- [ ] Backup automático configurado
- [ ] Índices creados (ver ANALYSIS.md #17)
- [ ] Foreign key constraints añadidos
- [ ] Migraciones Flyway ejecutadas

### Application
- [ ] SPRING_PROFILES_ACTIVE=prod
- [ ] JPA_SHOW_SQL=false
- [ ] Health checks respondiendo
- [ ] Actuator endpoints protegidos
- [ ] Email notifications funcionando

### Monitoring
- [ ] Logs centralizados
- [ ] Métricas exportadas (Prometheus)
- [ ] Alerts configurados
- [ ] Health dashboards creados

### Performance
- [ ] Frontend minificado y comprimido
- [ ] Assets en CDN (opcional)
- [ ] Database connection pool configurado
- [ ] Cache habilitado (Redis opcional)

---

## 🎯 DEMO CON CLIENTE

### Preparación

1. **Ambiente de Staging:**
   ```bash
   # Crear ambiente idéntico a producción
   railway environment create staging
   railway environment switch staging
   railway up
   ```

2. **Data de Demo:**
   ```sql
   -- Insertar leads de ejemplo
   INSERT INTO leads (name, email, company, stage) VALUES
   ('Juan Pérez', 'juan@example.com', 'Acme Corp', 'QUALIFIED'),
   ('María García', 'maria@example.com', 'Tech SA', 'PROPOSAL');
   ```

3. **Checklist Pre-Demo:**
   - [ ] Staging funcionando 100%
   - [ ] Data de demo cargada
   - [ ] Formulario de contacto testeado
   - [ ] Login admin funciona
   - [ ] Dashboard con datos
   - [ ] CRUD de leads/clients/projects funciona
   - [ ] Emails de notificación llegan
   - [ ] Responsive en mobile

---

## 📞 SOPORTE

Para problemas durante deployment:
- Railway: https://help.railway.app
- Render: https://render.com/docs
- Esta guía: Ver ANALYSIS.md para problemas conocidos

---

**Última actualización:** Enero 2026
