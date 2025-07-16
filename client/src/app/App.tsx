import { router } from '@/app/routes';
import GlobalStyles from '@/app/styles/GlobalStyles';
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
