import { navItems } from '@/app/layout/types/navItems';
import { LoginButton } from '@/features/auth/ui/LoginButton';
import { useOutsideClick } from '@/shared/hooks/useOutsideClick';
import { useToggle } from '@/shared/hooks/useToggle';
import { Logo } from '@/shared/ui/logo/Logo';
import { useRef } from 'react';
import { Link, useLocation } from 'react-router';
import * as S from './Navbar.styles';

export const Navbar = () => {
  const location = useLocation();
  const currentPath = location.pathname;
  const { isOpen: isMobileMenuOpen, toggle: toggleMobileMenu } = useToggle(false);
  const mobileMenuRef = useRef<HTMLDivElement>(null);
  const dropdownButtonRef = useRef<HTMLButtonElement>(null);

  useOutsideClick({
    ref: mobileMenuRef,
    callback: () => toggleMobileMenu(),
    isActive: isMobileMenuOpen,
    excludeRefs: [dropdownButtonRef],
  });

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

      <S.DropdownButton
        ref={dropdownButtonRef}
        onClick={toggleMobileMenu}
        $isOpen={isMobileMenuOpen}
      >
        {isMobileMenuOpen ? '✕' : '☰'}
      </S.DropdownButton>

      <S.MobileMenu ref={mobileMenuRef} $isOpen={isMobileMenuOpen}>
        <S.MobileMenuContent>
          <S.MobileNavItems>
            {navItems.map(item => (
              <S.MobileNavItem key={item.href} $isActive={currentPath === item.href}>
                <Link to={item.href} onClick={toggleMobileMenu}>
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
