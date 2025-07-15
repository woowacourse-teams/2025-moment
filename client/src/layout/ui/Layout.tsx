import { Navbar } from '@/layout/ui/Navbar';
import { StarField } from '@/layout/ui/StarField';
import React from 'react';
import { Outlet } from 'react-router';
import * as S from './Layout.styles';

export const Layout: React.FC = () => {
  return (
    <S.Wrapper>
      <StarField starCount={50} />
      <Navbar />
      <S.Main>
        <Outlet />
      </S.Main>
    </S.Wrapper>
  );
};
