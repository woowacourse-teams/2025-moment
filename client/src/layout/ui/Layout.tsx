import React from 'react';
import { Outlet } from 'react-router';
import * as S from './Layout.styles';

export const Layout: React.FC = () => {
  return (
    <S.Wrapper>
      <S.Navbar>navbar</S.Navbar>
      <S.Main>
        <Outlet />
      </S.Main>
    </S.Wrapper>
  );
};
