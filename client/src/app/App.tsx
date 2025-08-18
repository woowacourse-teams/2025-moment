import { queryClient } from '@/app/lib/queryClient';
import { router } from '@/app/routes';
import { ToastProvider } from '@/shared/context/toast/ToastProvider';
import { ErrorBoundary } from '@/shared/ui';
import { ThemeProvider } from '@emotion/react';
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { StrictMode } from 'react';
import { RouterProvider } from 'react-router';
import GlobalStyles from './styles/GlobalStyles';
import { theme } from './styles/theme';

const App = () => {
  return (
    <StrictMode>
      <ErrorBoundary>
        <QueryClientProvider client={queryClient}>
          <ThemeProvider theme={theme}>
            <ToastProvider>
              <GlobalStyles />
              <RouterProvider router={router} />
              {process.env.NODE_ENV === 'development' && (
                <ReactQueryDevtools initialIsOpen={false} />
              )}
            </ToastProvider>
          </ThemeProvider>
        </QueryClientProvider>
      </ErrorBoundary>
    </StrictMode>
  );
};

export default App;
