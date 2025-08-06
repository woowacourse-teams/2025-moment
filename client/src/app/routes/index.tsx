import { Layout } from '@/app/layout/ui';
import { ProtectedRoute } from '@/app/routes/ProtectedRoute';
import { ROUTES } from '@/app/routes/routes';
import Collection from '@/pages/collection';
import HomePage from '@/pages/home';
import LoginPage from '@/pages/login';
import SignupPage from '@/pages/signup';
import TodayCommentPage from '@/pages/todayComment';
import TodayCommentSuccessPage from '@/pages/todayComment/TodayCommentSuccessPage';
import TodayMomentPage from '@/pages/todayMoment';
import { createBrowserRouter, createRoutesFromElements, Route } from 'react-router';

export const router = createBrowserRouter(
  createRoutesFromElements(
    <Route path={ROUTES.ROOT} element={<Layout />}>
      <Route index element={<HomePage />} />
      <Route path={ROUTES.SIGNUP} element={<SignupPage />} />
      <Route path={ROUTES.LOGIN} element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path={ROUTES.COLLECTION} element={<Collection />} />
        <Route path={ROUTES.TODAY_MOMENT} element={<TodayMomentPage />} />
        <Route path={ROUTES.TODAY_COMMENT} element={<TodayCommentPage />} />
        <Route path={ROUTES.TODAY_COMMENT_SUCCESS} element={<TodayCommentSuccessPage />} />
      </Route>
    </Route>,
  ),
);
