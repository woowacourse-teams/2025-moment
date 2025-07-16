import { LoginButton } from '@/features/auth/ui/LoginButton';
import { Logo } from '@/shared/ui/Logo';
import * as S from './Navbar.styles';

export const Navbar = () => {
  return (
    <S.Navbar>
      <Logo />
      <LoginButton />
    </S.Navbar>
  );
};
