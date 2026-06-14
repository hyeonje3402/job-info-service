import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_BACKEND || '',
  withCredentials: true,
});

// Response Interceptor to handle session expiration (401 Unauthorized)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('authToken');
      // 사용자가 알아볼 수 있도록 알림을 띄우고 리다이렉트
      alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
