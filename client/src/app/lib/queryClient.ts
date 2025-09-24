import { QueryClient } from '@tanstack/react-query';
import { AxiosError } from 'axios';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5분간 fresh 유지
      gcTime: 10 * 60 * 1000, // 10분간 캐시 유지 (구 cacheTime)
      retry: (failureCount, error: unknown) => {
        if (
          (error as AxiosError)?.response?.status === 401 ||
          (error as AxiosError)?.response?.status === 403
        ) {
          return failureCount < 1;
        }
        return failureCount < 3;
      },
      refetchOnWindowFocus: false, // 윈도우 포커스 시 자동 리패치 비활성화
      refetchOnReconnect: true, // 네트워크 재연결 시 리패치
    },
    mutations: {
      retry: false, // mutation은 재시도하지 않음
    },
  },
});
