import { router } from '@/routes';
import GlobalStyles from '@/styles/GlobalStyles';
import { RouterProvider } from 'react-router';

const App = () => {
  return (
    <>
      <GlobalStyles />
      <RouterProvider router={router} />
    </>
  );
};

export default App;
