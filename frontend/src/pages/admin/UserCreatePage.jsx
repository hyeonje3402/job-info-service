import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../../services/api';
import './UserCreatePage.css';

const UserCreatePage = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('USER');
  const [phone, setPhone] = useState('');
  const [profileImage, setProfileImage] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const response = await api.post('/api/v1/auth/register', { name, email, password, role, phone, profileImage });
      if (response.data.status === 'success') {
        setSuccess('사용자 정보가 성공적으로 등록되었습니다.');
        setTimeout(() => {
          navigate('/users-management');
        }, 1500);
      } else {
        setError('사용자 등록에 실패했습니다.');
      }
    } catch (err) {
      setError(err.response?.data?.message || '사용자 등록 요청 중 오류가 발생했습니다.');
    }
  };

  return (
    <div className="user-create-container">
      <Link to="/users-management" className="back-link">← 사용자 목록으로 돌아가기</Link>
      <h2 className="user-create-title">사용자 정보 추가</h2>
      
      <form onSubmit={handleSubmit} className="user-create-form">
        <div className="form-group">
          <label className="form-label">이름</label>
          <input 
            type="text" 
            className="form-input"
            placeholder="이름 입력" 
            value={name} 
            onChange={(e) => setName(e.target.value)} 
            required 
          />
        </div>
        
        <div className="form-group">
          <label className="form-label">이메일 주소</label>
          <input 
            type="email" 
            className="form-input"
            placeholder="example@domain.com" 
            value={email} 
            onChange={(e) => setEmail(e.target.value)} 
            required 
          />
        </div>
        
        <div className="form-group">
          <label className="form-label">초기 비밀번호</label>
          <input 
            type="password" 
            className="form-input"
            placeholder="••••••••" 
            value={password} 
            onChange={(e) => setPassword(e.target.value)} 
            required 
          />
        </div>
        
        <div className="form-group">
          <label className="form-label">연락처 (선택)</label>
          <input 
            type="text" 
            className="form-input"
            placeholder="010-XXXX-XXXX" 
            value={phone} 
            onChange={(e) => setPhone(e.target.value)} 
          />
        </div>
        
        <div className="form-group">
          <label className="form-label">프로필 이미지 URL (선택)</label>
          <input 
            type="url" 
            className="form-input"
            placeholder="https://example.com/avatar.jpg" 
            value={profileImage} 
            onChange={(e) => setProfileImage(e.target.value)} 
          />
        </div>
        
        <div className="form-group">
          <label className="form-label">계정 역할</label>
          <select 
            value={role} 
            onChange={(e) => setRole(e.target.value)} 
            className="form-select"
          >
            <option value="USER">일반 사용자 (USER)</option>
            <option value="ADMIN">관리자 (ADMIN)</option>
          </select>
        </div>
        
        <button type="submit" className="submit-btn">
          사용자 정보 등록
        </button>
      </form>

      {error && <p className="error">{error}</p>}
      {success && <p className="success">{success}</p>}
    </div>
  );
};

export default UserCreatePage;

