import axios from 'axios';

const getBaseURL = () => {
  const envBackend = process.env.REACT_APP_BACKEND;
  if (envBackend) {
    try {
      const url = new URL(envBackend);
      // 브라우저가 접속한 호스트명(window.location.hostname)과 설정값의 호스트명이 다를 경우,
      // 브라우저 접속 호스트명에 맞춰 백엔드 API 주소를 동적으로 변경합니다.
      if (window.location.hostname !== url.hostname) {
        return `${url.protocol}//${window.location.hostname}:${url.port || '8080'}`;
      }
    } catch (e) {
      console.error("Failed to parse REACT_APP_BACKEND URL:", e);
    }
    return envBackend;
  }
  return '';
};

const api = axios.create({
  baseURL: getBaseURL(),
  withCredentials: true,
});

// Response Interceptor to handle session expiration (401 Unauthorized) or Permission issue (403 Forbidden)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      const isLoginRequest = error.config && error.config.url && error.config.url.includes('/api/v1/auth/login');
      if (!isLoginRequest) {
        localStorage.removeItem('authToken');
        // 사용자가 알아볼 수 있도록 알림을 띄우고 리다이렉트
        alert('인증이 만료되었거나 권한이 없습니다. 다시 로그인해 주세요.');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
