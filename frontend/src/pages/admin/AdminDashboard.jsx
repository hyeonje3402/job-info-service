import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../services/api';
import './AdminDashboard.css';

function formatTimeAgo(dateString) {
  if (!dateString) return '';
  const now = new Date();
  const date = new Date(dateString);
  const diffMs = now - date;
  
  if (diffMs < 0) return '방금 전';
  
  const diffMins = Math.floor(diffMs / (1000 * 60));
  if (diffMins < 1) return '방금 전';
  if (diffMins < 60) return `${diffMins}분 전`;
  
  const diffHours = Math.floor(diffMins / 60);
  if (diffHours < 24) return `${diffHours}시간 전`;
  
  const diffDays = Math.floor(diffHours / 24);
  return `${diffDays}일 전`;
}

function AdminDashboard() {
  const [loading, setLoading] = useState(true);
  const [userCount, setUserCount] = useState(0);
  const [totalJobs, setTotalJobs] = useState(0);
  const [approvedJobs, setApprovedJobs] = useState(0);
  const [pendingJobs, setPendingJobs] = useState(0);
  const [expiredJobs, setExpiredJobs] = useState(0);
  const [apiStatsData, setApiStatsData] = useState([]);
  const [recentActivities, setRecentActivities] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [usersRes, jobsRes, pendingJobsRes, approvedJobsRes, expiredJobsRes, apiLogsRes] = await Promise.all([
          api.get('/api/v1/users'),
          api.get('/api/v1/jobs'),
          api.get('/api/v1/jobs', { params: { status: 'PENDING', size: 1 } }),
          api.get('/api/v1/jobs', { params: { status: 'APPROVED', size: 1 } }),
          api.get('/api/v1/jobs', { params: { status: 'EXPIRED', size: 1 } }),
          api.get('/api/v1/api-logs', { params: { size: 2000 } })
        ]);

        if (usersRes.data && usersRes.data.status === 'success') {
          setUserCount((usersRes.data.data || []).length);
        }

        if (jobsRes.data && jobsRes.data.status === 'success') {
          setTotalJobs(jobsRes.data.data?.totalElements || 0);
        }

        if (pendingJobsRes.data && pendingJobsRes.data.status === 'success') {
          setPendingJobs(pendingJobsRes.data.data?.totalElements || 0);
        }

        if (approvedJobsRes.data && approvedJobsRes.data.status === 'success') {
          setApprovedJobs(approvedJobsRes.data.data?.totalElements || 0);
        }

        if (expiredJobsRes.data && expiredJobsRes.data.status === 'success') {
          setExpiredJobs(expiredJobsRes.data.data?.totalElements || 0);
        }

        if (apiLogsRes.data && apiLogsRes.data.status === 'success') {
          const logs = apiLogsRes.data.data?.content || [];
          
          const last7Days = [];
          for (let i = 6; i >= 0; i--) {
            const d = new Date();
            d.setDate(d.getDate() - i);
            const yyyy = d.getFullYear();
            const mm = String(d.getMonth() + 1).padStart(2, '0');
            const dd = String(d.getDate()).padStart(2, '0');
            const dateStr = `${yyyy}-${mm}-${dd}`;
            
            const dayNames = ['일', '월', '화', '수', '목', '금', '토'];
            const dayName = dayNames[d.getDay()];
            
            last7Days.push({
              dateStr,
              dayLabel: `${mm}/${dd}(${dayName})`,
              count: 0
            });
          }
          
          logs.forEach(log => {
            if (!log.createdAt) return;
            const logDate = log.createdAt.split('T')[0];
            const dayObj = last7Days.find(d => d.dateStr === logDate);
            if (dayObj) {
              dayObj.count += 1;
            }
          });
          
          const maxCount = Math.max(...last7Days.map(d => d.count), 1);
          const chartData = last7Days.map((d, idx) => ({
            day: d.dayLabel,
            value: d.count,
            height: `${(d.count / maxCount) * 80 + 10}%`,
            active: idx === 6
          }));
          
          setApiStatsData(chartData);

          const activities = [];
          for (const log of logs) {
            if (activities.length >= 5) break;

            const uri = log.requestUri || '';
            const method = log.httpMethod || '';
            const username = log.username || '비회원';
            const timeAgo = formatTimeAgo(log.createdAt);

            let activity = null;

            if (uri.startsWith('/api/v1/auth/register') && method === 'POST') {
              activity = {
                type: 'register',
                icon: 'person_add',
                text: '신규 사용자 회원가입이 완료되었습니다.',
                time: timeAgo
              };
            } else if (uri === '/api/v1/jobs' && method === 'POST') {
              activity = {
                type: 'job-add',
                icon: 'post_add',
                text: `${username}님이 새 채용공고를 등록했습니다.`,
                time: timeAgo
              };
            } else if (uri.startsWith('/api/v1/jobs/') && method === 'PUT') {
              activity = {
                type: 'job-edit',
                icon: 'edit',
                text: `${username}님이 채용공고를 수정/승인했습니다.`,
                time: timeAgo
              };
            } else if (uri.startsWith('/api/v1/jobs/') && method === 'DELETE') {
              activity = {
                type: 'job-delete',
                icon: 'delete',
                text: `${username}님이 채용공고를 삭제했습니다.`,
                time: timeAgo
              };
            } else if (uri.startsWith('/api/v1/sources/url') && method === 'POST') {
              activity = {
                type: 'analysis',
                icon: 'link',
                text: `${username}님이 URL 채용정보 파싱을 요청했습니다.`,
                time: timeAgo
              };
            } else if (uri.startsWith('/api/v1/sources/pdf') && method === 'POST') {
              activity = {
                type: 'analysis',
                icon: 'picture_as_pdf',
                text: `${username}님이 PDF 채용정보 파싱을 요청했습니다.`,
                time: timeAgo
              };
            } else if (uri.startsWith('/api/v1/auth/login') && method === 'POST') {
              activity = {
                type: 'login',
                icon: 'login',
                text: `${username}님이 로그인하였습니다.`,
                time: timeAgo
              };
            }

            if (activity) {
              activities.push(activity);
            }
          }
          setRecentActivities(activities);
        }
      } catch (err) {
        console.error("대시보드 실데이터 로드 중 실패", err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  return (
    <div className="dashboard-root">
      <div className="dashboard-wrapper">
        
        <header className="dashboard-top-appbar">
          <div className="appbar-title-group">
            <span className="material-symbols-outlined appbar-logo" style={{ fontVariationSettings: "'FILL' 1" }}>
              work_history
            </span>
            <h1 className="appbar-text">CareerAdmin</h1>
          </div>
        </header>

        <section className="dashboard-header-section">
          <h2 className="dashboard-main-title">대시보드 개요</h2>
          <p className="dashboard-main-subtitle">시스템의 주요 지표와 최근 활동을 확인하세요.</p>
        </section>

        <div className="bento-grid-container">
          
          {/* Stat Card 1: Users */}
          <div className="bento-stat-card">
            <span className="material-symbols-outlined bento-stat-bg-icon" style={{ fontSize: '64px' }}>
              group
            </span>
            <div>
              <h3 className="stat-label">등록된 사용자 수</h3>
              <div className="stat-value">{loading ? '...' : `${userCount}명`}</div>
            </div>
            <div className="stat-trend-footer">
              <span className="trend-badge">
                <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>
                  trending_up
                </span>
                <span>실시간</span>
              </span>
              <span className="trend-compare-text">인사 데이터베이스 연동</span>
            </div>
          </div>

          {/* Stat Card 2: Jobs */}
          <div className="bento-stat-card jobs-status-card">
            <span className="material-symbols-outlined bento-stat-bg-icon" style={{ fontSize: '64px' }}>
              work
            </span>
            <div className="jobs-status-header">
              <h3 className="stat-label">채용공고 등록 현황</h3>
              <div className="stat-value">{loading ? '...' : `${totalJobs}개`}</div>
            </div>
            <div className="jobs-status-list">
              <div className="status-item approved">
                <span className="status-pill-badge approved-badge">승인됨</span>
                <span className="status-count">{loading ? '...' : `${approvedJobs}개`}</span>
              </div>
              <div className="status-item pending">
                <span className="status-pill-badge pending-badge">대기중</span>
                <span className="status-count">{loading ? '...' : `${pendingJobs}개`}</span>
              </div>
              <div className="status-item expired">
                <span className="status-pill-badge expired-badge">만료됨</span>
                <span className="status-count">{loading ? '...' : `${expiredJobs}개`}</span>
              </div>
            </div>
          </div>

          {/* Card 3: Recent Activities */}
          <div className="bento-activity-card">
            <div className="activity-header">
              <h3 className="activity-title">최근 활동 목록</h3>
              <span className="material-symbols-outlined" style={{ color: 'var(--secondary-color)', fontSize: '20px' }}>
                notifications_active
              </span>
            </div>
            <div className="activity-list">
              {loading ? (
                <div style={{ color: 'var(--secondary-color)', fontSize: '13px' }}>로딩 중...</div>
              ) : recentActivities.length === 0 ? (
                <div style={{ color: 'var(--secondary-color)', fontSize: '13px', padding: '16px 0' }}>최근 활동이 없습니다.</div>
              ) : (
                recentActivities.map((act, idx) => (
                  <div key={idx} className="activity-item">
                    <div className={`activity-icon-container ${act.type}`}>
                      <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                        {act.icon}
                      </span>
                    </div>
                    <div className="activity-content-wrapper">
                      <span className="activity-text">{act.text}</span>
                      <span className="activity-time">{act.time}</span>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Chart Card */}
          <div className="bento-chart-card">
            <div className="chart-header">
              <h3 className="chart-title">API 호출 통계</h3>
              <button className="chart-filter-btn" onClick={() => alert('기간 필터 기능 준비 중')}>
                <span>지난 7일</span>
                <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>
                  expand_more
                </span>
              </button>
            </div>

            <div className="chart-visual-wrapper">
              
              <div className="chart-y-axis-labels">
                <span>{loading ? '...' : Math.max(...apiStatsData.map(d => d.value), 10)}</span>
                <span>{loading ? '...' : Math.floor(Math.max(...apiStatsData.map(d => d.value), 10) / 2)}</span>
                <span>0</span>
              </div>

              <div className="chart-grid-lines">
                <div className="grid-line"></div>
                <div className="grid-line"></div>
                <div className="grid-line"></div>
              </div>

              <div className="chart-bars-container">
                {apiStatsData.map((data, idx) => (
                  <div 
                    key={idx}
                    className={`chart-bar-item ${data.active ? 'active' : ''}`}
                    style={{ height: data.height }}
                    data-value={data.value}
                  ></div>
                ))}
              </div>

              <div className="chart-x-axis-labels">
                {apiStatsData.map((data, idx) => (
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
          <Link to="/users-management" className="bento-quick-link">
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
          </Link>

        </div>
      </div>
    </div>
  );
}

export default AdminDashboard;

