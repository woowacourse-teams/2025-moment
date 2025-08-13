import { useCheckIfLoggedInQuery } from '@/features/auth/hooks/useCheckIfLoggedInQuery';
import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router';
import { ROUTES } from './routes';

export const ProtectedRoute: React.FC = () => {
  const location = useLocation();
  const { data: isLoggedIn } = useCheckIfLoggedInQuery();

  if (!isLoggedIn) {
    alert('로그인 후 이용해주세요.');
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  return <Outlet />;
};
