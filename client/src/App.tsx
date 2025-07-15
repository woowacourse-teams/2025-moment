import { router } from '@/routes';
import { ThemeProvider } from '@emotion/react';
import { RouterProvider } from 'react-router';
import { theme } from './styles/theme';

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <RouterProvider router={router} />
    </ThemeProvider>
  );
};

export default App;
