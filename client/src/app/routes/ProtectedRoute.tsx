import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useToast } from '@/shared/hooks';
import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router';
import { ROUTES } from './routes';

export const ProtectedRoute: React.FC = () => {
  const location = useLocation();
  const { showWarning } = useToast();
  const { data: isLoggedIn, isLoading, isError } = useCheckIfLoggedInQuery();

  if (isLoading) {
    return null;
  }

  if (isError || isLoggedIn === false) {
    showWarning('Moment에 오신 걸 환영해요! 로그인하고 시작해보세요 💫', 3000);
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  return <Outlet />;
};
