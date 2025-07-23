import { navItems } from '@/app/layout/types/navItems';
import { LoginButton } from '@/features/auth/ui/LoginButton';
import { Logo } from '@/shared/ui/logo/Logo';
import { useState } from 'react';
import { Link, useLocation } from 'react-router';
import * as S from './Navbar.styles';

export const Navbar = () => {
  const location = useLocation();
  const currentPath = location.pathname;
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  const closeMobileMenu = () => {
    setIsMobileMenuOpen(false);
  };

  return (
    <S.Navbar>
      <Logo />

      <S.DesktopNavItems>
        {navItems.map(item => (
          <S.NavItem key={item.href} $isActive={currentPath === item.href}>
            <Link to={item.href}>{item.label}</Link>
          </S.NavItem>
        ))}
      </S.DesktopNavItems>

      <S.DesktopLoginButton>
        <LoginButton />
      </S.DesktopLoginButton>

      <S.HamburgerButton onClick={toggleMobileMenu} $isOpen={isMobileMenuOpen}>
        {isMobileMenuOpen ? '✕' : '☰'}
      </S.HamburgerButton>

      <S.MobileMenu $isOpen={isMobileMenuOpen}>
        <S.MobileMenuContent>
          <S.MobileNavItems>
            {navItems.map(item => (
              <S.MobileNavItem key={item.href} $isActive={currentPath === item.href}>
                <Link to={item.href} onClick={closeMobileMenu}>
                  {item.label}
                </Link>
              </S.MobileNavItem>
            ))}
            <LoginButton />
          </S.MobileNavItems>
        </S.MobileMenuContent>
      </S.MobileMenu>
    </S.Navbar>
  );
};
