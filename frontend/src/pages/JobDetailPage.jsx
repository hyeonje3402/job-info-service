import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import axios from 'axios';

const JobDetailPage = () => {
  const { id } = useParams();
  const [job, setJob] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchJobDetail = async () => {
      try {
        const response = await axios.get(`/api/job-postings/${id}`);
        setJob(response.data);
        setLoading(false);
      } catch (err) {
        setError('데이터를 불러오는 중 오류가 발생했습니다.');
        setLoading(false);
      }
    };

    fetchJobDetail();
  }, [id]);

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div>{error}</div>;
  if (!job) return <div>채용 정보를 찾을 수 없습니다.</div>;

  return (
    <div className="job-detail-container">
      <Link to="/" className="back-link">← 목록으로 돌아가기</Link>
      <h2>{job.position}</h2>
      <div className="job-info">
        <p><strong>회사명:</strong> {job.companyName}</p>
        <p><strong>위치:</strong> {job.location}</p>
        <p><strong>채용 조건:</strong></p>
        <div className="requirements-text">{job.requirements}</div>
        <p><strong>모집 기간:</strong> {job.openingDate} ~ {job.closingDate}</p>
        <p><strong>지원 방법:</strong> {job.applicationMethod}</p>
        <p><strong>예상 급여:</strong> {job.expectedSalary}</p>
      </div>
    </div>
  );
};

export default JobDetailPage;
