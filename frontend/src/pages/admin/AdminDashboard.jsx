import React, { useState, useEffect, useActionState } from 'react';
import axios from 'axios';

const AdminDashboard = () => {
  const [jobPostings, setJobPostings] = useState([]);
  const [file, setFile] = useState(null);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchJobPostings();
  }, []);

  const fetchJobPostings = async () => {
    try {
      const response = await axios.get('/api/v1/jobs');
      setJobPostings(response.data.data?.content || []);
      setLoading(false);
    } catch (err) {
      console.error('Failed to fetch job postings', err);
      setLoading(false);
    }
  };

  // URL 등록 Action
  const [urlState, urlAction, isUrlPending] = useActionState(async (prevState, formData) => {
    const urlValue = formData.get('url');
    try {
      await axios.post('/api/v1/sources/url', { url: urlValue });
      fetchJobPostings();
      return { success: true, message: 'URL이 성공적으로 등록되었습니다.' };
    } catch (err) {
      return { success: false, message: 'URL 등록 중 오류가 발생했습니다.' };
    }
  }, null);

  // PDF 업로드 처리
  const handlePdfUpload = async (e) => {
    e.preventDefault();
    if (!file) {
      setMessage('파일을 선택해주세요.');
      return;
    }

    const formData = new FormData();
    formData.append('file', file); // 백엔드 파라미터명이 'file'임

    try {
      await axios.post('/api/v1/sources/pdf', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        withCredentials: true
      });
      setMessage('PDF가 성공적으로 분석 및 등록되었습니다.');
      fetchJobPostings();
    } catch (err) {
      setMessage('PDF 분석 중 오류가 발생했습니다.');
    }
  };

  // 삭제 처리
  const handleDelete = async (id) => {
    if (!window.confirm('정말 삭제하시겠습니까?')) return;
    try {
      await axios.delete(`/api/v1/jobs/${id}`, { withCredentials: true });
      fetchJobPostings();
      alert('삭제되었습니다.');
    } catch (err) {
      alert('삭제 중 오류가 발생했습니다.');
    }
  };

  // 승인 처리 (Status PENDING -> APPROVED)
  const handleApprove = async (job) => {
    try {
      await axios.put(`/api/v1/jobs/${job.id}`, { ...job, status: 'APPROVED' }, { withCredentials: true });
      fetchJobPostings();
      alert('승인되었습니다.');
    } catch (err) {
      alert('승인 중 오류가 발생했습니다.');
    }
  };

  return (
    <div className="admin-dashboard">
      <h2>관리자 대시보드</h2>

      <div className="admin-grid">
        <section className="admin-section">
          <h3>신규 등록</h3>
          <div className="upload-box">
            <h4>URL 등록</h4>
            <form action={urlAction}>
              <input type="url" name="url" placeholder="URL 입력" required />
              <button type="submit" disabled={isUrlPending}>
                {isUrlPending ? '등록 중...' : 'URL 등록'}
              </button>
            </form>
            {urlState && <p className={urlState.success ? 'success' : 'error'}>{urlState.message}</p>}
          </div>

          <div className="upload-box">
            <h4>PDF 업로드</h4>
            <form onSubmit={handlePdfUpload}>
              <input type="file" accept=".pdf" onChange={(e) => setFile(e.target.files[0])} required />
              <button type="submit">PDF 분석</button>
            </form>
            {message && <p className="message">{message}</p>}
          </div>
        </section>

        <section className="admin-section">
          <h3>채용 정보 관리</h3>
          {loading ? <p>로딩 중...</p> : (
            <table className="admin-table">
              <thead>
                <tr>
                  <th>회사명</th>
                  <th>직무</th>
                  <th>상태</th>
                  <th>관리</th>
                </tr>
              </thead>
              <tbody>
                {jobPostings.map(job => (
                  <tr key={job.id}>
                    <td>{job.companyName}</td>
                    <td>{job.position}</td>
                    <td><span className={`status-badge ${job.status.toLowerCase()}`}>{job.status}</span></td>
                    <td>
                      {job.status === 'PENDING' && (
                        <button onClick={() => handleApprove(job)} className="btn-approve">승인</button>
                      )}
                      <button onClick={() => handleDelete(job.id)} className="btn-delete">삭제</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      </div>
    </div>
  );
};

export default AdminDashboard;
