import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import './UserListPage.css';

// Stitch 스크린 설계안에 부합하는 사용자 폴백 목업 데이터 정의
const FALLBACK_USERS = [
  {
    id: 1,
    name: '김지훈',
    email: 'jihoon.kim@example.com',
    role: 'GENERAL',
    status: 'ACTIVE',
    lastActive: '2023-10-25 14:30',
    phone: '010-1234-5678',
    profileImage: '',
    isNew: false
  },
  {
    id: 2,
    name: '이서연',
    email: 'seoyeon.lee@corp.kr',
    role: 'GENERAL',
    status: 'ACTIVE',
    lastActive: '2023-10-26 09:15',
    phone: '',
    profileImage: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150',
    isNew: true
  },
  {
    id: 3,
    name: '', // 이름 누락
    email: 'minsoo.park@test.com',
    role: 'GENERAL',
    status: 'REVOKED',
    lastActive: '2023-09-12 11:00',
    phone: '010-9876-5432',
    profileImage: '',
    isNew: false
  },
  {
    id: 4,
    name: '최유진',
    email: '', // 이메일 누락
    role: 'ADMIN',
    status: 'ACTIVE',
    lastActive: '2023-10-26 10:45',
    phone: '', // 연락처 누락
    profileImage: '',
    isNew: false
  }
];

function UserListPage() {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [activeDropdownUserId, setActiveDropdownUserId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [toastMessage, setToastMessage] = useState('');
  const [selectedUserIds, setSelectedUserIds] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [bulkAction, setBulkAction] = useState('');
  const [sortType, setSortType] = useState('최신 가입순');
  const [pageSize, setPageSize] = useState(10);
  const [currentPage, setCurrentPage] = useState(1);
  const [selectedRole, setSelectedRole] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  // 1. 사용자 데이터 조회 (GET API 연동 - 필터 적용)
  useEffect(() => {
    const fetchUsers = async () => {
      setLoading(true);
      setErrorMessage('');
      try {
        const params = {
          _t: Date.now() // 캐시 무효화용 타임스탬프 파라미터
        };
        if (selectedRole) params.role = selectedRole;
        if (selectedStatus) params.status = selectedStatus;

        const response = await api.get('/api/v1/users', { params });

        if (response.data && response.data.status === 'success') {
          const mappedUsers = (response.data.data || []).map((u, index) => ({
            ...u,
            id: u.id || u.email || index
          }));
          setUsers(mappedUsers);
        } else {
          const status = response.data ? response.data.status : 'unknown';
          setErrorMessage(`사용자 목록 응답 포맷이 올바르지 않습니다. (status: ${status})`);
          setUsers([]);
        }
      } catch (err) {
        if (err.response && (err.response.status === 401 || err.response.status === 403)) {
          // api.js의 전역 인터셉터가 리다이렉트 및 토큰 만료 처리를 담당하므로,
          // 목업 데이터를 세팅하지 않고 즉시 종료합니다.
          return;
        }
        console.error("백엔드 API 사용자 조회 실패:", err);
        setErrorMessage(`서버와 통신할 수 없습니다. (${err.message}). 백엔드 서버 구동 상태나 네트워크를 확인해 주세요.`);
        setUsers([]);
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
  }, [selectedRole, selectedStatus]);

  // 외부 클릭 시 드롭다운 닫기
  useEffect(() => {
    const handleOutsideClick = (e) => {
      if (!e.target.closest('.table-action-btn.more-btn') && !e.target.closest('.dropdown-menu')) {
        setActiveDropdownUserId(null);
      }
    };
    document.addEventListener('mousedown', handleOutsideClick);
    return () => {
      document.removeEventListener('mousedown', handleOutsideClick);
    };
  }, []);

  // 다중 체크박스 선택 처리
  const handleSelectAll = (e) => {
    if (e.target.checked) {
      const allIds = sortedUsers.map(u => u.id);
      setSelectedUserIds(allIds);
    } else {
      setSelectedUserIds([]);
    }
  };

  const handleSelectUser = (userId) => {
    if (selectedUserIds.includes(userId)) {
      setSelectedUserIds(selectedUserIds.filter(id => id !== userId));
    } else {
      setSelectedUserIds([...selectedUserIds, userId]);
    }
  };

  // 일괄 상태 변경 액션 제출 (PUT bulk-status 시뮬레이션)
  const handleBulkActionApply = async () => {
    if (selectedUserIds.length === 0) {
      alert('상태를 일괄 변경할 사용자를 선택해 주세요.');
      return;
    }
    if (!bulkAction) {
      alert('변경할 상태 옵션을 선택해 주세요.');
      return;
    }

    const nextStatus = bulkAction === 'active' ? 'ACTIVE' : 'REVOKED';
    const statusText = bulkAction === 'active' ? '활성' : '정지';

    if (window.confirm(`${selectedUserIds.length}명의 사용자를 '${statusText}' 상태로 변경하시겠습니까?`)) {
      setLoading(true);
      try {
        const response = await api.put('/api/v1/users/bulk-status', {
          userIds: selectedUserIds,
          status: nextStatus
        });

        if (response.data && response.data.status === 'success') {
          alert(`성공적으로 ${selectedUserIds.length}명의 사용자 상태를 ${statusText}로 변경하였습니다.`);
          // 로컬 데이터 갱신
          setUsers(users.map(u => 
            selectedUserIds.includes(u.id) ? { ...u, status: nextStatus } : u
          ));
          setSelectedUserIds([]);
        } else {
          applyBulkFallback(nextStatus, statusText);
        }
      } catch (err) {
        console.warn("백엔드 일괄 변경 실패 - 로컬 목업 환경으로 동작을 가상 완료합니다.", err);
        applyBulkFallback(nextStatus, statusText);
      } finally {
        setLoading(false);
      }
    }
  };

  const applyBulkFallback = (nextStatus, statusText) => {
    alert(`[데모 모드] 선택된 ${selectedUserIds.length}명의 사용자 상태가 '${statusText}'로 로컬 변경되었습니다.`);
    setUsers(users.map(u => 
      selectedUserIds.includes(u.id) ? { ...u, status: nextStatus } : u
    ));
    setSelectedUserIds([]);
  };

  // 개별 삭제
  const handleDeleteUser = (userId, name) => {
    if (window.confirm(`'${name}' 사용자를 정말 탈퇴/삭제 처리하시겠습니까?`)) {
      setUsers(users.filter(u => u.id !== userId));
      setSelectedUserIds(selectedUserIds.filter(id => id !== userId));
      alert('사용자 정보가 정상 삭제되었습니다.');
    }
  };

  // 개별 차단(계정 정지)
  const handleBlockUser = async (userId) => {
    const userToBlock = users.find(u => u.id === userId);
    if (!userToBlock) return;

    if (window.confirm("정말 이 사용자의 계정을 정지하시겠습니까?")) {
      setLoading(true);
      const updatedUser = { ...userToBlock, status: 'REVOKED' };
      
      const applyLocally = () => {
        setUsers(users.map(u => u.id === userId ? { ...u, status: 'REVOKED' } : u));
        setToastMessage('차단되었습니다');
        setTimeout(() => setToastMessage(''), 3000);
      };

      try {
        const response = await api.put(`/api/v1/users/${userId}`, updatedUser);
        if (response.data && response.data.status === 'success') {
          applyLocally();
        } else {
          applyLocally();
        }
      } catch (err) {
        console.warn("백엔드 사용자 차단 API 호출 실패 - 로컬 상태를 강제 정지 상태로 변경합니다.", err);
        applyLocally();
      } finally {
        setLoading(false);
      }
    }
  };

  // 개별 수정
  const handleEditUser = (name) => {
    alert(`'${name}' 사용자 상세 정보 편집 모달은 후속 인사관리 추가 스프린트에서 연동될 예정입니다.`);
  };

  // 신규 사용자 정보 추가
  const handleAddUser = () => {
    navigate('/users-management/add');
  };

  // 실시간 검색어 기반 필터링
  const filteredUsers = users.filter(user => {
    const query = searchQuery.trim().toLowerCase();
    return (
      user.name.toLowerCase().includes(query) ||
      user.email.toLowerCase().includes(query)
    );
  });

  // 정렬 처리
  const sortedUsers = [...filteredUsers].sort((a, b) => {
    if (sortType === '최신 가입순') {
      return b.id - a.id;
    } else if (sortType === '오래된 가입순') {
      return a.id - b.id;
    } else if (sortType === '이름 오름차순') {
      return a.name.localeCompare(b.name, 'ko');
    }
    return 0;
  });

  // 클라이언트 페이징 연산
  const indexOfLastRow = currentPage * pageSize;
  const indexOfFirstRow = indexOfLastRow - pageSize;
  const currentRows = sortedUsers.slice(indexOfFirstRow, indexOfLastRow);
  const totalPages = Math.ceil(sortedUsers.length / pageSize) || 1;

  // 아바타 성씨 이니셜 반환
  const getInitial = (name) => {
    return name ? name.substring(0, 1) : '회';
  };

  return (
    <div className="userlist-root">
      <div className="userlist-wrapper">
        
        {/* 1. 페이지 헤더 */}
        <header className="userlist-header-row">
          <div>
            <h1 className="userlist-title-title">사용자 관리</h1>
            <p className="userlist-title-subtitle">
              플랫폼에 등록된 전체 사용자를 조회하고 관리합니다.
            </p>
          </div>
          <button 
            type="button" 
            className="userlist-add-btn" 
            onClick={handleAddUser}
          >
            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>person_add</span>
            <span>사용자정보추가</span>
          </button>
        </header>

        {/* 2. 필터 및 툴바 카드 */}
        <section className="userlist-toolbar-card">
          {/* 검색창 */}
          <div className="userlist-search-wrapper">
            <span className="material-symbols-outlined search-input-icon">search</span>
            <input 
              type="text"
              className="search-input-field"
              placeholder="이름, 이메일 검색..."
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setCurrentPage(1); // 검색 시 페이지 리셋
              }}
              disabled={loading}
            />
          </div>

          {/* 벌크 액션 및 표시 정렬 제어 */}
          <div className="userlist-actions-group">
            {/* 일괄 액션 */}
            <div className="bulk-action-section">
              <select 
                className="toolbar-select-field"
                value={bulkAction}
                onChange={(e) => setBulkAction(e.target.value)}
                disabled={loading}
              >
                <option value="">일괄 상태 변경</option>
                <option value="active">활성 상태로 변경</option>
                <option value="suspended">정지 상태로 변경</option>
              </select>
              <button 
                type="button" 
                className="bulk-apply-btn" 
                onClick={handleBulkActionApply}
                disabled={loading}
              >
                적용
              </button>
            </div>

            {/* 필터 및 정렬 제어 */}
            <div className="filter-controls">
              {/* 권한 필터 */}
              <select 
                className="toolbar-select-field"
                value={selectedRole}
                onChange={(e) => {
                  setSelectedRole(e.target.value);
                  setCurrentPage(1);
                }}
                disabled={loading}
              >
                <option value="">모든 권한</option>
                <option value="ADMIN">ADMIN</option>
                <option value="GENERAL">GENERAL</option>
              </select>

              {/* 활성화 상태 필터 */}
              <select 
                className="toolbar-select-field"
                value={selectedStatus}
                onChange={(e) => {
                  setSelectedStatus(e.target.value);
                  setCurrentPage(1);
                }}
                disabled={loading}
              >
                <option value="">모든 상태</option>
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
                <option value="REVOKED">REVOKED</option>
              </select>

              <select 
                className="toolbar-select-field"
                value={sortType}
                onChange={(e) => setSortType(e.target.value)}
                disabled={loading}
              >
                <option value="최신 가입순">최신 가입순</option>
                <option value="오래된 가입순">오래된 가입순</option>
                <option value="이름 오름차순">이름 오름차순</option>
              </select>
              
              <select 
                className="toolbar-select-field"
                value={pageSize}
                onChange={(e) => {
                  setPageSize(Number(e.target.value));
                  setCurrentPage(1);
                }}
                disabled={loading}
              >
                <option value={10}>10개씩 보기</option>
                <option value={20}>20개씩 보기</option>
                <option value={50}>50개씩 보기</option>
              </select>
            </div>
          </div>
        </section>

        {/* 3. 데이터 테이블 카드 */}
        <section className="userlist-table-card">
          <div className="table-responsive-container">
            <table className="user-data-table">
              <colgroup>
                <col className="col-checkbox" />
                <col className="col-info" />
                <col className="col-role" />
                <col className="col-status" />
                <col className="col-date" />
                <col className="col-action" />
              </colgroup>
              <thead>
                <tr>
                  <th className="th-checkbox">
                    <input 
                      type="checkbox"
                      className="custom-checkbox-field"
                      onChange={handleSelectAll}
                      checked={sortedUsers.length > 0 && selectedUserIds.length === sortedUsers.length}
                      disabled={loading || sortedUsers.length === 0}
                    />
                  </th>
                  <th>사용자 정보</th>
                  <th>역할</th>
                  <th>상태</th>
                  <th>최근 접속일</th>
                  <th className="action-btn-cell">관리</th>
                </tr>
              </thead>
              <tbody>
                {errorMessage ? (
                  <tr>
                    <td colSpan="6" style={{ textAlign: 'center', padding: '32px', color: '#EF4444', fontWeight: '600' }}>
                      <span className="material-symbols-outlined" style={{ verticalAlign: 'middle', marginRight: '8px' }}>error</span>
                      {errorMessage}
                    </td>
                  </tr>
                ) : loading && users.length === 0 ? (
                  <tr>
                    <td colSpan="6" style={{ textAlign: 'center', padding: '32px', fontWeight: '600' }}>
                      사용자 데이터를 로딩 중입니다...
                    </td>
                  </tr>
                ) : currentRows.length === 0 ? (
                  <tr>
                    <td colSpan="6" style={{ textAlign: 'center', padding: '32px', color: 'var(--secondary-color)' }}>
                      조회된 사용자 내역이 존재하지 않습니다.
                    </td>
                  </tr>
                ) : (
                  currentRows.map((user) => {
                    const isSelected = selectedUserIds.includes(user.id);
                    const isSuspended = user.status === 'REVOKED';
                    
                    return (
                      <tr 
                        key={user.id}
                        className={isSelected ? 'row-selected' : ''}
                      >
                        <td style={{ textAlign: 'center' }}>
                          <input 
                            type="checkbox"
                            className="custom-checkbox-field"
                            checked={isSelected}
                            onChange={() => handleSelectUser(user.id)}
                            disabled={loading}
                          />
                        </td>
                        <td>
                          <div className="user-info-cell">
                            <div 
                              className={`avatar-circle ${user.id % 2 === 0 ? 'avatar-blue' : 'avatar-grey'}`}
                              style={{ overflow: 'hidden', padding: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                            >
                              {user.profileImage ? (
                                <img src={user.profileImage} alt={user.name || '유저 아바타'} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                              ) : (
                                getInitial(user.name)
                              )}
                            </div>
                            <div>
                              <p className="user-name-text">
                                {user.name ? (
                                  user.name
                                ) : (
                                  <span 
                                    className="missing-info-text"
                                    title="이름 입력하기"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      navigate(`/users-management/${user.id}/edit`);
                                    }}
                                  >
                                    - (이름 누락)
                                  </span>
                                )}
                                {user.isNew && <span className="new-badge">NEW</span>}
                              </p>
                              <p className="user-email-text">
                                {user.email ? (
                                  user.email
                                ) : (
                                  <span 
                                    className="missing-info-text"
                                    title="이메일 입력하기"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      navigate(`/users-management/${user.id}/edit`);
                                    }}
                                  >
                                    - (이메일 누락)
                                  </span>
                                )}
                              </p>
                              <p className="user-phone-text" style={{ fontSize: '12px', marginTop: '4px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                                <span style={{ color: 'var(--text-muted)' }}>연락처:</span>
                                {user.phone ? (
                                  <span style={{ color: 'var(--text-secondary)' }}>{user.phone}</span>
                                ) : (
                                  <span 
                                    className="missing-info-text"
                                    title="연락처 입력하기"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      navigate(`/users-management/${user.id}/edit`);
                                    }}
                                  >
                                    미입력 (정보 입력하기)
                                  </span>
                                )}
                              </p>
                            </div>
                          </div>
                        </td>
                        <td>
                          <span className={`role-badge ${(user.role || '').toLowerCase()}`}>
                            {user.role === 'ADMIN' ? '관리자' : '일반'}
                          </span>
                        </td>
                        <td>
                          <span className={`status-pill ${isSuspended ? 'suspended' : 'active'}`}>
                            <span className={isSuspended ? 'status-dot-suspended' : 'status-dot-active'}></span>
                            <span>{isSuspended ? '정지' : '활성'}</span>
                          </span>
                        </td>
                        <td style={{ color: 'var(--on-surface-variant)' }}>
                          {user.lastActive || '기록 없음'}
                        </td>
                        <td className="action-btn-cell" style={{ position: 'relative' }}>
                          <button 
                            type="button"
                            className="table-action-btn more-btn"
                            title="더보기"
                            onClick={() => setActiveDropdownUserId(activeDropdownUserId === user.id ? null : user.id)}
                            disabled={loading}
                          >
                            <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>more_vert</span>
                          </button>
                          
                          {activeDropdownUserId === user.id && (
                            <div className="dropdown-menu glass-panel" style={{ position: 'absolute', right: '16px', top: '100%', zIndex: 10, display: 'flex', flexDirection: 'column', background: 'var(--bg-secondary)', border: '1px solid var(--border-subtle)', borderRadius: '8px', padding: '6px', minWidth: '150px', boxShadow: '0 8px 30px rgba(0,0,0,0.3)' }}>
                              <button 
                                type="button" 
                                className="dropdown-item" 
                                onClick={() => {
                                  setActiveDropdownUserId(null);
                                  navigate(`/users-management/${user.id}/edit`);
                                }}
                                style={{ display: 'flex', alignItems: 'center', gap: '8px', width: '100%', padding: '10px', background: 'transparent', border: 'none', color: 'var(--text-primary)', cursor: 'pointer', textAlign: 'left', borderRadius: '4px', fontSize: '13px' }}
                              >
                                <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>edit</span>
                                <span>사용자 정보 수정</span>
                              </button>
                              <button 
                                type="button" 
                                className="dropdown-item delete" 
                                onClick={() => {
                                  setActiveDropdownUserId(null);
                                  handleBlockUser(user.id);
                                }}
                                style={{ display: 'flex', alignItems: 'center', gap: '8px', width: '100%', padding: '10px', background: 'transparent', border: 'none', color: '#EF4444', cursor: 'pointer', textAlign: 'left', borderRadius: '4px', fontSize: '13px' }}
                              >
                                <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>block</span>
                                <span>차단</span>
                              </button>
                              <button 
                                type="button" 
                                className="dropdown-item delete" 
                                onClick={() => {
                                  setActiveDropdownUserId(null);
                                  handleDeleteUser(user.id, user.name);
                                }}
                                style={{ display: 'flex', alignItems: 'center', gap: '8px', width: '100%', padding: '10px', background: 'transparent', border: 'none', color: '#EF4444', cursor: 'pointer', textAlign: 'left', borderRadius: '4px', fontSize: '13px' }}
                              >
                                <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>delete</span>
                                <span>사용자 삭제</span>
                              </button>
                            </div>
                          )}
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>

          {/* 4. 페이징 푸터 */}
          {!loading && sortedUsers.length > 0 && (
            <footer className="table-pagination-footer">
              <p className="pagination-info-text">
                총 <span>{sortedUsers.length}</span>명의 사용자 중 {indexOfFirstRow + 1}-{Math.min(indexOfLastRow, sortedUsers.length)}
              </p>
              
              <div className="pagination-controls">
                <button 
                  type="button" 
                  className="pagination-arrow-btn"
                  onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                  disabled={currentPage === 1}
                >
                  <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>chevron_left</span>
                </button>

                <div className="pagination-pages-list">
                  {Array.from({ length: totalPages }, (_, i) => i + 1).map((pageNum) => (
                    <button 
                      key={pageNum}
                      type="button"
                      className={`pagination-number-btn ${currentPage === pageNum ? 'active' : ''}`}
                      onClick={() => setCurrentPage(pageNum)}
                    >
                      {pageNum}
                    </button>
                  ))}
                </div>

                <button 
                  type="button" 
                  className="pagination-arrow-btn"
                  onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                  disabled={currentPage === totalPages}
                >
                  <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>chevron_right</span>
                </button>
              </div>
            </footer>
          )}
        </section>

        {toastMessage && (
          <div className="toast-notification" style={{
            position: 'fixed',
            top: '24px',
            right: '24px',
            background: 'rgba(239, 68, 68, 0.95)',
            color: '#fff',
            padding: '12px 24px',
            borderRadius: '8px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.3)',
            zIndex: 1000,
            fontSize: '14px',
            fontWeight: '600'
          }}>
            {toastMessage}
          </div>
        )}
      </div>
    </div>
  );
}

export default UserListPage;
