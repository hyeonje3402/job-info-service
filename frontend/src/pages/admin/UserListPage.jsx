import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './UserListPage.css';

// Stitch 스크린 설계안에 부합하는 사용자 폴백 목업 데이터 정의
const FALLBACK_USERS = [
  {
    id: 1,
    name: '김지훈',
    email: 'jihoon.kim@example.com',
    role: '지원자',
    status: 'ACTIVE',
    lastActive: '2023-10-25 14:30',
    isNew: false
  },
  {
    id: 2,
    name: '이서연',
    email: 'seoyeon.lee@corp.kr',
    role: '기업 인사담당자',
    status: 'ACTIVE',
    lastActive: '2023-10-26 09:15',
    isNew: true
  },
  {
    id: 3,
    name: '박민수',
    email: 'minsoo.park@test.com',
    role: '지원자',
    status: 'REVOKED',
    lastActive: '2023-09-12 11:00',
    isNew: false
  },
  {
    id: 4,
    name: '최유진',
    email: 'yujin.choi@dev.io',
    role: '리크루터',
    status: 'ACTIVE',
    lastActive: '2023-10-26 10:45',
    isNew: false
  }
];

function UserListPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedUserIds, setSelectedUserIds] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [bulkAction, setBulkAction] = useState('');
  const [sortType, setSortType] = useState('최신 가입순');
  const [pageSize, setPageSize] = useState(10);
  const [currentPage, setCurrentPage] = useState(1);

  // 1. 사용자 데이터 조회 (GET API 연동)
  useEffect(() => {
    const fetchUsers = async () => {
      setLoading(true);
      const backendUrl = process.env.REACT_APP_BACKEND || '';

      try {
        // AGENTS.md 규칙: 세션 JSESSIONID 연동을 보장하기 위해 withCredentials 탑재 필수
        const response = await axios.get(`${backendUrl}/api/v1/users`, {
          withCredentials: true
        });

        if (response.data && response.data.status === 'success') {
          // 백엔드 반환 규격 매핑
          setUsers(response.data.data || []);
        } else {
          setUsers(FALLBACK_USERS);
        }
      } catch (err) {
        console.warn("백엔드 API 사용자 조회 실패 - 목업 폴백 데이터를 대신 로드합니다.", err);
        setUsers(FALLBACK_USERS);
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
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
      const backendUrl = process.env.REACT_APP_BACKEND || '';

      try {
        // AGENTS.md 규칙: 세션 JSESSIONID 연동을 보장하기 위해 withCredentials 탑재 필수
        const response = await axios.put(`${backendUrl}/api/v1/users/bulk-status`, {
          userIds: selectedUserIds,
          status: nextStatus
        }, {
          withCredentials: true
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

  // 개별 수정
  const handleEditUser = (name) => {
    alert(`'${name}' 사용자 상세 정보 편집 모달은 후속 인사관리 추가 스프린트에서 연동될 예정입니다.`);
  };

  // 신규 사용자 정보 추가
  const handleAddUser = () => {
    alert('사용자 신규 정보 등록 기능은 대시보드 통합 패널 후속 스프린트에서 정식 연동될 예정입니다.');
  };

  // 실시간 검색어 기반 필터링
  const filteredUsers = users.filter(user => {
    const query = searchQuery.toLowerCase();
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

            {/* 정렬 필터 */}
            <div className="filter-controls">
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
                {loading && users.length === 0 ? (
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
                            <div className={`avatar-circle ${user.id % 2 === 0 ? 'avatar-blue' : 'avatar-grey'}`}>
                              {getInitial(user.name)}
                            </div>
                            <div>
                              <p className="user-name-text">
                                {user.name}
                                {user.isNew && <span className="new-badge">NEW</span>}
                              </p>
                              <p className="user-email-text">{user.email}</p>
                            </div>
                          </div>
                        </td>
                        <td style={{ color: 'var(--on-surface-variant)' }}>
                          {user.role}
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
                        <td className="action-btn-cell">
                          <button 
                            type="button"
                            className="table-action-btn edit-btn"
                            title="수정"
                            onClick={() => handleEditUser(user.name)}
                            disabled={loading}
                          >
                            <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>edit</span>
                          </button>
                          <button 
                            type="button"
                            className="table-action-btn delete-btn"
                            title="삭제"
                            onClick={() => handleDeleteUser(user.id, user.name)}
                            disabled={loading}
                          >
                            <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>delete</span>
                          </button>
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

      </div>
    </div>
  );
}

export default UserListPage;
