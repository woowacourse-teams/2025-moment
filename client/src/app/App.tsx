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
import { setupForegroundMessage } from '@/shared/utils/firebase';
import { shouldShowIOSBrowserWarning } from '@/shared/utils/detectBrowser';
import { IOSBrowserWarning } from '@/shared/ui/IOSBrowserWarning';

const AppContent = () => {
  useEffect(() => {
    const initializeMessaging = async () => {
      try {
        if ('serviceWorker' in navigator) {
          await navigator.serviceWorker.ready;
        }
        await setupForegroundMessage();
      } catch (error) {
        console.error('[FCM] Messaging 초기화 오류:', error);
      }
    };

    initializeMessaging();
  }, []);

  return (
    <ThemeProvider theme={theme}>
      <GlobalStyles />
      {shouldShowIOSBrowserWarning() && <IOSBrowserWarning />}
      <RouterProvider router={router} />
      {process.env.NODE_ENV === 'development' && <ReactQueryDevtools initialIsOpen={false} />}
    </ThemeProvider>
  );
};

const App = () => {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <AppContent />
      </QueryClientProvider>
    </ErrorBoundary>
  );
};

export default App;
