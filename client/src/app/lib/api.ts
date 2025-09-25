import * as Sentry from '@sentry/react';
import axios, { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { queryClient } from './queryClient';

export const BASE_URL = process.env.REACT_APP_BASE_URL || 'http://localhost:8080/api/v1';

type ShowToastFn = (message: string, duration?: number) => void;
let showErrorToast: ShowToastFn | null = null;
export const setToastFunctions = (errorFn: ShowToastFn) => {
  showErrorToast = errorFn;
};

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
  const refreshApi = axios.create({
    baseURL: BASE_URL,
    headers: { 'Content-Type': 'application/json' },
    timeout: 10000,
    withCredentials: true,
  });

  try {
    await refreshApi.post('/auth/refresh');
  } catch (error) {
    console.log('Refresh token error:', error);
    if (error instanceof AxiosError && error.response) {
      console.log('Refresh token error response:', error.response.status, error.response.data);
    }
    throw error;
  }
};

const redirectToLogin = (): void => {
  if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
    window.location.href = '/login';
  }
};

api.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RequestConfig;
    const status = error.response?.status;
    const url = originalRequest?.url ?? '';

    Sentry.captureException(error);

    if (url.includes('/auth/refresh') && (status === 401 || status === 403)) {
      queryClient.setQueryData(['checkIfLoggedIn'], false);
      if (showErrorToast) {
        showErrorToast('로그인이 만료되었어요! 다시 로그인해 주세요.');
      }
      redirectToLogin();
      return Promise.reject(error);
    }

    if (status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      if (isRefreshing && refreshPromise) {
        try {
          await refreshPromise;
          queryClient.invalidateQueries({ queryKey: ['checkIfLoggedIn'] });
          queryClient.invalidateQueries({ queryKey: ['profile'] });
          return api(originalRequest);
        } catch {
          queryClient.setQueryData(['checkIfLoggedIn'], false);
          if (showErrorToast) {
            showErrorToast('잠시 문제가 생겼어요. 다시 로그인해 주세요.');
          }
          redirectToLogin();
          return Promise.reject(error);
        }
      }

      isRefreshing = true;
      refreshPromise = refreshToken();

      try {
        await refreshPromise;
        queryClient.invalidateQueries({ queryKey: ['checkIfLoggedIn'] });
        queryClient.invalidateQueries({ queryKey: ['profile'] });
        return api(originalRequest);
      } catch (refreshError) {
        queryClient.setQueryData(['checkIfLoggedIn'], false);
        if (showErrorToast) {
          showErrorToast('로그인이 만료되었어요. 다시 로그인해 주세요.');
        }
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
