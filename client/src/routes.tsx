import { ROUTES } from '@/constants/routes';
import { Layout } from '@/layout/ui/Layout';
import HomePage from '@/pages/home';
import { createBrowserRouter, createRoutesFromElements, Route } from 'react-router';

export const router = createBrowserRouter(
  createRoutesFromElements(
    <Route path={ROUTES.ROOT} element={<Layout />}>
      <Route path={ROUTES.ROOT} element={<HomePage />} />
    </Route>,
  ),
);
