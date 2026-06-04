import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import './LoginPage.css';

function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [remember, setRemember] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      // .env의 REACT_APP_BACKEND 환경변수를 활용하여 API 호출
      const backendUrl = process.env.REACT_APP_BACKEND || '';
      const response = await axios.post(`${backendUrl}/api/v1/auth/login`, {
        email,
        password
      });

      if (response.data && response.data.status === 'success') {
        const token = response.data.data; // JWT 토큰 값 추출
        
        // 토큰 로컬 스토리지에 보관
        localStorage.setItem('authToken', token);
        
        alert('로그인에 성공했습니다.');
        navigate('/admin'); // 관리자 대시보드로 이동
      } else {
        setError('로그인 처리에 실패했습니다. 응답 형식을 확인하세요.');
      }
    } catch (err) {
      console.error('로그인 에러:', err);
      // 백엔드 에러 응답 및 일반 네트워크 예외 처리
      const errorMessage = err.response?.data?.message || '이메일 혹은 비밀번호가 올바르지 않습니다.';
      setError(errorMessage);
    }
  };

  const handleCancel = () => {
    setEmail('');
    setPassword('');
    setError('');
    navigate('/'); // 메인 채용공고 리스트로 이동
  };

  return (
    <div className="login-page-root">
      {/* Login Container Card */}
      <main className="login-card">
        {/* Header */}
        <div className="login-header">
          <div className="login-icon-container">
            <span className="material-symbols-outlined" style={{ fontSize: '28px' }}>
              lock_person
            </span>
          </div>
          <h1 className="login-title">로그인</h1>
          <p className="login-subtitle">CareerAdmin 시스템에 접속하세요.</p>
        </div>

        {/* Error Alert Display */}
        {error && (
          <div style={{
            color: '#991B1B',
            backgroundColor: '#FEE2E2',
            border: '1px solid #FCA5A5',
            padding: '12px',
            borderRadius: '4px',
            fontSize: '14px',
            marginBottom: '16px',
            textAlign: 'center',
            fontWeight: '500'
          }}>
            {error}
          </div>
        )}

        {/* Login Form */}
        <form className="login-form" onSubmit={handleSubmit}>
          {/* Email Input */}
          <div className="input-field-group">
            <label className="input-label" htmlFor="email">이메일</label>
            <div className="input-wrapper">
              <span className="input-icon">
                <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>
                  mail
                </span>
              </span>
              <input
                type="email"
                id="email"
                name="email"
                className="login-input"
                placeholder="admin@career.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
          </div>

          {/* Password Input */}
          <div className="input-field-group">
            <label className="input-label" htmlFor="password">비밀번호</label>
            <div className="input-wrapper">
              <span className="input-icon">
                <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>
                  key
                </span>
              </span>
              <input
                type="password"
                id="password"
                name="password"
                className="login-input"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
          </div>

          {/* Options Row */}
          <div className="login-options-row">
            <label className="remember-me-label">
              <input
                type="checkbox"
                name="remember"
                className="remember-me-checkbox"
                checked={remember}
                onChange={(e) => setRemember(e.target.checked)}
              />
              <span className="remember-me-text">로그인 상태 유지</span>
            </label>
            <Link className="forgot-password-link" to="/forgot-password">
              비밀번호 찾기
            </Link>
          </div>

          {/* Actions Row */}
          <div className="login-action-group">
            <button className="btn btn-primary" type="submit">
              <span>로그인</span>
              <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                arrow_forward
              </span>
            </button>
            <button className="btn btn-outline" type="button" onClick={handleCancel}>
              취소
            </button>
          </div>
        </form>

        {/* Footer Links */}
        <div className="login-footer">
          <span className="footer-text">계정이 없으신가요?</span>
          <Link to="/register" className="signup-link">
            아이디 등록
          </Link>
        </div>
      </main>
    </div>
  );
}

export default LoginPage;
