import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';

const JobListPage = () => {
  const [jobPostings, setJobPostings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchJobs = async () => {
      try {
        const response = await axios.get('/api/v1/jobs');
        setJobPostings(response.data.data?.content || []);
        setLoading(false);
      } catch (err) {
        setError('데이터를 불러오는 중 오류가 발생했습니다.');
        setLoading(false);
      }
    };

    fetchJobs();
  }, []);

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div className="job-list-container">
      <h2>채용 정보 목록</h2>
      <div className="job-grid">
        {jobPostings.map((job) => (
          <div key={job.id} className="job-card">
            <h3>{job.position}</h3>
            <p><strong>회사:</strong> {job.companyName}</p>
            <p><strong>위치:</strong> {job.location}</p>
            <p><strong>마감일:</strong> {job.closingDate}</p>
            <Link to={`/job-postings/${job.id}`} className="detail-link">상세보기</Link>
          </div>
        ))}
      </div>
      {jobPostings.length === 0 && <p>등록된 채용 정보가 없습니다.</p>}
    </div>
  );
};

export default JobListPage;
