import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import JobListPage from './pages/JobListPage';
import JobDetailPage from './pages/JobDetailPage';
import AdminDashboard from './pages/admin/AdminDashboard';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <header className="App-header">
          <h1>HireSync 채용 정보</h1>
        </header>
        <main>
          <Routes>
            <Route path="/" element={<JobListPage />} />
            <Route path="/job-postings/:id" element={<JobDetailPage />} />
            <Route path="/admin" element={<AdminDashboard />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
