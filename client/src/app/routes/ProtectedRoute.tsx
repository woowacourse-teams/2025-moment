import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useToast } from '@/shared/hooks';
import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router';
import { ROUTES } from './routes';
import { AxiosError } from 'axios';

export const ProtectedRoute: React.FC = () => {
  const location = useLocation();
  const { showError } = useToast();
  const { data: isLoggedIn, isLoading, isError, error } = useCheckIfLoggedInQuery();

  if (isLoading) {
    return null;
  }

  if (isLoggedIn === false || (isError && (error as AxiosError)?.response?.status === 401)) {
    showError('로그인 후 이용해주세요.', 3000);
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  return <Outlet />;
};
