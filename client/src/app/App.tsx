import { router } from '@/app/routes';
import { UserProvider } from '@/features/auth/context/UserProvider';
import { ThemeProvider } from '@emotion/react';
import { RouterProvider } from 'react-router';
import GlobalStyles from './styles/GlobalStyles';
import { theme } from './styles/theme';

const App = () => {
  return (
    <>
      <ThemeProvider theme={theme}>
        <GlobalStyles />
        <UserProvider>
          <RouterProvider router={router} />
        </UserProvider>
      </ThemeProvider>
    </>
  );
};

export default App;
