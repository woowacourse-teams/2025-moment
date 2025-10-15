import { queryClient } from '@/app/lib/queryClient';
import { router } from '@/app/routes';
import { ErrorBoundary } from '@/shared/ui';
import { ThemeProvider } from '@emotion/react';
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { RouterProvider } from 'react-router';
import GlobalStyles from './styles/GlobalStyles';
import { theme } from './styles/theme';
import { useEffect } from 'react';
import { requestFCMPermissionAndToken, setupForegroundMessage } from '@/shared/utils/firebase';
import { registerFCMToken } from '@/shared/api/registerFCMToken';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';

const App = () => {
  const { data: isLoggedIn } = useCheckIfLoggedInQuery();

  useEffect(() => {
    const initializeFCM = async () => {
      try {
        if ('serviceWorker' in navigator) {
          await navigator.serviceWorker.ready;
        }

        await setupForegroundMessage();

        if (!isLoggedIn) {
          return;
        }

        const token = await requestFCMPermissionAndToken();
        if (!token) return;

        await registerFCMToken(token);
        console.log('[FCM] 초기화 완료');
      } catch (error) {
        console.error('[FCM] 초기화 오류:', error);
      }
    };

    initializeFCM();
  }, [isLoggedIn]);

  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={theme}>
          <GlobalStyles />
          <RouterProvider router={router} />
          {process.env.NODE_ENV === 'development' && <ReactQueryDevtools initialIsOpen={false} />}
        </ThemeProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
};

export default App;
