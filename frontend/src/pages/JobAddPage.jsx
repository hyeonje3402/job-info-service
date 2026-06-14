import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import './JobAddPage.css';

function JobAddPage({ isModal = false, onClose, onSuccess }) {
  const navigate = useNavigate();
  const fileInputRef = useRef(null);

  const [activeTab, setActiveTab] = useState('file'); // 'file' or 'url'
  const [file, setFile] = useState(null);
  const [url, setUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [dragActive, setDragActive] = useState(false);

  // 드롭존 클릭 시 브라우저 파일 탐색기 열기
  const handleDropzoneClick = () => {
    if (fileInputRef.current) {
      fileInputRef.current.click();
    }
  };

  // 파일 탐색기를 통한 파일 선택
  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
      setError('');
    }
  };

  // 드래그 앤 드롭 핸들러
  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const droppedFile = e.dataTransfer.files[0];
      // PDF 확장자 검사
      if (droppedFile.name.endsWith('.pdf') || droppedFile.type === 'application/pdf') {
        setFile(droppedFile);
        setError('');
      } else {
        setError('PDF 형식의 파일만 업로드할 수 있습니다.');
      }
    }
  };

  // 취소 버튼
  const handleCancel = () => {
    if (isModal && onClose) {
      onClose();
    } else {
      navigate('/'); // 메인JobList로 이동
    }
  };

  // 폼 확인 전송 제출
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (activeTab === 'file') {
        if (!file) {
          setError('업로드할 PDF 파일을 선택해 주세요.');
          setLoading(false);
          return;
        }

        // 1. PDF 파일 전송용 FormData 구축
        const formData = new FormData();
        formData.append('file', file);

        // 명시적으로 withCredentials를 추가해 쿠키 동봉
        const response = await api.post('/api/v1/sources/pdf', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          },
          withCredentials: true
        });

        if (response.data && response.data.status === 'success') {
          alert('PDF 채용정보 공고가 성공적으로 등록 및 파싱되었습니다. (APPROVED 승인 대기상태)');
          if (isModal && onSuccess) {
            onSuccess();
          } else {
            navigate('/'); // 메인 리스트로 복귀
          }
        } else {
          const errMsg = response.data?.message || '파일 업로드 및 채용정보 추가 처리에 실패했습니다.';
          setError(errMsg);
        }

      } else {
        if (!url.trim()) {
          setError('파싱할 데이터 소스 URL 주소를 입력해 주세요.');
          setLoading(false);
          return;
        }

        // 2. URL 파싱 요청용 JSON 데이터 전송
        // 명시적으로 withCredentials를 추가해 쿠키 동봉
        const response = await api.post('/api/v1/sources/url', {
          url: url.trim()
        }, {
          withCredentials: true
        });

        if (response.data && response.data.status === 'success') {
          alert('지정된 URL 채용정보 공고가 성공적으로 등록 및 파싱되었습니다. (APPROVED 승인 대기상태)');
          if (isModal && onSuccess) {
            onSuccess();
          } else {
            navigate('/'); // 메인 리스트로 복귀
          }
        } else {
          const errMsg = response.data?.message || 'URL 등록 및 채용정보 추가 처리에 실패했습니다.';
          setError(errMsg);
        }
      }
    } catch (err) {
      console.error('채용정보 추가 요청 에러 상세:', {
        message: err.message,
        name: err.name,
        code: err.code,
        status: err.response?.status,
        statusText: err.response?.statusText,
        responseData: err.response?.data,
        config: err.config
      });
      // 세션 세부 정보 누락(401 권한 미달 등) 및 백엔드 피드백 처리
      const errorMessage = err.response?.data?.message || '인증 정보 세션(JSESSIONID) 만료되었거나, 처리 중 서버 오류가 발생하였습니다.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={isModal ? "addjob-modal-body" : "addjob-page-root"}>
      {/* Main 폼 카드 */}
      <main className={isModal ? "addjob-modal-card" : "addjob-card"}>
        {/* Header */}
        <header className="addjob-header">
          <h1 className="addjob-title">데이터 소스 추가</h1>
          <p className="addjob-subtitle">새로운 채용 공고 데이터를 가져올 방식을 선택하세요.</p>
        </header>

        {/* 세그먼티드 컨트롤 (탭 영역) */}
        <div className="addjob-segmented-control">
          <button
            type="button"
            className={`segmented-tab-btn ${activeTab === 'file' ? 'active' : ''}`}
            onClick={() => {
              setActiveTab('file');
              setError('');
            }}
            disabled={loading}
          >
            <span className="material-symbols-outlined tab-icon">upload_file</span>
            <span>파일 입력</span>
          </button>
          <button
            type="button"
            className={`segmented-tab-btn ${activeTab === 'url' ? 'active' : ''}`}
            onClick={() => {
              setActiveTab('url');
              setError('');
            }}
            disabled={loading}
          >
            <span className="material-symbols-outlined tab-icon">link</span>
            <span>URL 입력</span>
          </button>
        </div>

        {/* 에러 피드백 배너 */}
        {error && (
          <div style={{
            color: '#991B1B',
            backgroundColor: '#FEE2E2',
            border: '1px solid #FCA5A5',
            padding: '12px',
            borderRadius: '4px',
            fontSize: '14px',
            marginBottom: '24px',
            textAlign: 'center',
            fontWeight: '500'
          }}>
            {error}
          </div>
        )}

        {/* 폼 입력 몸체 영역 */}
        <form onSubmit={handleSubmit}>
          <div className="addjob-form-body-area">
            
            {/* 1. 파일 입력 영역 (activeTab === 'file' 일 때 노출) */}
            {activeTab === 'file' && (
              <div 
                className={`file-dropzone-container ${dragActive ? 'drag-active' : ''}`}
                onClick={handleDropzoneClick}
                onDragEnter={handleDrag}
                onDragOver={handleDrag}
                onDragLeave={handleDrag}
                onDrop={handleDrop}
              >
                <input 
                  type="file"
                  ref={fileInputRef}
                  className="hidden-file-input"
                  style={{ display: 'none' }}
                  accept=".pdf"
                  onChange={handleFileChange}
                  disabled={loading}
                />
                
                <div className="dropzone-icon-circle">
                  <span className="material-symbols-outlined" style={{ fontSize: '32px' }}>
                    cloud_upload
                  </span>
                </div>
                
                <div>
                  <h3 className="dropzone-main-label">
                    {file ? file.name : '파일을 선택하거나 드래그하세요'}
                  </h3>
                  <p className="dropzone-sub-label">
                    {file ? `${(file.size / 1024 / 1024).toFixed(2)} MB` : '지원 형식: PDF (최대 50MB)'}
                  </p>
                </div>

                <button 
                  type="button" 
                  className="dropzone-btn"
                  disabled={loading}
                >
                  {file ? '다른 파일 찾기' : '파일 찾기'}
                </button>
              </div>
            )}

            {/* 2. URL 입력 영역 (activeTab === 'url' 일 때 노출) */}
            {activeTab === 'url' && (
              <div className="url-form-group">
                <label className="url-input-label" htmlFor="url-input">데이터 소스 URL</label>
                <div className="url-input-wrapper">
                  <span className="material-symbols-outlined url-input-icon">
                    public
                  </span>
                  <input
                    type="url"
                    id="url-input"
                    className="url-input-field"
                    placeholder="https://example.com/jobs/spec"
                    value={url}
                    onChange={(e) => setUrl(e.target.value)}
                    required
                    disabled={loading}
                  />
                </div>
                <p className="url-info-desc">
                  외부 채용 플랫폼 또는 사내 시스템의 API 엔드포인트를 입력하세요.
                </p>
              </div>
            )}

          </div>

          {/* 하단 액션 버튼 바 */}
          <div className="addjob-action-bar">
            <button 
              className="addjob-btn addjob-btn-outline" 
              type="button" 
              onClick={handleCancel}
              disabled={loading}
            >
              취소
            </button>
            <button 
              className="addjob-btn addjob-btn-primary" 
              type="submit"
              disabled={loading}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                check
              </span>
              <span>{loading ? '등록 중...' : '확인'}</span>
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}

export default JobAddPage;
