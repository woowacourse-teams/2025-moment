import axios from 'axios';

const BASE_URL = process.env.REACT_APP_BASE_URL || 'http://localhost:8080/api/v1';

export const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000, // 10초
  withCredentials: true,
});

api.interceptors.response.use(
  // 추후 refresh token 로직 추가
  response => response,
  error => {
    if (error.response.status === 401) {
      alert('로그인 후 이용해주세요.');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  },
);
