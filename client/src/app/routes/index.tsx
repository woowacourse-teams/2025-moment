import { Layout } from '@/app/layout/ui';
import { ProtectedRoute } from '@/app/routes/ProtectedRoute';
import { ROUTES } from '@/app/routes/routes';
import HomePage from '@/pages/home';
import LoginPage from '@/pages/login';
import NotFoundPage from '@/pages/notFound';
import MyCommentCollectionPage from '@/pages/collection/mycomment';
import MyMomentCollectionPage from '@/pages/collection/mymoment';
import TodayCommentPage from '@/pages/todayComment';
import TodayMomentPage from '@/pages/todayMoment';
import TodayMomentSuccessPage from '@/pages/todayMoment/TodayMomentSuccessPage';
import MyPage from '@/pages/my';
import { FormPageSkeleton } from '@/shared/ui/skeleton/FormPageSkeleton';
import { StaticPageSkeleton } from '@/shared/ui/skeleton/StaticPageSkeleton';
import { lazy, Suspense } from 'react';
import { createBrowserRouter, createRoutesFromElements, Outlet, Route } from 'react-router';

const SignupPage = lazy(() => import('@/pages/signup'));
const GoogleCallbackPage = lazy(() => import('@/pages/googleCallback'));
const FindPasswordPage = lazy(() => import('@/pages/findPassword'));
const NewPasswordPage = lazy(() => import('@/pages/newPassword'));
const TermsPage = lazy(() => import('@/pages/terms'));
const PrivacyPolicyPage = lazy(() => import('@/pages/privacyPolicy'));
const DeleteAccountPage = lazy(() => import('@/pages/deleteAccount'));

export const router = createBrowserRouter(
  createRoutesFromElements(
    <Route element={<Outlet />}>
      <Route
        path={ROUTES.TERMS}
        element={
          <Suspense fallback={<StaticPageSkeleton />}>
            <TermsPage />
          </Suspense>
        }
      />
      <Route
        path={ROUTES.PRIVACY_POLICY}
        element={
          <Suspense fallback={<StaticPageSkeleton />}>
            <PrivacyPolicyPage />
          </Suspense>
        }
      />
      <Route
        path={ROUTES.DELETE_ACCOUNT}
        element={
          <Suspense fallback={<StaticPageSkeleton />}>
            <DeleteAccountPage />
          </Suspense>
        }
      />
      <Route path={ROUTES.ROOT} element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route
          path={ROUTES.SIGNUP}
          element={
            <Suspense fallback={<FormPageSkeleton />}>
              <SignupPage />
            </Suspense>
          }
        />
        <Route path={ROUTES.LOGIN} element={<LoginPage />} />
        <Route
          path={ROUTES.GOOGLE_CALLBACK}
          element={
            <Suspense fallback={null}>
              <GoogleCallbackPage />
            </Suspense>
          }
        />
        <Route
          path={ROUTES.FIND_PASSWORD}
          element={
            <Suspense fallback={<FormPageSkeleton />}>
              <FindPasswordPage />
            </Suspense>
          }
        />
        <Route
          path={ROUTES.NEW_PASSWORD}
          element={
            <Suspense fallback={<FormPageSkeleton />}>
              <NewPasswordPage />
            </Suspense>
          }
        />

        <Route element={<ProtectedRoute />}>
          <Route path={ROUTES.TODAY_MOMENT} element={<TodayMomentPage />} />
          <Route path={ROUTES.TODAY_COMMENT} element={<TodayCommentPage />} />
          <Route path={ROUTES.TODAY_MOMENT_SUCCESS} element={<TodayMomentSuccessPage />} />

          <Route path={ROUTES.COLLECTION_MYMOMENT} element={<MyMomentCollectionPage />} />
          <Route path={ROUTES.COLLECTION_MYCOMMENT} element={<MyCommentCollectionPage />} />

          <Route path={ROUTES.MY} element={<MyPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Route>,
  ),
);
