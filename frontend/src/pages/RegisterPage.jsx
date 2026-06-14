import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../services/api';
import './RegisterPage.css';

function RegisterPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      // API Contracts 규격에 맞게 POST /api/v1/auth/register 전송
      const response = await api.post('/api/v1/auth/register', {
        email,
        password,
        name,
        role: 'ADMIN' // 새 시스템 관리자 계정 생성 기본
      });

      if (response.data && response.data.status === 'success') {
        alert('사용자 등록이 성공적으로 완료되었습니다. 로그인 화면으로 이동합니다.');
        navigate('/login'); // 가입 성공 후 로그인으로 이동
      } else {
        setError('등록 처리에 실패했습니다. 서버 상태를 확인해 주세요.');
      }
    } catch (err) {
      console.error('가입 요청 에러:', err);
      const errorMessage = err.response?.data?.message || '이메일이 이미 존재하거나 입력 형식이 올바르지 않습니다.';
      setError(errorMessage);
    }
  };

  const handleCancel = () => {
    navigate('/login'); // 로그인 화면으로 복귀
  };

  return (
    <div className="register-page-root">
      {/* Main Registration Container Card */}
      <main className="register-card">
        {/* Header */}
        <header className="register-header">
          <div className="register-brand-row">
            <span className="material-symbols-outlined register-brand-icon">
              work_history
            </span>
            <span className="register-brand-text">CareerAdmin</span>
          </div>
          <h1 className="register-title">사용자 등록</h1>
          <p className="register-subtitle">새 관리자 계정을 생성하려면 아래 정보를 입력하세요.</p>
        </header>

        {/* Error Alert Display */}
        {error && (
          <div style={{
            color: '#991B1B',
            backgroundColor: '#FEE2E2',
            border: '1px solid #FCA5A5',
            padding: '12px',
            borderRadius: '4px',
            fontSize: '14px',
            margin: '24px 24px 0 24px',
            textAlign: 'center',
            fontWeight: '500'
          }}>
            {error}
          </div>
        )}

        {/* Form Area */}
        <form className="register-form" onSubmit={handleSubmit}>
          {/* Email Field */}
          <div className="register-field-group">
            <label className="register-label" htmlFor="email">이메일</label>
            <div className="register-input-wrapper">
              <span className="register-input-icon">
                <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>
                  mail
                </span>
              </span>
              <input
                type="email"
                id="email"
                name="email"
                className="register-input"
                placeholder="admin@careeradmin.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
          </div>

          {/* Password Field */}
          <div className="register-field-group">
            <label className="register-label" htmlFor="password">비밀번호</label>
            <div className="register-input-wrapper">
              <span className="register-input-icon">
                <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>
                  lock
                </span>
              </span>
              <input
                type="password"
                id="password"
                name="password"
                className="register-input"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
          </div>

          {/* Name Field */}
          <div className="register-field-group">
            <label className="register-label" htmlFor="name">이름</label>
            <div className="register-input-wrapper">
              <span className="register-input-icon">
                <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>
                  person
                </span>
              </span>
              <input
                type="text"
                id="name"
                name="name"
                className="register-input"
                placeholder="홍길동"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>
          </div>

          {/* Action Row */}
          <div className="register-action-row">
            <button className="reg-btn reg-btn-outline" type="button" onClick={handleCancel}>
              취소
            </button>
            <button className="reg-btn reg-btn-primary" type="submit">
              확인
            </button>
          </div>
        </form>
        {/* Footer Links */}
        <div className="login-footer" style={{ marginTop: '16px', textAlign: 'center' }}>
          <span className="footer-text">이미 계정이 있으신가요? </span>
          <Link to="/login" className="signup-link">
            로그인
          </Link>
        </div>
      </main>
    </div>
  );
}

export default RegisterPage;
