import * as Sentry from '@sentry/react';
import axios, { AxiosError, AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { queryClient } from './queryClient';
import { toasts } from '@/shared/store/toast';

export const BASE_URL = process.env.REACT_APP_BASE_URL || 'http://localhost:8080/api/v1';

const commonConfig = {
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
  withCredentials: true,
};

export const api = axios.create({
  baseURL: BASE_URL,
  ...commonConfig,
});

type RequestConfig = InternalAxiosRequestConfig & { _retry?: boolean };

let isRefreshing = false;
let refreshPromise: Promise<void> | null = null;

const refreshToken = async (): Promise<void> => {
  const refreshApi = axios.create({
    baseURL: BASE_URL,
    ...commonConfig,
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

const setupInterceptors = (instance: AxiosInstance) => {
  instance.interceptors.response.use(
    (response: AxiosResponse) => response,
    async (error: AxiosError) => {
      const originalRequest = error.config as RequestConfig;
      const status = error.response?.status;
      const url = originalRequest?.url ?? '';

      // 서버 에러 응답에서 에러 코드 추출
      const serverError = error.response?.data;
      const errorCode = (serverError as { code: string })?.code || 'unknown';
      const errorMessage = (serverError as { message: string })?.message || error.message;

      const domain = url.split('/')[4] || 'unknown';
      const endpoint = url.replace(instance.defaults.baseURL || '', '') || '/';
      const httpMethod = originalRequest?.method?.toUpperCase() || 'UNKNOWN';

      const level = 'error';

      Sentry.captureException(error, {
        level,
        tags: {
          domain,
          http_method: httpMethod,
          http_status: status?.toString() || 'unknown',
          endpoint,
          error_code: errorCode,
        },
        contexts: {
          request: {
            url: originalRequest?.url || url,
            method: httpMethod,
            baseURL: instance.defaults.baseURL,
          },
          response: {
            status: status || 0,
            statusText: error.response?.statusText || 'Unknown Error',
            serverError: serverError,
          },
          error_details: {
            message: errorMessage,
            code: errorCode,
          },
        },
      });

      if (url.includes('/auth/refresh') && (status === 401 || status === 403)) {
        queryClient.setQueryData(['checkIfLoggedIn'], false);
        toasts.error('로그인이 만료되었어요! 다시 로그인해 주세요.');
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
            return instance(originalRequest);
          } catch {
            queryClient.setQueryData(['checkIfLoggedIn'], false);
            toasts.error('잠시 문제가 생겼어요. 다시 로그인해 주세요.');
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
          return instance(originalRequest);
        } catch (refreshError) {
          queryClient.setQueryData(['checkIfLoggedIn'], false);
          toasts.error('로그인이 만료되었어요. 다시 로그인해 주세요.');
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
};

setupInterceptors(api);
