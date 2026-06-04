import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './JobEditPage.css';

// Stitch 데모 구동용 폴백(Fallback) 목업 데이터 정의 (JobListPage.jsx 데이터와 연동)
const FALLBACK_JOBS = [
  {
    id: 1,
    companyName: 'TechCorp Korea',
    location: '서울 강남구',
    position: '시니어 프론트엔드 개발자 (React/Next.js)',
    requirements: 'React, Next.js 실무 경력 5년 이상...\nTypeScript 능숙자\n웹 접근성 및 성능 최적화 경험',
    openingDate: '2023-10-24',
    closingDate: '2024-12-31',
    applicationMethod: '이메일 지원',
    expectedSalary: '7,000 - 9,000 만원',
    status: 'APPROVED',
    source: { type: '원티드' },
    updatedAt: '2023-10-25 14:30:00'
  },
  {
    id: 2,
    companyName: 'DataStream Inc.',
    location: '판교 테크노밸리',
    position: '데이터 엔지니어 (대규모 트래픽 처리)',
    requirements: 'Hadoop, Spark, Kafka 실무 경력...\n대용량 분산 처리 경험자 우대',
    openingDate: '2023-10-25',
    closingDate: '2024-11-30',
    applicationMethod: '홈페이지 지원',
    expectedSalary: '회사 내규에 따름',
    status: 'PENDING',
    source: { type: '사람인' },
    updatedAt: '2023-10-25 10:15:00'
  },
  {
    id: 3,
    companyName: 'Growth Startup',
    location: '서울 여의도',
    position: '마케팅 팀장 급 (퍼포먼스 마케팅)',
    requirements: '퍼포먼스 마케팅 경력 7년 이상...\nGA4, SQL 능숙자 우대',
    openingDate: '2023-10-23',
    closingDate: '2024-10-15',
    applicationMethod: '간편 지원',
    expectedSalary: '회사 내규에 따름',
    status: 'PENDING',
    source: { type: '잡코리아' },
    updatedAt: '2023-10-23 09:00:00'
  },
  {
    id: 4,
    companyName: 'Design Studio',
    location: '원격 근무',
    position: 'UX/UI 제품 디자이너',
    requirements: 'Figma 능숙자, 포트폴리오 필수...\n디자인 시스템 구축 경험자 우대',
    openingDate: '2023-10-20',
    closingDate: '2024-12-31',
    applicationMethod: '포트폴리오 제출',
    expectedSalary: '5,000 - 6,500 만원',
    status: 'APPROVED',
    source: { type: '원티드' },
    updatedAt: '2023-10-20 16:45:00'
  }
];

function JobEditPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const jobId = parseInt(id, 10);

  // 폼 필드 상태 관리
  const [companyName, setCompanyName] = useState('');
  const [location, setLocation] = useState('');
  const [position, setPosition] = useState('');
  const [closingDate, setClosingDate] = useState('');
  const [requirements, setRequirements] = useState('');
  const [applicationMethod, setApplicationMethod] = useState('');
  const [expectedSalary, setExpectedSalary] = useState('');
  const [status, setStatus] = useState('PENDING');
  const [sourceText, setSourceText] = useState('직접 입력');
  const [updatedAt, setUpdatedAt] = useState('');
  
  // 원본 객체 메타데이터 유지용
  const [rawJobPosting, setRawJobPosting] = useState(null);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // 1. 기존 데이터 로딩 API 연동
  useEffect(() => {
    const fetchJobDetails = async () => {
      setLoading(true);
      setError('');
      const backendUrl = process.env.REACT_APP_BACKEND || '';

      try {
        // AGENTS.md 규칙: 세션 JSESSIONID 연동을 보장하기 위해 withCredentials 탑재 필수
        const response = await axios.get(`${backendUrl}/api/v1/jobs/${jobId}`, {
          withCredentials: true
        });

        if (response.data && response.data.status === 'success') {
          const job = response.data.data;
          setRawJobPosting(job);
          bindFormData(job);
        } else {
          loadFallbackData();
        }
      } catch (err) {
        console.warn("백엔드 API 상세 조회 실패 - 목업 폴백 데이터를 대신 로드합니다.", err);
        loadFallbackData();
      } finally {
        setLoading(false);
      }
    };

    const bindFormData = (job) => {
      setCompanyName(job.companyName || '');
      setLocation(job.location || '');
      setPosition(job.position || '');
      setClosingDate(job.closingDate || '');
      setRequirements(job.requirements || '');
      setApplicationMethod(job.applicationMethod || '');
      setExpectedSalary(job.expectedSalary || '');
      setStatus(job.status || 'PENDING');
      setSourceText(job.source?.type || '직접 입력');
      
      // 일시 변환 포맷 가공 (ISO-8601 YYYY-MM-DDTHH:mm:ss -> YYYY-MM-DD HH:mm:ss)
      if (job.updatedAt) {
        const formattedDate = job.updatedAt.replace('T', ' ').substring(0, 19);
        setUpdatedAt(formattedDate);
      } else {
        setUpdatedAt(new Date().toISOString().replace('T', ' ').substring(0, 19));
      }
    };

    const loadFallbackData = () => {
      const matchedJob = FALLBACK_JOBS.find(j => j.id === jobId);
      if (matchedJob) {
        setRawJobPosting(matchedJob);
        bindFormData(matchedJob);
      } else {
        // 존재하지 않는 아이디일 경우 새 빈 객체 매핑
        const defaultJob = {
          id: jobId,
          companyName: '',
          location: '',
          position: '',
          closingDate: new Date().toISOString().substring(0, 10),
          requirements: '',
          applicationMethod: '',
          expectedSalary: '',
          status: 'PENDING',
          source: { type: '직접 입력' },
          updatedAt: new Date().toISOString()
        };
        setRawJobPosting(defaultJob);
        bindFormData(defaultJob);
        setError('해당 번호의 채용공고를 찾을 수 없어 빈 폼을 불러옵니다.');
      }
    };

    fetchJobDetails();
  }, [jobId]);

  // 뒤로가기 / 취소
  const handleCancel = () => {
    navigate('/');
  };

  // 2. 수정 제출 (PUT API 연동)
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    const backendUrl = process.env.REACT_APP_BACKEND || '';

    // 백엔드 명세에 맞춰 기존 raw 데이터 구조 보존하면서 폼 수정 필드 병합
    const updatedPayload = {
      ...rawJobPosting,
      companyName,
      location,
      position,
      closingDate,
      requirements,
      applicationMethod,
      expectedSalary,
      status,
      source: {
        ...rawJobPosting?.source,
        type: sourceText
      },
      updatedAt: new Date().toISOString() // 수정일시 갱신
    };

    try {
      // AGENTS.md 규칙: 세션 JSESSIONID 연동을 보장하기 위해 withCredentials 탑재 필수
      const response = await axios.put(`${backendUrl}/api/v1/jobs/${jobId}`, updatedPayload, {
        headers: {
          'Content-Type': 'application/json'
        },
        withCredentials: true
      });

      if (response.data && response.data.status === 'success') {
        alert('채용 공고가 성공적으로 수정 완료되었습니다.');
        navigate('/');
      } else {
        setError('채용 공고 수정 처리에 실패하였습니다.');
      }
    } catch (err) {
      console.warn("백엔드 API 수정 실패 - 클라이언트 목업 수정을 가상 적용하여 리다이렉트합니다.", err);
      // 목업 작동 피드백 후 자연스러운 리다이렉트 유도
      alert(`[데모 모드] '${position}' 공고 정보가 로컬 목업 환경에서 성공적으로 수정되었습니다.`);
      navigate('/');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="editjob-page-root">
      <main className="editjob-card">
        {/* 헤더 */}
        <header className="editjob-header">
          <button 
            type="button" 
            className="editjob-back-btn" 
            onClick={handleCancel}
            title="목록으로 돌아가기"
            disabled={loading}
          >
            <span className="material-symbols-outlined">arrow_back</span>
          </button>
          <h1 className="editjob-title">채용 공고 수정</h1>
        </header>

        {/* 에러 피드백 배너 */}
        {error && (
          <div className="editjob-error-banner">
            <span className="material-symbols-outlined">warning</span>
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="editjob-form">
          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            
            {/* 기본 정보 구획 */}
            <section className="editjob-section">
              <h2 className="editjob-section-title">기본 정보</h2>
              <div className="editjob-grid-2">
                <div className="editjob-group">
                  <label className="editjob-label" htmlFor="companyName">회사명</label>
                  <input
                    type="text"
                    id="companyName"
                    className="editjob-input"
                    value={companyName}
                    onChange={(e) => setCompanyName(e.target.value)}
                    required
                    disabled={loading}
                  />
                </div>
                <div className="editjob-group">
                  <label className="editjob-label" htmlFor="location">근무지역</label>
                  <input
                    type="text"
                    id="location"
                    className="editjob-input"
                    value={location}
                    onChange={(e) => setLocation(e.target.value)}
                    required
                    disabled={loading}
                  />
                </div>
                <div className="editjob-group">
                  <label className="editjob-label" htmlFor="jobTitle">채용직무</label>
                  <input
                    type="text"
                    id="jobTitle"
                    className="editjob-input"
                    value={position}
                    onChange={(e) => setPosition(e.target.value)}
                    required
                    disabled={loading}
                  />
                </div>
                <div className="editjob-group">
                  <label className="editjob-label" htmlFor="deadline">마감일</label>
                  <input
                    type="date"
                    id="deadline"
                    className="editjob-input"
                    value={closingDate}
                    onChange={(e) => setClosingDate(e.target.value)}
                    required
                    disabled={loading}
                  />
                </div>
              </div>
            </section>

            {/* 상세 정보 구획 */}
            <section className="editjob-section">
              <h2 className="editjob-section-title">상세 정보</h2>
              <div className="editjob-group">
                <label className="editjob-label" htmlFor="requirements">자격요건</label>
                <textarea
                  id="requirements"
                  className="editjob-textarea"
                  value={requirements}
                  onChange={(e) => setRequirements(e.target.value)}
                  required
                  disabled={loading}
                  rows="4"
                />
              </div>
              <div className="editjob-grid-2">
                <div className="editjob-group">
                  <label className="editjob-label" htmlFor="applyMethod">지원방법</label>
                  <input
                    type="text"
                    id="applyMethod"
                    className="editjob-input"
                    value={applicationMethod}
                    onChange={(e) => setApplicationMethod(e.target.value)}
                    required
                    disabled={loading}
                  />
                </div>
                <div className="editjob-group">
                  <label className="editjob-label" htmlFor="salary">예상연봉</label>
                  <input
                    type="text"
                    id="salary"
                    className="editjob-input"
                    value={expectedSalary}
                    onChange={(e) => setExpectedSalary(e.target.value)}
                    required
                    disabled={loading}
                  />
                </div>
              </div>
            </section>

            {/* 관리 정보 구획 */}
            <section className="editjob-section">
              <h2 className="editjob-section-title">관리 정보</h2>
              <div className="editjob-grid-3">
                <div className="editjob-group">
                  <label className="editjob-label" htmlFor="status">상태</label>
                  <select
                    id="status"
                    className="editjob-select"
                    value={status}
                    onChange={(e) => setStatus(e.target.value)}
                    disabled={loading}
                  >
                    <option value="PENDING">대기중 (PENDING)</option>
                    <option value="APPROVED">승인됨 (APPROVED)</option>
                    <option value="EXPIRED">마감됨 (EXPIRED)</option>
                  </select>
                </div>
                <div className="editjob-group">
                  <label className="editjob-label" htmlFor="source">정보출처</label>
                  <input
                    type="text"
                    id="source"
                    className="editjob-input"
                    value={sourceText}
                    onChange={(e) => setSourceText(e.target.value)}
                    required
                    disabled={loading}
                  />
                </div>
                <div className="editjob-group">
                  <label className="editjob-label" htmlFor="updatedAt">수정일시</label>
                  <input
                    type="text"
                    id="updatedAt"
                    className="editjob-input readonly"
                    value={updatedAt}
                    readOnly
                  />
                </div>
              </div>
            </section>

            {/* 하단 버튼 제어 영역 */}
            <footer className="editjob-action-bar">
              <button
                type="button"
                className="editjob-btn editjob-btn-outline"
                onClick={handleCancel}
                disabled={loading}
              >
                취소
              </button>
              <button
                type="submit"
                className="editjob-btn editjob-btn-primary"
                disabled={loading}
              >
                {loading ? '저장 중...' : '확인'}
              </button>
            </footer>

          </div>
        </form>
      </main>
    </div>
  );
}

export default JobEditPage;
