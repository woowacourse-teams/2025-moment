import { Navbar } from '@/app/layout/ui/Navbar';
import { StarField } from '@/app/layout/ui/StarField';
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

export const Layout: React.FC = () => {
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
