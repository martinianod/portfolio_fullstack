# ✅ CHECKLIST DE PRODUCCIÓN - PORTFOLIO CRM

**Sistema:** Portfolio CRM Full-Stack  
**Fecha:** Enero 2026  
**Versión:** 0.0.1-SNAPSHOT

---

## 🔴 CRÍTICO - Obligatorio Antes de Deployment

### Seguridad

- [ ] **JWT Secret generado y configurado**
  ```bash
  # Generar secret de 256 bits
  JWT_SECRET=$(openssl rand -base64 64)
  echo "JWT_SECRET=$JWT_SECRET" >> .env
  ```
  - ✓ Secret tiene mínimo 64 caracteres
  - ✓ Secret no contiene "changeme"
  - ✓ Secret es único para este ambiente

- [ ] **Contraseña de Admin cambiada**
  ```bash
  # Generar password seguro
  ADMIN_PASSWORD=$(openssl rand -base64 32)
  echo "ADMIN_PASSWORD=$ADMIN_PASSWORD" >> .env
  ```
  - ✓ Password diferente a "admin123"
  - ✓ Password tiene min 16 caracteres
  - ✓ Password incluye mayúsculas, minúsculas, números, símbolos

- [ ] **CORS configurado para dominio de producción**
  ```bash
  CORS_ORIGINS=https://tudominio.com,https://www.tudominio.com
  ```
  - ✓ Solo dominios de producción listados
  - ✓ Sin `localhost` en producción
  - ✓ HTTPS habilitado

- [ ] **Variables de entorno validadas**
  - ✓ DATABASE_URL apunta a DB de producción
  - ✓ MAIL_HOST configurado correctamente
  - ✓ CONTACT_RECIPIENT es email válido
  - ✓ Sin valores por defecto (changeme, admin123, etc.)

---

### Base de Datos

- [ ] **PostgreSQL 16 provisionado**
  - ✓ Database `portfolio_crm` creada
  - ✓ Usuario con permisos correctos
  - ✓ Backup automático configurado
  - ✓ Retention policy definido (min 7 días)

- [ ] **Migraciones Flyway ejecutadas**
  ```bash
  # Verificar migraciones
  SELECT version, description, installed_on, success 
  FROM flyway_schema_history 
  ORDER BY installed_rank;
  ```
  - ✓ V1__initial_schema.sql aplicada
  - ✓ V2__seed_data.sql aplicada
  - ✓ Sin errores en migraciones
  - ✓ Admin user creado

- [ ] **Índices de performance creados**
  ```sql
  -- Verificar índices
  SELECT indexname, tablename 
  FROM pg_indexes 
  WHERE schemaname = 'public';
  ```
  - ✓ idx_users_email
  - ✓ idx_leads_name
  - ✓ idx_leads_stage
  - ✓ idx_leads_created_at

---

### Configuración de Aplicación

- [ ] **Spring Profile correcto**
  ```bash
  SPRING_PROFILES_ACTIVE=prod
  ```
  - ✓ Profile es "prod", no "dev"
  - ✓ JPA_SHOW_SQL=false
  - ✓ hibernate.ddl-auto=validate

- [ ] **Actuator endpoints protegidos**
  ```yaml
  management:
    endpoints:
      web:
        exposure:
          include: health,info
  ```
  - ✓ Solo health e info expuestos
  - ✓ Metrics no públicos
  - ✓ Health details no expuestos públicamente

---

## 🟠 ALTA PRIORIDAD - Recomendado Antes de Go-Live

### Performance

- [ ] **Connection pool configurado**
  ```yaml
  spring:
    datasource:
      hikari:
        maximum-pool-size: 10
        minimum-idle: 2
  ```

- [ ] **Timeout configurations**
  ```yaml
  server:
    tomcat:
      connection-timeout: 20000
      max-threads: 200
  ```

### Monitoring

- [ ] **Health checks funcionando**
  ```bash
  curl https://api.tudominio.com/actuator/health
  # Debe retornar: {"status":"UP"}
  ```

- [ ] **Logs centralizados**
  - ✓ Logs enviándose a servicio centralizado (Papertrail, CloudWatch, etc.)
  - ✓ Retention policy configurado
  - ✓ Log level apropiado (INFO en prod)

- [ ] **Alertas configuradas**
  - ✓ Health check DOWN
  - ✓ Error rate > 5%
  - ✓ Response time > 2s
  - ✓ Database connection pool exhausted

### SSL/HTTPS

- [ ] **Certificado SSL instalado**
  - ✓ Certificate válido (no expirado)
  - ✓ Chain completo
  - ✓ Sin mixed content warnings

- [ ] **HTTPS redirect configurado**
  ```nginx
  server {
      listen 80;
      return 301 https://$host$request_uri;
  }
  ```

- [ ] **Security headers**
  ```nginx
  add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
  add_header X-Frame-Options "SAMEORIGIN" always;
  add_header X-Content-Type-Options "nosniff" always;
  ```

---

## 🟡 MEDIA PRIORIDAD - Mejora Continua

### Rate Limiting

- [ ] **Rate limiting en login**
  - ✓ Max 5 intentos por minuto por IP
  - ✓ Bucket4j configurado

- [ ] **Rate limiting en API pública**
  - ✓ /api/v1/leads/public limitado
  - ✓ 10 requests por hora por IP

### Backup & Recovery

- [ ] **Estrategia de backup definida**
  - ✓ Backups diarios automáticos
  - ✓ Backup antes de cada deployment
  - ✓ Proceso de restore testeado

- [ ] **Disaster recovery plan**
  - ✓ RTO (Recovery Time Objective) definido
  - ✓ RPO (Recovery Point Objective) definido
  - ✓ Runbook de recovery documentado

### Optimización

- [ ] **Frontend optimizado**
  - ✓ Assets minificados
  - ✓ Gzip/Brotli habilitado
  - ✓ Cache headers configurados
  - ✓ CDN configurado (opcional)

- [ ] **Backend optimizado**
  - ✓ Queries N+1 eliminadas
  - ✓ Índices en columnas de búsqueda
  - ✓ Soft delete implementado
  - ✓ Pagination en todos los endpoints

---

## 🟢 BAJA PRIORIDAD - Nice to Have

### Features Adicionales

- [ ] Refresh token implementado
- [ ] Logout con token blacklist
- [ ] Error boundary en frontend
- [ ] Analytics configurado
- [ ] PropTypes en todos los componentes

### Testing

- [ ] Tests unitarios backend (cobertura > 70%)
- [ ] Tests e2e frontend
- [ ] Tests de integración API
- [ ] Load testing ejecutado

---

## 📋 CHECKLIST DE DEPLOYMENT

### Pre-Deployment (1 día antes)

- [ ] **Comunicación**
  - ✓ Cliente notificado de ventana de deployment
  - ✓ Equipo preparado
  - ✓ Runbook revisado

- [ ] **Preparación**
  - ✓ Backup completo de DB
  - ✓ Rollback plan documentado
  - ✓ Smoke tests preparados

### Durante Deployment

- [ ] **Backend**
  ```bash
  # 1. Verificar health antes
  curl https://api.tudominio.com/actuator/health
  
  # 2. Deploy nueva versión
  railway deploy
  
  # 3. Verificar health después
  curl https://api.tudominio.com/actuator/health
  
  # 4. Verificar logs
  railway logs | tail -100
  ```

- [ ] **Frontend**
  ```bash
  # 1. Build
  npm run build
  
  # 2. Deploy
  vercel deploy --prod
  
  # 3. Verificar sitio carga
  curl -I https://tudominio.com
  ```

### Post-Deployment (30 min después)

- [ ] **Smoke Tests**
  - ✓ Homepage carga
  - ✓ Login funciona
  - ✓ Dashboard carga con datos
  - ✓ CRUD de leads funciona
  - ✓ CRUD de clients funciona
  - ✓ CRUD de projects funciona
  - ✓ Formulario público funciona
  - ✓ Emails se envían

- [ ] **Monitoring**
  - ✓ Health status: UP
  - ✓ Error rate < 1%
  - ✓ Response time < 500ms
  - ✓ CPU usage < 70%
  - ✓ Memory usage < 80%

---

## 🚨 ROLLBACK PLAN

Si algo falla durante deployment:

### Backend Rollback
```bash
# Railway
railway rollback

# O re-deploy versión anterior
git checkout <previous-commit>
railway deploy
```

### Frontend Rollback
```bash
# Vercel
vercel rollback
```

### Database Rollback
```bash
# Restore backup
pg_restore -d portfolio_crm backup_YYYYMMDD.sql

# O rollback migración Flyway
flyway undo
```

---

## 📊 MÉTRICAS DE ÉXITO

### Inmediato (primeras 24h)

- [ ] Uptime > 99%
- [ ] Error rate < 1%
- [ ] P95 response time < 1s
- [ ] Cero incidentes críticos

### Primera semana

- [ ] Uptime > 99.5%
- [ ] Cero downtime no planificado
- [ ] Cliente satisfecho
- [ ] Todas las features funcionando

### Primer mes

- [ ] Performance estable
- [ ] Backups consistentes
- [ ] Monitoring efectivo
- [ ] Sin vulnerabilidades críticas

---

## 👥 CONTACTOS DE EMERGENCIA

### Equipo
- **Tech Lead:** [nombre] - [email] - [teléfono]
- **DevOps:** [nombre] - [email] - [teléfono]
- **Backend Dev:** [nombre] - [email] - [teléfono]
- **Frontend Dev:** [nombre] - [email] - [teléfono]

### Servicios
- **Railway Support:** https://help.railway.app
- **Vercel Support:** https://vercel.com/support
- **Database Provider:** [contact]

---

## 📖 REFERENCIAS

- **Análisis Completo:** ANALYSIS.md
- **Guía de Deployment:** DEPLOYMENT_GUIDE.md
- **Resumen de Mejoras:** IMPLEMENTATION_SUMMARY.md
- **README Principal:** README.md

---

**Última actualización:** Enero 2026  
**Versión Checklist:** 1.0
