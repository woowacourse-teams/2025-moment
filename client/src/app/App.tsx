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

const App = () => {
  useEffect(() => {
    setupForegroundMessage(payload => {
      alert(
        `알림 도착!\n제목: ${payload.notification?.title}\n내용: ${payload.notification?.body}`,
      );
    });

    const initializeFCM = async () => {
      try {
        await requestFCMPermissionAndToken();
      } catch (error) {
        console.error('FCM 초기화 오류:', error);
      }
    };

    initializeFCM();
  }, []);

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
