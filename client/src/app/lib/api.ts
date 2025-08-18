import * as Sentry from '@sentry/react';
import axios, { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from 'axios';

export const BASE_URL = process.env.REACT_APP_BASE_URL || 'http://localhost:8080/api/v1';

export const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
  withCredentials: true,
});

type RequestConfig = InternalAxiosRequestConfig & { _retry?: boolean };

let isRefreshing = false;
let refreshPromise: Promise<void> | null = null;

const refreshToken = async (): Promise<void> => {
  await axios.post(`${BASE_URL}/auth/refresh`, {}, { withCredentials: true });
};

const redirectToLogin = (): void => {
  if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
    window.location.href = '/login';
  }
};

const isPublicEndpoint = (url: string): boolean => {
  return url.includes('/auth');
};

api.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RequestConfig;
    const status = error.response?.status;
    const url = originalRequest?.url ?? '';

    Sentry.captureException(error);

    // 퍼블릭 엔드포인트는 인터셉터 제외
    if (isPublicEndpoint(url)) {
      return Promise.reject(error);
    }

    // 리프레시 엔드포인트 자체의 인증 실패
    if (url.includes('/auth/refresh') && (status === 401 || status === 403)) {
      redirectToLogin();
      return Promise.reject(error);
    }

    // 401 에러이고 아직 재시도하지 않은 경우 토큰 리프레시 시도
    if (status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      // 이미 리프레시가 진행 중인 경우 해당 Promise를 기다림
      if (isRefreshing && refreshPromise) {
        try {
          await refreshPromise;
          return api(originalRequest);
        } catch {
          redirectToLogin();
          return Promise.reject(error);
        }
      }

      // 새로운 리프레시 시작
      isRefreshing = true;
      refreshPromise = refreshToken();

      try {
        await refreshPromise;
        return api(originalRequest);
      } catch (refreshError) {
        redirectToLogin();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
        refreshPromise = null;
      }
    }

    return Promise.reject(error);
  },
);
