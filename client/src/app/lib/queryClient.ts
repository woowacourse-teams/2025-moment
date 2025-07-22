import { QueryClient } from '@tanstack/react-query';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5분간 fresh 유지
      gcTime: 10 * 60 * 1000, // 10분간 캐시 유지 (구 cacheTime)
      retry: (failureCount, error: any) => {
        // 401, 403은 재시도하지 않음 (인증 오류)
        if (error?.response?.status === 401 || error?.response?.status === 403) {
          return false;
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
