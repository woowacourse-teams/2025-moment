import { Layout } from '@/app/layout/ui';
import { ROUTES } from '@/app/routes/routes';
import HomePage from '@/pages/home';
import MyMoments from '@/pages/myMoments';
import SignupPage from '@/pages/signup';
import { createBrowserRouter, createRoutesFromElements, Route } from 'react-router';

export const router = createBrowserRouter(
  createRoutesFromElements(
    <Route path={ROUTES.ROOT} element={<Layout />}>
      <Route index element={<HomePage />} />
      <Route path={ROUTES.SIGNUP} element={<SignupPage />} />
      <Route path={ROUTES.MY_MOMENTS} element={<MyMoments />} />
    </Route>,
  ),
);
