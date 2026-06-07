import React from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import JobListPage from './pages/JobListPage';
import JobDetailPage from './pages/JobDetailPage';
import JobAddPage from './pages/JobAddPage';
import JobEditPage from './pages/JobEditPage';
import UserListPage from './pages/admin/UserListPage';
import UserCreatePage from './pages/admin/UserCreatePage';
import UserEditPage from './pages/admin/UserEditPage';
import AdminDashboard from './pages/admin/AdminDashboard';
import './index.css';

function AppContent() {
  const location = useLocation();
  const isAuthPage = location.pathname === '/login' || location.pathname === '/register' || location.pathname === '/forgot-password';

  return (
    <div className={isAuthPage ? "auth-layout-container" : "app-container"}>
      {!isAuthPage && <Sidebar />}
      <main className={isAuthPage ? "auth-content-container" : "content-container"}>
        <Routes>
          <Route path="/" element={<JobListPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/jobs/new" element={<JobAddPage />} />
          <Route path="/jobs/:id/edit" element={<JobEditPage />} />
          <Route path="/users-management" element={<UserListPage />} />
          <Route path="/users-management/add" element={<UserCreatePage />} />
          <Route path="/users-management/:id/edit" element={<UserEditPage />} />
          <Route path="/job-postings/:id" element={<JobDetailPage />} />
          <Route path="/admin" element={<AdminDashboard />} />
        </Routes>
      </main>
    </div>
  );
}

function App() {
  return (
    <Router>
      <AppContent />
    </Router>
  );
}

export default App;


