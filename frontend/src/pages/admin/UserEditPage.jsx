import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import api from '../../services/api';
import './UserEditPage.css';

const UserEditPage = () => {
  const { id } = useParams();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('USER');
  const [phone, setPhone] = useState('');
  const [profileImage, setProfileImage] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();

  // 기존 사용자 데이터 조회 (GET /api/v1/users/{id})
  useEffect(() => {
    const fetchUserDetail = async () => {
      try {
        const response = await api.get(`/api/v1/users/${id}`);
        if (response.data && response.data.status === 'success') {
          const user = response.data.data;
          setName(user.name || '');
          setEmail(user.email || '');
          setRole(user.role || 'USER');
          setPhone(user.phone || '');
          setProfileImage(user.profileImage || '');
        } else {
          setError('사용자 정보를 불러올 수 없습니다.');
        }
      } catch (err) {
        setError('사용자 데이터 로드 중 오류가 발생했습니다.');
      } finally {
        setLoading(false);
      }
    };
    fetchUserDetail();
  }, [id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      // 명세에 따른 사용자 수정 PUT API 호출
      const response = await api.put(`/api/v1/users/${id}`, { name, email, password, role, phone, profileImage });
      if (response.data.status === 'success') {
        setSuccess('사용자 정보가 성공적으로 수정되었습니다.');
        setTimeout(() => {
          navigate('/users-management');
        }, 1500);
      } else {
        setError('사용자 수정에 실패했습니다.');
      }
    } catch (err) {
      setError(err.response?.data?.message || '사용자 정보 수정 중 오류가 발생했습니다.');
    }
  };

  if (loading) return <div className="message">사용자 정보를 조회하고 있습니다...</div>;

  return (
    <div className="user-edit-container">
      <Link to="/users-management" className="back-link">← 사용자 목록으로 돌아가기</Link>
      <h2 className="user-edit-title">사용자 정보 수정</h2>
      
      <form onSubmit={handleSubmit} className="user-edit-form">
        <div className="form-group">
          <label className="form-label">이름</label>
          <input 
            type="text" 
            className="form-input"
            placeholder="이름 입력" 
            value={name} 
            onChange={(e) => setName(e.target.value)} 
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
          <label className="form-label">새 비밀번호 (변경시에만 입력)</label>
          <input 
            type="password" 
            className="form-input"
            placeholder="••••••••" 
            value={password} 
            onChange={(e) => setPassword(e.target.value)} 
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
          사용자 정보 수정 완료
        </button>
      </form>

      {error && <p className="error">{error}</p>}
      {success && <p className="success">{success}</p>}
    </div>
  );
};

export default UserEditPage;

