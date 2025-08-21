import { Navbar } from '@/app/layout/ui/Navbar';
import { StarField } from '@/app/layout/ui/StarField';
import { setToastFunctions } from '@/app/lib/api';
import { useSSENotifications } from '@/features/notification/hooks/useSSENotifications';
import { ToastProvider } from '@/shared/context/toast/ToastProvider';
import { useToast } from '@/shared/hooks/useToast';
import { initGA, sendPageview } from '@/shared/lib/ga';
import { ErrorBoundary } from '@/shared/ui';
import React, { useEffect } from 'react';
import { Outlet, useLocation } from 'react-router';
import * as S from './Layout.styles';

const GATracker = () => {
  const location = useLocation();

  useEffect(() => {
    initGA();
  }, []);

  useEffect(() => {
    sendPageview(location.pathname + location.search);
  }, [location]);

  return null;
};

const LayoutContent: React.FC = () => {
  const { showError } = useToast();

  useSSENotifications();

  useEffect(() => {
    setToastFunctions(showError);
  }, [showError]);

  return (
    <S.Wrapper>
      <GATracker />
      <Navbar />
      <S.Main>
        <StarField starCount={50} />
        <ErrorBoundary>
          <Outlet />
        </ErrorBoundary>
      </S.Main>
    </S.Wrapper>
  );
};

export const Layout: React.FC = () => {
  return (
    <ToastProvider>
      <LayoutContent />
    </ToastProvider>
  );
};
