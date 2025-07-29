import { useProfileQuery } from '@/features/auth/hooks/useProfileQuery';
import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router';
import { ROUTES } from './routes';

export const ProtectedRoute: React.FC = () => {
  const { data: profile, isLoading, isError } = useProfileQuery();
  const location = useLocation();

  if (isLoading) {
    return null;
  }

  if (isError || !profile) {
    alert('로그인 후 이용해주세요.');
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  return <Outlet />;
};
