import React from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation, Navigate } from 'react-router-dom';
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
import SystemSettingsPage from './pages/SystemSettingsPage';
import './index.css';

// 비로그인 사용자의 보호된 경로 접근을 차단하고 로그인 화면으로 리다이렉트하는 Guard 컴포넌트
function ProtectedRoute({ children }) {
  const token = localStorage.getItem('authToken');
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

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
          <Route path="/job-postings/:id" element={<JobDetailPage />} />

          {/* 라우트 가드(ProtectedRoute)가 적용된 보호된 관리자 전용 라우트들 */}
          <Route path="/jobs/new" element={<ProtectedRoute><JobAddPage /></ProtectedRoute>} />
          <Route path="/jobs/:id/edit" element={<ProtectedRoute><JobEditPage /></ProtectedRoute>} />
          <Route path="/users-management" element={<ProtectedRoute><UserListPage /></ProtectedRoute>} />
          <Route path="/users-management/add" element={<ProtectedRoute><UserCreatePage /></ProtectedRoute>} />
          <Route path="/users-management/:id/edit" element={<ProtectedRoute><UserEditPage /></ProtectedRoute>} />
          <Route path="/admin" element={<ProtectedRoute><AdminDashboard /></ProtectedRoute>} />
          <Route path="/admin/dashboard" element={<ProtectedRoute><AdminDashboard /></ProtectedRoute>} />
          <Route path="/settings" element={<ProtectedRoute><SystemSettingsPage /></ProtectedRoute>} />
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


