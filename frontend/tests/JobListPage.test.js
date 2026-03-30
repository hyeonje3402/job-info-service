import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import axios from 'axios';
import JobListPage from '../src/pages/JobListPage';

jest.mock('axios');

describe('JobListPage', () => {
  test('renders job postings from API', async () => {
    const jobPostings = {
      data: {
        content: [
          { id: 1, companyName: 'Company A', jobCategory: 'Backend' },
          { id: 2, companyName: 'Company B', jobCategory: 'Frontend' },
        ]
      }
    };

    axios.get.mockResolvedValue(jobPostings);

    render(
      <MemoryRouter>
        <JobListPage />
      </MemoryRouter>
    );

    // Initial loading state or empty
    expect(screen.getByText(/채용 정보 목록/i)).toBeInTheDocument();

    // Wait for data to be displayed
    await waitFor(() => {
      expect(screen.getByText('Company A')).toBeInTheDocument();
      expect(screen.getByText('Company B')).toBeInTheDocument();
    });
  });

  test('renders error message on API failure', async () => {
    axios.get.mockRejectedValue(new Error('API Error'));

    render(
      <MemoryRouter>
        <JobListPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/데이터를 불러오는 중 오류가 발생했습니다/i)).toBeInTheDocument();
    });
  });
});
