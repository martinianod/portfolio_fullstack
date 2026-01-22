# Login Diagnostic Guide

This guide helps diagnose login issues in the Portfolio CRM application.

## Quick Test Commands

### 1. Check Backend Health
```bash
curl http://localhost:8080/actuator/health
```
Expected: `{"status":"UP",...}`

### 2. Test Login Endpoint
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@martiniano.dev","password":"admin123"}' \
  -v
```

Expected success (200):
```json
{
  "token": "eyJ...",
  "username": "admin",
  "email": "admin@martiniano.dev",
  "role": "ADMIN"
}
```

Expected validation error (400):
```json
{
  "error": "Validation failed",
  "fields": {
    "email": "Email is required"
  }
}
```

Expected authentication error (401):
```json
{
  "error": "Authentication failed",
  "message": "Invalid email or password"
}
```

## Backend Logs Analysis

### Check Backend Logs
```bash
docker-compose logs backend | tail -100
```

or for real-time:
```bash
docker-compose logs -f backend
```

### What to Look For

**1. Successful Login Flow:**
```
Login attempt - Path: /api/v1/auth/login, Method: POST, Content-Type: application/json, Email: admin@martiniano.dev
Attempting authentication for email: admin@martiniano.dev
User found in database for email: admin@martiniano.dev
Password verified for email: admin@martiniano.dev
User entity retrieved for email: admin@martiniano.dev
JWT token generated for user: admin
Login successful for email: admin@martiniano.dev
```

**2. Validation Error (400):**
```
Validation error - Field: email, Message: Email is required
Returning 400 - Validation failed: {email=Email is required}
```
**Cause:** Request body is missing the email field or it's empty
**Fix:** Ensure frontend sends `{"email":"...","password":"..."}`

**3. User Not Found:**
```
Login attempt - Path: /api/v1/auth/login, Method: POST, Content-Type: application/json, Email: test@example.com
Attempting authentication for email: test@example.com
User not found for email: test@example.com
Login failed for email: test@example.com - Reason: Invalid credentials
Returning 401 - Authentication failed
```
**Cause:** User doesn't exist in database
**Fix:** Check if admin user was created. Run:
```bash
docker-compose logs backend | grep -i "admin user"
```
Should see: "Default admin user created successfully" or "Admin user already exists"

**4. Password Mismatch:**
```
Login attempt - Path: /api/v1/auth/login, Method: POST, Content-Type: application/json, Email: admin@martiniano.dev
Attempting authentication for email: admin@martiniano.dev
User found in database for email: admin@martiniano.dev
Password mismatch for email: admin@martiniano.dev
Login failed for email: admin@martiniano.dev - Reason: Invalid credentials
Returning 401 - Authentication failed
```
**Cause:** Password is incorrect or BCrypt hash doesn't match
**Fix:** Verify password is exactly `admin123` (case-sensitive)

**5. Database Connection Issue:**
```
Error creating bean... DataSource...
Could not connect to database
```
**Cause:** Backend can't connect to PostgreSQL
**Fix:** 
```bash
docker-compose ps postgres  # Should show "healthy"
docker-compose logs postgres
```

## Frontend Logs Analysis (Browser Console)

Open browser DevTools Console (F12) and look for:

**1. Configuration:**
```javascript
[API Client] Configuration { 
  baseURL: 'http://localhost:8080',
  env: { VITE_API_URL: 'http://localhost:8080', mode: 'development' }
}
```
**Check:** baseURL should be `http://localhost:8080`

**2. Login Request:**
```javascript
[API Client] Request { 
  method: 'POST', 
  url: '/api/v1/auth/login',
  fullURL: 'http://localhost:8080/api/v1/auth/login',
  headers: { 'Content-Type': 'application/json' },
  hasData: true 
}
[AuthService] Login attempt { 
  email: 'admin@martiniano.dev',
  endpoint: '/api/v1/auth/login',
  baseURL: 'http://localhost:8080'
}
```
**Check:** URL should be `http://localhost:8080/api/v1/auth/login`

**3. Success Response:**
```javascript
[API Client] Response { status: 200, url: '/api/v1/auth/login', hasData: true }
[AuthService] Login successful { email: 'admin@martiniano.dev', status: 200, hasToken: true }
```

**4. Error Response (400 Validation):**
```javascript
[API Client] Response error { 
  status: 400,
  url: '/api/v1/auth/login',
  data: { error: 'Validation failed', fields: { email: 'Email is required' } }
}
[AuthService] Login failed { 
  email: 'admin@martiniano.dev',
  status: 400,
  errorData: { error: 'Validation failed', fields: {...} }
}
```
**Cause:** Frontend not sending correct payload

**5. Error Response (401 Authentication):**
```javascript
[API Client] Response error { 
  status: 401,
  url: '/api/v1/auth/login',
  data: { error: 'Authentication failed', message: 'Invalid email or password' }
}
[AuthService] Login failed { 
  email: 'admin@martiniano.dev',
  status: 401,
  errorData: { error: 'Authentication failed', ... }
}
```
**Cause:** Wrong credentials or user doesn't exist

**6. Network Error:**
```javascript
[API Client] Response error { 
  status: undefined,
  message: 'Network Error'
}
```
**Cause:** Backend not accessible
**Fix:** Check if backend is running and accessible at http://localhost:8080

## Common Issues and Solutions

### Issue: 400 Bad Request - "Email is required"

**Diagnosis:**
- Check browser console: Is frontend sending `email` field?
- Check Network tab: Request payload should be `{"email":"...","password":"..."}`

**Solution:**
- Frontend should use `email` field, NOT `username`
- Correct: `{"email":"admin@martiniano.dev","password":"admin123"}`
- Wrong: `{"username":"admin","password":"admin123"}`

### Issue: 401 Unauthorized - User doesn't exist

**Diagnosis:**
```bash
docker-compose logs backend | grep -i "admin user"
```

**Solution if admin not created:**
```bash
# Restart backend to trigger DataInitializer
docker-compose restart backend
docker-compose logs backend | grep -i "admin user"
```

**Solution if admin exists but login fails:**
```bash
# Verify admin user in database
docker-compose exec postgres psql -U portfolio -d portfolio_crm \
  -c "SELECT username, email, role, enabled FROM users WHERE email='admin@martiniano.dev';"
```

Should show:
```
 username |          email          | role  | enabled 
----------+-------------------------+-------+---------
 admin    | admin@martiniano.dev    | ADMIN | t
```

### Issue: CORS Error

**Diagnosis:**
Browser console shows: `Access to XMLHttpRequest at 'http://localhost:8080/api/v1/auth/login' from origin 'http://localhost:5173' has been blocked by CORS policy`

**Solution:**
Check backend CORS configuration allows http://localhost:5173

### Issue: Wrong Backend URL

**Diagnosis:**
Frontend logs show: `baseURL: 'http://localhost:3000'` (wrong port)

**Solution:**
Create/update `frontend/.env`:
```
VITE_API_URL=http://localhost:8080
```

## Step-by-Step Diagnostic Process

1. **Check Backend is Running:**
   ```bash
   docker-compose ps backend
   curl http://localhost:8080/actuator/health
   ```

2. **Check Admin User Exists:**
   ```bash
   docker-compose logs backend | grep -i "admin"
   ```

3. **Test Login with curl:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@martiniano.dev","password":"admin123"}' \
     -v
   ```

4. **Check Backend Logs During curl Test:**
   ```bash
   docker-compose logs backend | tail -50
   ```

5. **Test from Browser:**
   - Open http://localhost:5173/admin/login
   - Open browser DevTools (F12)
   - Try to login with admin@martiniano.dev / admin123
   - Check Console tab for frontend logs
   - Check Network tab for request/response details

6. **Compare Logs:**
   - What does backend log show?
   - What does frontend console show?
   - What does Network tab show for the request?

## Contact Support

If the issue persists after following this guide, provide:
1. Backend logs during login attempt
2. Frontend console logs during login attempt
3. Network tab screenshot showing the failed request
4. curl command output
