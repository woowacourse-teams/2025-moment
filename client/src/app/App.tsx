import { router } from '@/app/routes';
import { RouterProvider } from 'react-router';
import { ThemeProvider } from '@emotion/react';
import { theme } from './styles/theme';
import GlobalStyles from './styles/GlobalStyles';

const App = () => {
  return (
    <>
      <ThemeProvider theme={theme}>
        <GlobalStyles />
        <RouterProvider router={router} />
      </ThemeProvider>
    </>
  );
};

export default App;
