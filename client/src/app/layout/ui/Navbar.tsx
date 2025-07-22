import { navItems } from '@/app/layout/types/navItems';
import { LoginButton } from '@/features/auth/ui/LoginButton';
import { Logo } from '@/shared/ui/logo/Logo';
import { Link, useLocation } from 'react-router';
import * as S from './Navbar.styles';

export const Navbar = () => {
  const location = useLocation();
  const currentPath = location.pathname;

  return (
    <S.Navbar>
      <Logo />
      <S.NavItems>
        {navItems.map(item => (
          <S.NavItem key={item.href} $isActive={currentPath === item.href}>
            <Link to={item.href}>{item.label}</Link>
          </S.NavItem>
        ))}
      </S.NavItems>
      <LoginButton />
    </S.Navbar>
  );
};
