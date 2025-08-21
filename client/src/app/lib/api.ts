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
  await api.post('/auth/refresh');
};

const redirectToLogin = (): void => {
  if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
    window.location.href = '/login';
  }
};

const isPublicEndpoint = (url: string): boolean => {
  const publicEndpoints = ['/auth/login', '/auth/refresh', '/auth/email', '/users/signup'];

  return publicEndpoints.some(endpoint => url.includes(endpoint));
};

api.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RequestConfig;
    const status = error.response?.status;
    const url = originalRequest?.url ?? '';

    Sentry.captureException(error);

    if (isPublicEndpoint(url)) {
      return Promise.reject(error);
    }

    if (url.includes('/auth/refresh') && (status === 401 || status === 403)) {
      redirectToLogin();
      return Promise.reject(error);
    }

    if (status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      if (isRefreshing && refreshPromise) {
        try {
          await refreshPromise;
          return api(originalRequest);
        } catch {
          redirectToLogin();
          return Promise.reject(error);
        }
      }

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
