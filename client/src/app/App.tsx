import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { queryClient } from '@/app/lib/queryClient';
import { router } from '@/app/routes';
import { ThemeProvider } from '@emotion/react';
import { QueryClientProvider } from '@tanstack/react-query';
import { RouterProvider } from 'react-router';
import GlobalStyles from './styles/GlobalStyles';
import { theme } from '../shared/styles/theme';
import { useInitializeFCM } from '@/shared/lib/notifications/useInitializeFCM';
import { ErrorBoundary } from '@/shared/ui/errorBoundary';

const AppContent = () => {
  return (
    <ThemeProvider theme={theme}>
      <GlobalStyles />
      <RouterProvider router={router} />
      {/* {process.env.NODE_ENV === 'development' && <ReactQueryDevtools initialIsOpen={false} />} */}
    </ThemeProvider>
  );
};

const AppContentWithFCM = () => {
  useInitializeFCM();
  return <AppContent />;
};

const App = () => {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <AppContentWithFCM />
      </QueryClientProvider>
    </ErrorBoundary>
  );
};

export default App;
