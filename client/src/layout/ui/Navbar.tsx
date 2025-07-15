import { LoginButton } from '@/components/auth/LoginButton';
import * as S from './Navbar.styles';

export const Navbar = () => {
  return (
    <S.Navbar>
      <S.LogoContainer>
        <S.Logo src="/logo.webp" alt="logo" />
      </S.LogoContainer>
      <LoginButton />
    </S.Navbar>
  );
};
