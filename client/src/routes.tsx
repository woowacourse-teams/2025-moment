import { ROUTES } from '@/constants/routes';
import { Layout } from '@/layout/ui';
import HomePage from '@/pages/home';
import SignupPage from '@/pages/signup';
import { createBrowserRouter, createRoutesFromElements, Route } from 'react-router';

export const router = createBrowserRouter(
  createRoutesFromElements(
    <Route path={ROUTES.ROOT} element={<Layout />}>
      <Route index element={<HomePage />} />
      <Route path={ROUTES.SIGNUP} element={<SignupPage />} />
    </Route>,
  ),
);
