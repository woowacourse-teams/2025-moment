import { Layout } from '@/app/layout/ui';
import { ProtectedRoute } from '@/app/routes/ProtectedRoute';
import { ROUTES } from '@/app/routes/routes';
import MyCommentCollectionPage from '@/pages/collection/mycomment';
import MyMomentCollectionPage from '@/pages/collection/mymoment';
import FindPasswordPage from '@/pages/findPassword';
import GoogleCallbackPage from '@/pages/googleCallback';
import HomePage from '@/pages/home';
import LoginPage from '@/pages/login';
import MyPage from '@/pages/my';
import NewPasswordPage from '@/pages/newPassword';
import NotFoundPage from '@/pages/notFound';
import SignupPage from '@/pages/signup';
import TodayCommentPage from '@/pages/todayComment';
import TodayMomentPage from '@/pages/todayMoment';
import TodayMomentExtraPage from '@/pages/todayMoment/TodayMomentExtraPage';
import TodayMomentSuccessPage from '@/pages/todayMoment/TodayMomentSuccessPage';
import { createBrowserRouter, createRoutesFromElements, Route } from 'react-router';

export const router = createBrowserRouter(
  createRoutesFromElements(
    <Route path={ROUTES.ROOT} element={<Layout />}>
      <Route index element={<HomePage />} />
      <Route path={ROUTES.SIGNUP} element={<SignupPage />} />
      <Route path={ROUTES.LOGIN} element={<LoginPage />} />
      <Route path={ROUTES.GOOGLE_CALLBACK} element={<GoogleCallbackPage />} />
      <Route path={ROUTES.TODAY_MOMENT} element={<TodayMomentPage />} />
      <Route path={ROUTES.TODAY_COMMENT} element={<TodayCommentPage />} />
      <Route path={ROUTES.FIND_PASSWORD} element={<FindPasswordPage />} />
      <Route path={ROUTES.NEW_PASSWORD} element={<NewPasswordPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path={ROUTES.COLLECTION_MYMOMENT} element={<MyMomentCollectionPage />} />
        <Route path={ROUTES.COLLECTION_MYCOMMENT} element={<MyCommentCollectionPage />} />
        <Route path={ROUTES.TODAY_MOMENT_EXTRA} element={<TodayMomentExtraPage />} />
        <Route path={ROUTES.TODAY_MOMENT_SUCCESS} element={<TodayMomentSuccessPage />} />
        <Route path={ROUTES.MY} element={<MyPage />} />
      </Route>
      <Route path="*" element={<NotFoundPage />} />
    </Route>,
  ),
);
