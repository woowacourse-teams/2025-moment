import { Layout } from '@/app/layout/ui';
import { ROUTES } from '@/app/routes/routes';
import HomePage from '@/pages/home';
import SignupPage from '@/pages/signup';
import TodayMomentPage from '@/pages/todayMoment';

import { createBrowserRouter, createRoutesFromElements, Route } from 'react-router';

export const router = createBrowserRouter(
  createRoutesFromElements(
    <Route path={ROUTES.ROOT} element={<Layout />}>
      <Route index element={<HomePage />} />
      <Route path={ROUTES.SIGNUP} element={<SignupPage />} />
      <Route path={ROUTES.TODAY_MOMENT} element={<TodayMomentPage />} />
    </Route>,
  ),
);
