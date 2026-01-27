import { Navbar } from '@/app/layout/ui/Navbar';
import { BottomNavbar } from '@/app/layout/ui/BottomNavbar';
import { StarField } from '@/app/layout/ui/StarField';
import { useSSENotifications } from '@/features/notification/hooks/useSSENotifications';
import { Toast } from '@/shared/ui/toast';
import { initGA, sendPageview } from '@/shared/lib/ga';
import React, { useEffect } from 'react';
import { Outlet, useLocation } from 'react-router';
import * as S from './Layout.styles';
import { Footer } from './Footer';
import { ErrorBoundary } from '@/shared/ui/errorBoundary';

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
      <BottomNavbar />
      <Footer />
      <Toast />
    </S.Wrapper>
  );
};

export const Layout: React.FC = () => {
  return <LayoutContent />;
};
