import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { ThemeProvider } from './contexts/ThemeContext'
import { AuthProvider } from './admin/contexts/AuthContext'
import App from './App.jsx'
import './index.css'
import './i18n/i18n.js'

// Admin imports
import ProtectedRoute from './admin/components/ProtectedRoute'
import AdminLayout from './admin/layout/AdminLayout'
import Login from './admin/pages/Login'
import Dashboard from './admin/pages/Dashboard'
import Leads from './admin/pages/Leads'
import LeadDetail from './admin/pages/LeadDetail'
import Clients from './admin/pages/Clients'
import Projects from './admin/pages/Projects'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <ThemeProvider>
        <AuthProvider>
          <Routes>
            {/* Public portfolio site */}
            <Route path="/" element={<App />} />
            
            {/* Admin login */}
            <Route path="/admin/login" element={<Login />} />
            
            {/* Protected admin routes */}
            <Route
              path="/admin"
              element={
                <ProtectedRoute>
                  <AdminLayout />
                </ProtectedRoute>
              }
            >
              <Route path="dashboard" element={<Dashboard />} />
              <Route path="leads" element={<Leads />} />
              <Route path="leads/:id" element={<LeadDetail />} />
              <Route path="clients" element={<Clients />} />
              <Route path="projects" element={<Projects />} />
            </Route>
          </Routes>
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  </React.StrictMode>,
)
