import { useCheckIfLoggedInQuery } from '@/features/auth/hooks/useCheckIfLoggedInQuery';
import { useToast } from '@/shared/hooks';
import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router';
import { ROUTES } from './routes';

export const ProtectedRoute: React.FC = () => {
  const location = useLocation();
  const { showError } = useToast();
  const { data: isLoggedIn, isLoading, isError } = useCheckIfLoggedInQuery();

  if (isLoading) {
    return null;
  }

  if (isError || isLoggedIn === false) {
    showError('로그인 후 이용해주세요.', 3000);
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  return <Outlet />;
};
