import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { toast } from '@/shared/store/toast';
import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router';
import { ROUTES } from './routes';
import { AxiosError } from 'axios';
import styled from '@emotion/styled';

const LoadingContainer = styled.div`
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
`;

export const ProtectedRoute: React.FC = () => {
  const location = useLocation();
  const { data: isLoggedIn, isLoading, isError, error } = useCheckIfLoggedInQuery();

  if (isLoading) {
    return <LoadingContainer />;
  }

  if (isLoggedIn === false || (isError && (error as AxiosError)?.response?.status === 401)) {
    toast.warning('Moment에 오신 걸 환영해요! 로그인하고 시작해보세요 💫', 3000);
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  return <Outlet />;
};
