import { queryClient } from '@/app/lib/queryClient';
import { router } from '@/app/routes';
import { ThemeProvider } from '@emotion/react';
import { QueryClientProvider } from '@tanstack/react-query';
import { RouterProvider } from 'react-router';
import { useEffect } from 'react';
import GlobalStyles from './styles/GlobalStyles';
import { theme } from '../shared/styles/theme';
import { useInitializePushNotification } from '@/shared/lib/notifications/useInitializePushNotification';
import { ErrorBoundary } from '@/shared/ui/errorBoundary';

declare global {
  interface Window {
    onTabFocus?: () => void;
  }
}

const AppContent = () => {
  return (
    <ThemeProvider theme={theme}>
      <GlobalStyles />
      <RouterProvider router={router} />
      {/* {process.env.NODE_ENV === 'development' && <ReactQueryDevtools initialIsOpen={false} />} */}
    </ThemeProvider>
  );
};

const AppContentWithPushNotification = () => {
  useInitializePushNotification();

  useEffect(() => {
    if (typeof window !== 'undefined') {
      window.onTabFocus = () => {
        queryClient.invalidateQueries({ queryKey: ['checkIfLoggedIn'] });
        console.log('Tab Focused: Refetching login state');
      };
    }
  }, []);

  return <AppContent />;
};

const App = () => {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <AppContentWithPushNotification />
      </QueryClientProvider>
    </ErrorBoundary>
  );
};

export default App;
