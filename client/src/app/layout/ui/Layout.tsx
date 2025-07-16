import { Navbar } from '@/app/layout/ui/Navbar.styles';
import React from 'react';
import { Outlet } from 'react-router';
import * as S from './Layout.styles';

export const Layout: React.FC = () => {
  return (
    <S.Wrapper>
      <Navbar />
      <S.Main>
        <Outlet />
      </S.Main>
    </S.Wrapper>
  );
};
