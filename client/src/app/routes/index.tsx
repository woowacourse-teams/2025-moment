import { Layout } from '@/app/layout/ui';
import { ROUTES } from '@/app/routes/routes';
import HomePage from '@/pages/home';
import MyMoments from '@/pages/myMoments';
import LoginPage from '@/pages/login';
import SignupPage from '@/pages/signup';

import TodayMomentPage from '@/pages/todayMoment';
import PostCommentsPage from '@/pages/postComments';

import { createBrowserRouter, createRoutesFromElements, Route } from 'react-router';
import TodayCommentPage from '@/pages/todayComment/TodayCommentPage';
import TodayCommentSuccessPage from '@/pages/todayComment/TodayCommentSuccessPage';

export const router = createBrowserRouter(
  createRoutesFromElements(
    <Route path={ROUTES.ROOT} element={<Layout />}>
      <Route index element={<HomePage />} />
      <Route path={ROUTES.SIGNUP} element={<SignupPage />} />
      <Route path={ROUTES.LOGIN} element={<LoginPage />} />
      <Route path={ROUTES.MY_MOMENTS} element={<MyMoments />} />
      <Route path={ROUTES.TODAY_MOMENT} element={<TodayMomentPage />} />
      <Route path={ROUTES.POST_COMMENTS} element={<PostCommentsPage />} />
      <Route path={ROUTES.TODAY_MOMENT} element={<TodayMomentPage />} />
      <Route path={ROUTES.TODAY_COMMENT} element={<TodayCommentPage />} />
      <Route path={ROUTES.TODAY_COMMENT_SUCCESS} element={<TodayCommentSuccessPage />} />
    </Route>,
  ),
);
