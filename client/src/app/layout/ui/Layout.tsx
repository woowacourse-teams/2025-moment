import { Navbar } from '@/app/layout/ui/Navbar';
import { StarField } from '@/app/layout/ui/StarField';
import React from 'react';
import { Outlet } from 'react-router';
import * as S from './Layout.styles';

export const Layout: React.FC = () => {
  return (
    <S.Wrapper>
      <Navbar />
      <S.Main>
        <StarField starCount={50} />
        <Outlet />
      </S.Main>
    </S.Wrapper>
  );
};
