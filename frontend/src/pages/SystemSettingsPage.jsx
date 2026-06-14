import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './SystemSettingsPage.css';

const DEFAULT_SETTINGS = {
  systemTitle: 'CareerAdmin 채용 관리 시스템',
  pageSize: 10,
  defaultLang: 'ko',
  aiModel: 'yc-gemma-4-26b-a4b',
  aiTimeout: 120,
  promptVersion: 'v1.2.0 (최신)',
  crawlerEnabled: true,
  crawlerDelay: 1000,
  debugMode: false
};

function SystemSettingsPage() {
  const [activeTab, setActiveTab] = useState('general');
  const [settings, setSettings] = useState(DEFAULT_SETTINGS);
  const [isSaving, setIsSaving] = useState(false);
  const [isResetting, setIsResetting] = useState(false);
  const [toastMessage, setToastMessage] = useState('');
  const [showToast, setShowToast] = useState(false);

  // 로컬 스토리지에서 설정 불러오기
  useEffect(() => {
    const saved = localStorage.getItem('hiresync_system_settings');
    if (saved) {
      try {
        setSettings({ ...DEFAULT_SETTINGS, ...JSON.parse(saved) });
      } catch (e) {
        console.error('Failed to parse saved settings', e);
      }
    }
  }, []);

  const triggerToast = (msg) => {
    setToastMessage(msg);
    setShowToast(true);
    setTimeout(() => setShowToast(false), 3000);
  };

  const handleInputChange = (key, value) => {
    setSettings(prev => ({
      ...prev,
      [key]: value
    }));
  };

  const handleSaveSettings = () => {
    setIsSaving(true);
    setTimeout(() => {
      localStorage.setItem('hiresync_system_settings', JSON.stringify(settings));
      setIsSaving(false);
      triggerToast('설정이 성공적으로 저장되었습니다.');
    }, 800000 / 1000000); // 800ms 지연으로 부드러운 UX 유도
  };

  // H2 DB 초기화 (ID 1번 SK키파운드리를 제외한 모든 채용공고 삭제)
  const handleResetDatabase = async () => {
    if (!window.confirm('정말로 H2 데이터베이스를 초기화하시겠습니까? \n(기본 데이터인 SK키파운드리를 제외한 사용자가 업로드한 모든 채용 공고가 삭제됩니다.)')) {
      return;
    }

    setIsResetting(true);
    try {
      const backendUrl = process.env.REACT_APP_BACKEND || '';
      // 1. 모든 공고 목록 가져오기 (최대 50개)
      const response = await axios.get(`${backendUrl}/api/v1/jobs?size=50`);
      
      if (response.data && response.data.status === 'success') {
        const jobs = response.data.data.content || [];
        // ID 1 (SK키파운드리)을 제외한 나머지 삭제 대상 필터링
        const targetsToDelete = jobs.filter(job => job.id !== 1);

        if (targetsToDelete.length === 0) {
          triggerToast('초기화할 추가 채용 공고가 없습니다.');
          setIsResetting(false);
          return;
        }

        // 2. 순차적으로 삭제 진행
        for (const job of targetsToDelete) {
          await axios.delete(`${backendUrl}/api/v1/jobs/${job.id}`);
        }
        
        triggerToast(`데이터베이스 리셋 완료 (${targetsToDelete.length}개 공고 삭제됨)`);
      } else {
        throw new Error('공고 목록 조회 실패');
      }
    } catch (err) {
      console.error('Database reset error:', err);
      alert('데이터베이스 리셋 중 오류가 발생했습니다. (권한 또는 네트워크 연결을 확인해 주세요)');
    } finally {
      setIsResetting(false);
    }
  };

  return (
    <div className="settings-root">
      <div className="settings-wrapper">
        {/* 상단 앱바 */}
        <header className="settings-top-appbar">
          <div className="appbar-title-group">
            <span className="material-symbols-outlined appbar-logo" style={{ fontVariationSettings: "'FILL' 1" }}>
              work_history
            </span>
            <h1 className="appbar-text">CareerAdmin</h1>
          </div>
        </header>

        {/* 타이틀 헤더 */}
        <section className="settings-header-section">
          <h2 className="settings-main-title">시스템 설정</h2>
          <p className="settings-main-subtitle">어플리케이션의 핵심 동작 환경 및 AI 파싱 옵션을 관리합니다.</p>
        </section>

        {/* 메인 레이아웃 (좌측 탭 메뉴 / 우측 설정 폼) */}
        <div className="settings-layout-container">
          
          {/* 좌측 탭 메뉴 */}
          <aside className="settings-tabs-sidebar">
            <button 
              className={`tab-btn-item ${activeTab === 'general' ? 'active' : ''}`}
              onClick={() => setActiveTab('general')}
            >
              <span className="material-symbols-outlined tab-icon">tune</span>
              <span className="tab-label">일반 설정</span>
            </button>
            <button 
              className={`tab-btn-item ${activeTab === 'ai' ? 'active' : ''}`}
              onClick={() => setActiveTab('ai')}
            >
              <span className="material-symbols-outlined tab-icon">psychology</span>
              <span className="tab-label">AI 파싱 엔진</span>
            </button>
            <button 
              className={`tab-btn-item ${activeTab === 'crawler' ? 'active' : ''}`}
              onClick={() => setActiveTab('crawler')}
            >
              <span className="material-symbols-outlined tab-icon">manage_search</span>
              <span className="tab-label">수집 및 크롤링</span>
            </button>
            <button 
              className={`tab-btn-item ${activeTab === 'system' ? 'active' : ''}`}
              onClick={() => setActiveTab('system')}
            >
              <span className="material-symbols-outlined tab-icon">database</span>
              <span className="tab-label">시스템 진단 & 리셋</span>
            </button>
          </aside>

          {/* 우측 설정 폼 영역 */}
          <main className="settings-form-panel">
            <div className="form-panel-content">
              
              {/* TAB 1: 일반 설정 */}
              {activeTab === 'general' && (
                <div className="tab-content-pane">
                  <h3 className="pane-title">일반 시스템 설정</h3>
                  
                  <div className="form-input-group">
                    <label className="input-field-label">시스템 기본 타이틀</label>
                    <input 
                      type="text" 
                      className="form-input-text"
                      value={settings.systemTitle}
                      onChange={(e) => handleInputChange('systemTitle', e.target.value)}
                    />
                    <p className="input-helper-text">브라우저 제목 탭 및 최상단 앱바에 노출되는 기본 이름입니다.</p>
                  </div>

                  <div className="form-input-group">
                    <label className="input-field-label">기본 페이지 크기 (채용공고 목록 갯수)</label>
                    <select 
                      className="form-input-select"
                      value={settings.pageSize}
                      onChange={(e) => handleInputChange('pageSize', Number(e.target.value))}
                    >
                      <option value={10}>10개씩 보기</option>
                      <option value={20}>20개씩 보기</option>
                      <option value={50}>50개씩 보기</option>
                    </select>
                    <p className="input-helper-text">채용 공고 목록 화면에서 한 번에 렌더링할 목록의 기본 단위입니다.</p>
                  </div>

                  <div className="form-input-group">
                    <label className="input-field-label">기본 표시 다국어 언어</label>
                    <select 
                      className="form-input-select"
                      value={settings.defaultLang}
                      disabled
                    >
                      <option value="ko">한국어 (Korean)</option>
                      <option value="en">영어 (English)</option>
                    </select>
                    <p className="input-helper-text">현재 시스템은 한국어만 지원하며, 변경할 수 없습니다.</p>
                  </div>
                </div>
              )}

              {/* TAB 2: AI 파싱 엔진 */}
              {activeTab === 'ai' && (
                <div className="tab-content-pane">
                  <h3 className="pane-title">AI 정보 추출 엔진 환경 설정</h3>
                  
                  <div className="form-input-group">
                    <label className="input-field-label">사용할 LLM 모델 버전</label>
                    <select 
                      className="form-input-select"
                      value={settings.aiModel}
                      onChange={(e) => handleInputChange('aiModel', e.target.value)}
                    >
                      <option value="yc-gemma-4-26b-a4b">Gemma 26B (yc-gemma-4-26b-a4b)</option>
                      <option value="gpt-4o">GPT-4o (OpenAI Cloud Gateway)</option>
                      <option value="gpt-3.5-turbo">GPT-3.5-Turbo (경량형)</option>
                    </select>
                    <p className="input-helper-text">PDF나 URL 분석 시 구문 추출에 사용할 인공지능 기초 모델을 결정합니다.</p>
                  </div>

                  <div className="form-input-group">
                    <label className="input-field-label">AI 응답 제한 시간 (타임아웃 - 초 단위)</label>
                    <input 
                      type="number" 
                      className="form-input-text"
                      min="10"
                      max="300"
                      value={settings.aiTimeout}
                      onChange={(e) => handleInputChange('aiTimeout', Number(e.target.value))}
                    />
                    <p className="input-helper-text">LLM 게이트웨이 호출 시 최대 응답 대기 시간입니다. (권장: 120초)</p>
                  </div>

                  <div className="form-input-group">
                    <label className="input-field-label">정보 추출 프롬프트 템플릿 버전</label>
                    <input 
                      type="text" 
                      className="form-input-text"
                      value={settings.promptVersion}
                      disabled
                    />
                    <p className="input-helper-text">백엔드 소스에 내장된 고정 프롬프트 리소스의 버전입니다.</p>
                  </div>
                </div>
              )}

              {/* TAB 3: 수집 및 크롤링 */}
              {activeTab === 'crawler' && (
                <div className="tab-content-pane">
                  <h3 className="pane-title">채용 사이트 정보 수집기 설정</h3>
                  
                  <div className="form-input-group row-flex">
                    <div>
                      <label className="input-field-label">사람인(Saramin) 전용 크롤러 활성화</label>
                      <p className="input-helper-text" style={{ marginTop: '4px' }}>사람인 도메인 URL 수집 시 정밀 DOM 파싱 엔진을 기본 구동합니다.</p>
                    </div>
                    <label className="toggle-switch-container">
                      <input 
                        type="checkbox"
                        checked={settings.crawlerEnabled}
                        onChange={(e) => handleInputChange('crawlerEnabled', e.target.checked)}
                      />
                      <span className="toggle-switch-slider"></span>
                    </label>
                  </div>

                  <div className="form-input-group">
                    <label className="input-field-label">수집 요청 간 지연 대기 시간 (밀리초)</label>
                    <input 
                      type="number" 
                      className="form-input-text"
                      step="500"
                      min="0"
                      value={settings.crawlerDelay}
                      onChange={(e) => handleInputChange('crawlerDelay', Number(e.target.value))}
                    />
                    <p className="input-helper-text">웹 크롤링 시 대상 서버의 트래픽 과부하 및 차단을 방지하기 위한 인위적인 대기 시간입니다.</p>
                  </div>

                  <div className="form-input-group row-flex">
                    <div>
                      <label className="input-field-label">수집 및 AI 파싱 디버그 로그 출력</label>
                      <p className="input-helper-text" style={{ marginTop: '4px' }}>브라우저 및 서버 콘솔창에 상세 로직 흐름 로그를 활성화합니다.</p>
                    </div>
                    <label className="toggle-switch-container">
                      <input 
                        type="checkbox"
                        checked={settings.debugMode}
                        onChange={(e) => handleInputChange('debugMode', e.target.checked)}
                      />
                      <span className="toggle-switch-slider"></span>
                    </label>
                  </div>
                </div>
              )}

              {/* TAB 4: 시스템 진단 및 리셋 */}
              {activeTab === 'system' && (
                <div className="tab-content-pane">
                  <h3 className="pane-title">시스템 진단 및 초기화</h3>
                  
                  <div className="diagnostics-status-card">
                    <div className="status-indicator-row">
                      <span className="status-dot online"></span>
                      <span className="status-indicator-label">시스템 상태: 정상 가동 중 (ONLINE)</span>
                    </div>
                    <div className="diagnostics-details">
                      <p><strong>데이터베이스 타입:</strong> H2 In-Memory Database</p>
                      <p><strong>활성 커넥션 정보:</strong> jdbc:h2:mem:hiresync (sa)</p>
                      <p><strong>운영 체제 환경:</strong> Linux (Ubuntu-based Sandbox)</p>
                    </div>
                  </div>

                  <div className="danger-zone-container">
                    <h4 className="danger-zone-title">위험 지대 (Danger Zone)</h4>
                    <p className="danger-zone-desc">아래 작업은 실행 즉시 복구할 수 없습니다. 신중히 결정해 주세요.</p>
                    
                    <div className="danger-action-row">
                      <div className="action-desc-group">
                        <h5>데이터베이스 임시 초기화 (Reset Database)</h5>
                        <p>H2 인메모리 데이터베이스에 적재된 모든 채용 정보(SK키파운드리 제외)를 즉시 영구 삭제합니다.</p>
                      </div>
                      <button 
                        className="danger-btn-action" 
                        onClick={handleResetDatabase}
                        disabled={isResetting}
                      >
                        {isResetting ? '초기화 중...' : '데이터베이스 리셋'}
                      </button>
                    </div>
                  </div>
                </div>
              )}

            </div>

            {/* 설정 하단 버튼 영역 */}
            {activeTab !== 'system' && (
              <footer className="form-panel-footer">
                <button 
                  className="settings-save-btn"
                  onClick={handleSaveSettings}
                  disabled={isSaving}
                >
                  <span className="material-symbols-outlined">save</span>
                  <span>{isSaving ? '저장 중...' : '설정 저장'}</span>
                </button>
              </footer>
            )}
          </main>

        </div>
      </div>

      {/* 토스트 알림 팝업 */}
      {showToast && (
        <div className="settings-toast-popup">
          <span className="material-symbols-outlined toast-check-icon">check_circle</span>
          <span>{toastMessage}</span>
        </div>
      )}
    </div>
  );
}

export default SystemSettingsPage;
