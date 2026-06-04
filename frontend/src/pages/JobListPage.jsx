import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import './JobListPage.css';

// Stitch 스펙 명세에 따른 실제 폴백(Fallback) 목업 데이터 정의
const FALLBACK_JOBS = [
  {
    id: 1,
    companyName: 'TechCorp Korea',
    location: '서울 강남구',
    position: '시니어 프론트엔드 개발자 (React/Next.js)',
    requirements: 'React, Next.js 실무 경력 5년 이상...',
    openingDate: '2023.10.24',
    applicationMethod: '이메일 지원',
    expectedSalary: '7,000 - 9,000 만원',
    status: 'APPROVED',
    applicants: 12
  },
  {
    id: 2,
    companyName: 'DataStream Inc.',
    location: '판교 테크노밸리',
    position: '데이터 엔지니어 (대규모 트래픽 처리)',
    requirements: 'Hadoop, Spark, Kafka 실무 경력...',
    openingDate: '2023.10.25',
    applicationMethod: '홈페이지 지원',
    expectedSalary: '회사 내규에 따름',
    status: 'PENDING',
    applicants: 0
  },
  {
    id: 3,
    companyName: 'Growth Startup',
    location: '서울 여의도',
    position: '마케팅 팀장 급 (퍼포먼스 마케팅)',
    requirements: '퍼포먼스 마케팅 경력 7년 이상...',
    openingDate: '2023.10.23',
    applicationMethod: '간편 지원',
    expectedSalary: '회사 내규에 따름',
    status: 'REJECTED',
    rejectedReason: '연봉 정보 누락',
    applicants: 0
  },
  {
    id: 4,
    companyName: 'Design Studio',
    location: '원격 근무',
    position: 'UX/UI 제품 디자이너',
    requirements: 'Figma 능숙자, 포트폴리오 필수...',
    openingDate: '2023.10.20',
    applicationMethod: '포트폴리오 제출',
    expectedSalary: '5,000 - 6,500 만원',
    status: 'APPROVED',
    applicants: 45
  }
];

function JobListPage() {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedJobIds, setSelectedJobIds] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortType, setSortType] = useState('최신 등록순');
  const [pageSize, setPageSize] = useState(10);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // 백엔드 API 연동
  useEffect(() => {
    const fetchJobs = async () => {
      setLoading(true);
      try {
        const backendUrl = process.env.REACT_APP_BACKEND || '';
        // API Contracts 명세: GET /api/v1/jobs 호출
        const response = await axios.get(`${backendUrl}/api/v1/jobs`, {
          params: {
            page: currentPage - 1, // Spring Data Page는 0-index
            size: pageSize
          }
        });

        if (response.data && response.data.status === 'success') {
          // 백엔드에서 반환한 페이징 응답 데이터 및 페이징 속성 동적 설정
          const pageData = response.data.data;
          setJobs(pageData.content || []);
          setTotalPages(pageData.totalPages || 1);
          setTotalElements(pageData.totalElements || 0);
        } else {
          // 응답 실패 시 목업 폴백 데이터 장착
          setJobs(FALLBACK_JOBS);
          setTotalPages(1);
          setTotalElements(FALLBACK_JOBS.length);
        }
      } catch (err) {
        console.warn("백엔드 API 호출 실패 - Stitch 디자인 데모용 폴백 데이터를 대신 로드합니다.", err);
        setJobs(FALLBACK_JOBS);
        setTotalPages(1);
        setTotalElements(FALLBACK_JOBS.length);
      } finally {
        setLoading(false);
      }
    };

    fetchJobs();
  }, [currentPage, pageSize]);

  // 다중 체크박스 선택/해제 로직
  const handleCheckboxChange = (jobId) => {
    if (selectedJobIds.includes(jobId)) {
      setSelectedJobIds(selectedJobIds.filter(id => id !== jobId));
    } else {
      setSelectedJobIds([...selectedJobIds, jobId]);
    }
  };

  // 벌크 선택 일괄 해제
  const handleClearSelection = () => {
    setSelectedJobIds([]);
  };

  // 벌크 일괄 삭제 모조 연동
  const handleBulkDelete = () => {
    alert(`${selectedJobIds.length}개의 공고가 삭제 처리되었습니다. (목업 작동)`);
    setJobs(jobs.filter(job => !selectedJobIds.includes(job.id)));
    setSelectedJobIds([]);
  };

  // 벌크 일괄 승인(상태 변경) 모조 연동
  const handleBulkApprove = () => {
    alert(`${selectedJobIds.length}개의 공고 상태가 APPROVED 로 일괄 승인되었습니다. (목업 작동)`);
    setJobs(jobs.map(job => 
      selectedJobIds.includes(job.id) ? { ...job, status: 'APPROVED', rejectedReason: undefined } : job
    ));
    setSelectedJobIds([]);
  };

  // 개별 삭제
  const handleDeleteJob = (jobId, position) => {
    if (window.confirm(`'${position}' 공고를 정말 삭제하시겠습니까?`)) {
      setJobs(jobs.filter(job => job.id !== jobId));
      setSelectedJobIds(selectedJobIds.filter(id => id !== jobId));
    }
  };

  // 개별 승인/수정 임시 모조 연동
  const handleEditJob = (position) => {
    alert(`'${position}' 공고의 정보 수정 창을 띄웁니다. (준비 중)`);
  };

  // 실시간 검색 및 정렬 필터링 처리
  const filteredJobs = jobs.filter(job => {
    const query = searchQuery.toLowerCase();
    return (
      job.position.toLowerCase().includes(query) ||
      job.companyName.toLowerCase().includes(query) ||
      (job.location && job.location.toLowerCase().includes(query))
    );
  });

  // 정렬 처리
  const sortedJobs = [...filteredJobs].sort((a, b) => {
    if (sortType === '최신 등록순') {
      return b.id - a.id;
    } else if (sortType === '마감일 임박순') {
      // 마감일 데이터가 없으므로 목업으로 ID 정렬 순서 응용
      return a.id - b.id;
    }
    return 0;
  });

  return (
    <div className="joblist-root">
      <div className="joblist-wrapper">
        
        {/* 상단 앱바 탑바 */}
        <header className="joblist-top-appbar">
          <div className="appbar-title-group">
            <span className="material-symbols-outlined appbar-logo" style={{ fontVariationSettings: "'FILL' 1" }}>
              work_history
            </span>
            <h1 className="appbar-text">CareerAdmin</h1>
          </div>
          <div className="appbar-actions">
            <button className="appbar-btn-icon" onClick={() => alert('신규 알림이 없습니다.')}>
              <span className="material-symbols-outlined">notifications</span>
            </button>
            <img 
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuCIhJYl55iKlUmCmBrFcKbg-EqLpL2NPMRh5daNthu1os_zloaZgnUxUIW6VwPjA4bbcm8MU2_EIZUt8T3NcZuEMNYfuzVfSJDDP_FJpmpDiwkpGE1eAnV28Fd2EX3OMSBDC2xlg9hKjjjEFaKpDhsgcxvQV2qKlQSZXKrN-cX82RKUFs7p_g4-PfKnXnlE1nOnViu_iizKsW-beuILg9bxqvIRxYSBMuKJLs0JT429AlDNddkr41LJV4aiFG4w2HE5cNr9M-R2mwsU"
              alt="Admin Profile"
              className="appbar-avatar"
            />
          </div>
        </header>

        {/* 1. 벌크 액션 배너 (다중 선택 활성화 시 노출) */}
        {selectedJobIds.length > 0 && (
          <div className="bulk-action-banner">
            <div className="bulk-left-group">
              <button className="bulk-close-btn" onClick={handleClearSelection} title="선택 해제">
                <span className="material-symbols-outlined">close</span>
              </button>
              <span className="bulk-count-label">{selectedJobIds.length}개 선택됨</span>
            </div>
            <div className="bulk-actions-group">
              <button className="bulk-btn bulk-btn-delete" onClick={handleBulkDelete}>
                삭제
              </button>
              <button className="bulk-btn bulk-btn-status" onClick={handleBulkApprove}>
                상태 변경 (APPROVED)
              </button>
            </div>
          </div>
        )}

        {/* 2. 헤더 타이틀 및 필터 영역 */}
        <section className="joblist-header-section">
          <div>
            <h2 class="joblist-title-title">채용 공고 관리</h2>
            <p class="joblist-title-subtitle">총 {sortedJobs.length}개의 채용 공고가 등록되어 있습니다.</p>
          </div>

          {/* 실시간 필터 및 정렬 */}
          <div className="joblist-filters-row">
            {/* 검색창 */}
            <div className="joblist-search-wrapper">
              <span className="material-symbols-outlined search-input-icon">search</span>
              <input
                type="text"
                className="search-input-field"
                placeholder="공고 검색..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            
            {/* 정렬 셀렉트 */}
            <div className="filter-select-wrapper">
              <select 
                className="filter-select-field"
                value={sortType}
                onChange={(e) => setSortType(e.target.value)}
              >
                <option value="최신 등록순">최신 등록순</option>
                <option value="마감일 임박순">마감일 임박순</option>
              </select>
              <span className="material-symbols-outlined select-expand-icon">expand_more</span>
            </div>

            {/* 표시 갯수 셀렉트 */}
            <div className="filter-select-wrapper" style={{ display: 'none' }}>
              <select 
                className="filter-select-field"
                value={pageSize}
                onChange={(e) => setPageSize(Number(e.target.value))}
              >
                <option value={10}>10개씩 보기</option>
                <option value={20}>20개씩 보기</option>
                <option value={50}>50개씩 보기</option>
              </select>
              <span className="material-symbols-outlined select-expand-icon">expand_more</span>
            </div>
          </div>
        </section>

        {/* 3. 공고 카드 Grid */}
        {loading ? (
          <div style={{ textAlign: 'center', padding: '48px', fontSize: '18px', fontWeight: '600' }}>
            채용 공고를 로딩하고 있습니다...
          </div>
        ) : sortedJobs.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '48px', color: 'var(--secondary-color)', border: '1px dashed var(--border-subtle)', borderRadius: '8px', backgroundColor: 'var(--surface-lowest)' }}>
            등록된 채용 공고가 존재하지 않습니다.
          </div>
        ) : (
          <div className="joblist-cards-grid">
            {sortedJobs.map((job) => {
              const isSelected = selectedJobIds.includes(job.id);
              return (
                <div 
                  key={job.id} 
                  className={`job-posting-card ${isSelected ? 'card-selected' : ''}`}
                >
                  {/* 체크박스 및 모바일 상태배지 */}
                  <div className="card-checkbox-area">
                    <input 
                      type="checkbox"
                      className="card-checkbox-field"
                      checked={isSelected}
                      onChange={() => handleCheckboxChange(job.id)}
                    />
                    {/* 모바일 뷰 전용 배지 */}
                    <span className={`status-pill-badge ${job.status.toLowerCase()} md:hidden`}>
                      {job.status}
                    </span>
                  </div>

                  {/* 세부 정보 영역 */}
                  <div className="card-details-area">
                    <div className="card-main-header">
                      <div>
                        <h3 className={`card-title-text ${job.status === 'REJECTED' ? 'line-through-rejected' : ''}`}>
                          {job.position}
                        </h3>
                        <p className="card-company-name">{job.companyName}</p>
                      </div>
                      {/* 데스크톱 전용 배지 */}
                      <span className={`status-pill-badge ${job.status.toLowerCase()} hidden md:inline-flex`}>
                        {job.status}
                      </span>
                    </div>

                    {/* 메타데이터 */}
                    <div className="card-metadata-row">
                      <div className="metadata-item">
                        <span className="material-symbols-outlined metadata-icon">location_on</span>
                        <span>{job.location || '근무지 협의'}</span>
                      </div>
                      <div className="metadata-item">
                        <span className="material-symbols-outlined metadata-icon">work</span>
                        <span>정규직</span>
                      </div>
                      {job.expectedSalary && (
                        <div className="metadata-item">
                          <span className="material-symbols-outlined metadata-icon">payments</span>
                          <span>{job.expectedSalary}</span>
                        </div>
                      )}
                    </div>

                    {/* 카드 하단 */}
                    <div className="card-bottom-row">
                      {job.status === 'REJECTED' && job.rejectedReason ? (
                        <div className="bottom-rejected-reason">
                          <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>info</span>
                          <span>반려 사유: {job.rejectedReason}</span>
                        </div>
                      ) : (
                        <div className="bottom-date-label">
                          등록일: {job.openingDate} · 지원자 <span className="bottom-applicants-highlight">{job.applicants || 0}</span>명
                        </div>
                      )}

                      {/* 액션 툴 (수정/삭제 호버) */}
                      <div className="card-action-tools-group">
                        <Link 
                          to={`/jobs/${job.id}/edit`}
                          className="tool-action-btn tool-action-btn-edit" 
                          title="수정"
                        >
                          <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>edit</span>
                        </Link>
                        <button 
                          className="tool-action-btn tool-action-btn-delete" 
                          title="삭제"
                          onClick={() => handleDeleteJob(job.id, job.position)}
                        >
                          <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>delete</span>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* 4. 페이징 영역 */}
        {!loading && sortedJobs.length > 0 && (
          <div className="joblist-pagination-row">
            <div className="pagination-counter-text">
              Showing {((currentPage - 1) * pageSize) + 1} to {Math.min(currentPage * pageSize, totalElements)} of {totalElements} entries
            </div>
            
            <div className="pagination-controls-group">
              <button 
                className="pagination-arrow-btn" 
                onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                disabled={currentPage === 1}
              >
                <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>chevron_left</span>
              </button>
              
              <div className="pagination-pages-group">
                {Array.from({ length: totalPages }, (_, i) => i + 1).map((pageNum) => (
                  <button 
                    key={pageNum}
                    className={`pagination-number-btn ${currentPage === pageNum ? 'active' : ''}`}
                    onClick={() => setCurrentPage(pageNum)}
                  >
                    {pageNum}
                  </button>
                ))}
              </div>

              <button 
                className="pagination-arrow-btn" 
                onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                disabled={currentPage === totalPages}
              >
                <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>chevron_right</span>
              </button>
            </div>
          </div>
        )}

        {/* 5. 플로팅 액션 버튼 (FAB - 취업 정보 추가) */}
        <Link 
          to="/jobs/new"
          className="floating-add-btn"
        >
          <span className="material-symbols-outlined floating-add-btn-icon">add</span>
          <span className="floating-add-btn-label">취업정보 추가</span>
        </Link>

      </div>
    </div>
  );
}

export default JobListPage;
