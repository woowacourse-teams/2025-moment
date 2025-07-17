import { router } from '@/app/routes';
import { SignupProvider } from '@/features/auth/context/SignupProvider';
import { ThemeProvider } from '@emotion/react';
import { RouterProvider } from 'react-router';
import GlobalStyles from './styles/GlobalStyles';
import { theme } from './styles/theme';

const App = () => {
  return (
    <>
      <ThemeProvider theme={theme}>
        <GlobalStyles />
        <SignupProvider>
          <RouterProvider router={router} />
        </SignupProvider>
      </ThemeProvider>
    </>
  );
};

export default App;
