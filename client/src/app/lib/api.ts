import axios from 'axios';
import * as Sentry from '@sentry/react';
import { AxiosError } from 'axios';

const BASE_URL = process.env.REACT_APP_BASE_URL || 'http://localhost:8080/api/v1';

export const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000, // 10초
  withCredentials: true,
});

api.interceptors.response.use(
  // 추후 refresh token 로직 추가
  // CD 테스트
  response => response,
  error => {
    Sentry.captureException(error.message);
  },
);
