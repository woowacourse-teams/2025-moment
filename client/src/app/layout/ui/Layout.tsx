import { Navbar } from '@/app/layout/ui/Navbar';
import { StarField } from '@/app/layout/ui/StarField';
import { useSSENotifications } from '@/features/notification/hooks/useSSENotifications';
import { Toast } from '@/shared/ui/toast';
import { initGA, sendPageview } from '@/shared/lib/ga';
import { ErrorBoundary } from '@/shared/ui';
import React, { useEffect } from 'react';
import { Outlet, useLocation } from 'react-router';
import * as S from './Layout.styles';
import { Footer } from './Footer';

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
  useSSENotifications();

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
      <Footer />
      <Toast />
    </S.Wrapper>
  );
};

export const Layout: React.FC = () => {
  return <LayoutContent />;
};
