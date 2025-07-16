import { Button } from '@/shared/ui/Button';
import { Logo } from '@/shared/ui/Logo';
import * as S from './Navbar.styles';

export const Navbar = () => {
  return (
    <S.Navbar>
      <Logo />
      <Button title="Sign Up" />
    </S.Navbar>
  );
};
