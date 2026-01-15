# Admin CRM Frontend

This is the admin CRM frontend for the portfolio application. It provides a complete dashboard for managing leads, clients, and projects.

## Features

- **Authentication**: JWT-based login system
- **Dashboard**: KPI cards and analytics
- **Leads Management**: View, edit, and track leads through pipeline stages
- **Clients Management**: View and manage active clients
- **Projects Management**: Track project status and progress
- **Dark Mode**: Full dark mode support
- **Responsive**: Mobile-friendly design

## Routes

- `/` - Public portfolio site
- `/admin/login` - Admin login page
- `/admin/dashboard` - Dashboard with KPIs
- `/admin/leads` - Leads list
- `/admin/leads/:id` - Lead detail page
- `/admin/clients` - Clients list
- `/admin/projects` - Projects list

## Setup

1. Make sure the backend API is running
2. Configure the API URL in `.env`:
   ```
   VITE_API_URL=http://localhost:8080
   ```
3. Run the development server:
   ```
   npm run dev
   ```

## Authentication

The admin system uses JWT tokens stored in localStorage:
- `auth_token` - JWT authentication token
- `auth_user` - User information

When a 401 response is received, the user is automatically redirected to the login page.

## API Endpoints Used

- `POST /api/v1/auth/login` - Login
- `GET /api/v1/dashboard/kpis` - Get dashboard KPIs
- `GET /api/v1/dashboard/leads-by-stage` - Get leads by stage
- `GET /api/v1/leads` - Get leads list
- `GET /api/v1/leads/:id` - Get lead details
- `PUT /api/v1/leads/:id` - Update lead
- `PATCH /api/v1/leads/:id/stage` - Update lead stage
- `GET /api/v1/clients` - Get clients list
- `GET /api/v1/projects` - Get projects list

## Components Structure

```
src/admin/
├── components/
│   └── ProtectedRoute.jsx      # Route protection wrapper
├── contexts/
│   └── AuthContext.jsx          # Authentication state management
├── layout/
│   └── AdminLayout.jsx          # Main layout with sidebar
├── pages/
│   ├── Login.jsx                # Login page
│   ├── Dashboard.jsx            # Dashboard with KPIs
│   ├── Leads.jsx                # Leads list
│   ├── LeadDetail.jsx           # Lead detail/edit
│   ├── Clients.jsx              # Clients list
│   └── Projects.jsx             # Projects list
└── services/
    ├── api.service.js           # Axios instance with interceptors
    ├── auth.service.js          # Authentication service
    ├── leads.service.js         # Leads API calls
    └── dashboard.service.js     # Dashboard API calls
```

## Styling

All components use Tailwind CSS with dark mode support. The admin section integrates with the existing ThemeContext for consistent dark mode behavior across the app.

## Icons

React Icons (react-icons/fi) are used throughout for a consistent icon system.
