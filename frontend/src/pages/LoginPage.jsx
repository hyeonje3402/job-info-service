import React, { useState, useEffect } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import api from '../services/api';
import './LoginPage.css';

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [remember, setRemember] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (params.get('error') === 'unauthorized') {
      setError('인증이 만료되었거나 권한이 없습니다. 다시 로그인해 주세요.');
    }
  }, [location]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      // api 인스턴스를 활용하여 API 호출 (withCredentials 자동 적용)
      const response = await api.post('/api/v1/auth/login', {
        email,
        password
      });

      if (response.data && response.data.status === 'success') {
        const userData = response.data.data;
        const authorities = userData.authorities || [];
        const isAdmin = authorities.some(auth => auth.authority === 'ADMIN' || auth.authority === 'ROLE_ADMIN');
        
        // 토큰 및 역할 정보를 로컬 스토리지에 보관
        localStorage.setItem('authToken', userData.email || 'user');
        localStorage.setItem('userRole', isAdmin ? 'ADMIN' : 'GENERAL');
        
        alert('로그인에 성공했습니다.');
        if (isAdmin) {
          navigate('/admin/dashboard'); // 관리자 대시보드로 이동
        } else {
          navigate('/'); // 일반 사용자는 메인 채용공고 목록으로 이동
        }
      } else {
        setError('로그인 처리에 실패했습니다. 응답 형식을 확인하세요.');
      }
    } catch (err) {
      console.error('로그인 에러:', err);
      // 예외 발생 시 크래시가 나지 않도록 안전하게 에러 메시지 추출
      let errorMessage = '이메일 또는 비밀번호가 올바르지 않습니다.';
      if (err) {
        if (err.response && err.response.data && err.response.data.message) {
          errorMessage = err.response.data.message;
        } else if (err.message) {
          errorMessage = err.message;
        }
      }
      setError(`로그인에 실패했습니다. (${errorMessage})`);
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
          <div 
            style={{ color: 'var(--color-error)' }} 
            className="login-error-alert-box"
          >
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
