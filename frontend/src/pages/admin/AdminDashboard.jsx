import React, { useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './AdminDashboard.css';

function AdminDashboard() {
  const navigate = useNavigate();

  // 대시보드 접근 시 로그인 여부 간단 체크 (선택 사항)
  useEffect(() => {
    const token = localStorage.getItem('authToken');
    if (!token) {
      console.warn("로그인 토큰이 없어 로그인 페이지로 리다이렉트합니다.");
      // 강제 리다이렉션을 켜둘 수 있으나, 유저 편의를 위해 일단 보류하거나 가벼운 알림으로 대체 가능
      // 여기서는 데모 목적을 위해 바로 튕기지 않고 확인만 처리합니다.
    }
  }, [navigate]);

  // 방문자 통계 목업 데이터
  const visitorData = [
    { day: '월', value: '300명', height: '30%' },
    { day: '화', value: '500명', height: '50%' },
    { day: '수', value: '400명', height: '40%' },
    { day: '목', value: '800명', height: '80%', active: true }, // 목요일이 가장 높은 주 피크
    { day: '금', value: '600명', height: '60%' },
    { day: '토', value: '700명', height: '70%' },
    { day: '일', value: '450명', height: '45%' },
  ];

  return (
    <div className="dashboard-root">
      <div className="dashboard-wrapper">
        
        {/* 상단 앱바 탑바 (모바일 뷰에서 주로 브랜딩용으로 보임) */}
        <header className="dashboard-top-appbar">
          <div className="appbar-title-group">
            <span className="material-symbols-outlined appbar-logo" style={{ fontVariationSettings: "'FILL' 1" }}>
              work_history
            </span>
            <h1 className="appbar-text">CareerAdmin</h1>
          </div>
        </header>

        {/* 대시보드 타이틀 헤더 */}
        <section className="dashboard-header-section">
          <h2 className="dashboard-main-title">대시보드 개요</h2>
          <p className="dashboard-main-subtitle">시스템의 주요 지표와 최근 활동을 확인하세요.</p>
        </section>

        {/* Bento Grid 구조 */}
        <div className="bento-grid-container">
          
          {/* Stat Card 1: Users */}
          <div className="bento-stat-card">
            {/* 우상단 배경 아이콘 */}
            <span className="material-symbols-outlined bento-stat-bg-icon" style={{ fontSize: '64px' }}>
              group
            </span>
            <div>
              <h3 className="stat-label">등록된 사용자 수</h3>
              <div className="stat-value">1,248</div>
            </div>
            <div className="stat-trend-footer">
              <span className="trend-badge">
                <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>
                  trending_up
                </span>
                <span>+12%</span>
              </span>
              <span className="trend-compare-text">지난 달 대비</span>
            </div>
          </div>

          {/* Stat Card 2: Jobs */}
          <div className="bento-stat-card">
            {/* 우상단 배경 아이콘 */}
            <span className="material-symbols-outlined bento-stat-bg-icon" style={{ fontSize: '64px' }}>
              work
            </span>
            <div>
              <h3 className="stat-label">등록된 취업정보의 수</h3>
              <div className="stat-value">342</div>
            </div>
            <div className="stat-trend-footer">
              <span className="trend-badge">
                <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>
                  trending_up
                </span>
                <span>+5%</span>
              </span>
              <span className="trend-compare-text">이번 주 신규</span>
            </div>
          </div>

          {/* Chart Card: Visitor Stats */}
          <div className="bento-chart-card">
            <div className="chart-header">
              <h3 className="chart-title">방문자 통계</h3>
              <button className="chart-filter-btn" onClick={() => alert('기간 필터 기능 준비 중')}>
                <span>이번 주</span>
                <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>
                  expand_more
                </span>
              </button>
            </div>

            {/* 차트 시각화 영역 */}
            <div className="chart-visual-wrapper">
              
              {/* Y축 라벨 */}
              <div className="chart-y-axis-labels">
                <span>1k</span>
                <span>500</span>
                <span>0</span>
              </div>

              {/* 가로 점선 격자 */}
              <div className="chart-grid-lines">
                <div className="grid-line"></div>
                <div className="grid-line"></div>
                <div className="grid-line"></div>
              </div>

              {/* 차트 바 목록 */}
              <div className="chart-bars-container">
                {visitorData.map((data, idx) => (
                  <div 
                    key={idx}
                    className={`chart-bar-item ${data.active ? 'active' : ''}`}
                    style={{ height: data.height }}
                    data-value={data.value}
                  ></div>
                ))}
              </div>

              {/* X축 라벨 */}
              <div className="chart-x-axis-labels">
                {visitorData.map((data, idx) => (
                  <span key={idx}>{data.day}</span>
                ))}
              </div>
            </div>
          </div>

          {/* Quick Link 1: 취업정보목록 */}
          <Link to="/" className="bento-quick-link">
            <div className="quick-link-left">
              <div className="quick-link-icon-container">
                <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>
                  work
                </span>
              </div>
              <div>
                <h3 className="quick-link-title">취업정보목록</h3>
                <p className="quick-link-subtitle">공고 관리 및 승인</p>
              </div>
            </div>
            <span className="material-symbols-outlined quick-link-arrow">
              arrow_forward
            </span>
          </Link>

          {/* Quick Link 2: 사용자목록 */}
          <a 
            href="#" 
            className="bento-quick-link"
            onClick={(e) => {
              e.preventDefault();
              alert('사용자 목록 관리 기능은 후속 스프린트에서 구현 예정입니다.');
            }}
          >
            <div className="quick-link-left">
              <div className="quick-link-icon-container">
                <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>
                  group
                </span>
              </div>
              <div>
                <h3 className="quick-link-title">사용자목록</h3>
                <p className="quick-link-subtitle">구직자 및 기업회원 관리</p>
              </div>
            </div>
            <span className="material-symbols-outlined quick-link-arrow">
              arrow_forward
            </span>
          </a>

        </div>
      </div>
    </div>
  );
}

export default AdminDashboard;
