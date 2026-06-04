import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './ForgotPasswordPage.css';

function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');

    // 가상 이메일 전송 딜레이 연출 (1초)
    setTimeout(() => {
      setLoading(false);
      alert(`${email} 주소로 비밀번호 재설정 링크를 전송하였습니다. 메일함을 확인해 주세요.`);
      navigate('/login'); // 로그인 화면으로 복귀
    }, 1000);
  };

  const handleCancel = () => {
    navigate('/login'); // 로그인 화면으로 복귀
  };

  return (
    <div className="forgot-page-root">
      {/* Main Password Reset Card */}
      <main className="forgot-card">
        {/* Header */}
        <header className="forgot-header">
          <div className="forgot-icon-container">
            <span className="material-symbols-outlined" style={{ fontSize: '32px' }}>
              lock_reset
            </span>
          </div>
          <div className="flex flex-col gap-sm">
            <h1 className="forgot-title">비밀번호 분실</h1>
            <p className="forgot-subtitle">
              가입 시 사용한 이메일 주소를 입력해 주세요. 등록된 이메일로 비밀번호 재설정 링크를 보내드립니다.
            </p>
          </div>
        </header>

        {/* Action Form */}
        <form className="forgot-form" onSubmit={handleSubmit}>
          {/* Email Field */}
          <div className="forgot-field-group">
            <label className="forgot-label" htmlFor="email">이메일 주소</label>
            <div className="forgot-input-wrapper">
              <span className="forgot-input-icon">
                <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>
                  mail
                </span>
              </span>
              <input
                type="email"
                id="email"
                name="email"
                className="forgot-input"
                placeholder="user@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                disabled={loading}
              />
            </div>
          </div>

          {/* Action Buttons */}
          <div className="forgot-action-group">
            <button 
              className="forgot-btn forgot-btn-outline" 
              type="button" 
              onClick={handleCancel}
              disabled={loading}
            >
              취소
            </button>
            <button 
              className="forgot-btn forgot-btn-primary" 
              type="submit"
              disabled={loading}
            >
              {loading ? '전송 중...' : '확인'}
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}

export default ForgotPasswordPage;
