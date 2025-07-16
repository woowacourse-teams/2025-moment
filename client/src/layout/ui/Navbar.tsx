import { LoginButton } from '@/components/auth/LoginButton';
import { Logo } from '@/components/logo';
import * as S from './Navbar.styles';

export const Navbar = () => {
  return (
    <S.Navbar>
      <Logo />
      <LoginButton />
    </S.Navbar>
  );
};
