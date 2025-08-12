import { levelMap, navItems } from '@/app/layout/data/navItems';
import { useProfileQuery } from '@/features/auth/hooks/useProfileQuery';
import { AuthButton } from '@/features/auth/ui/AuthButton';
import { useOutsideClick } from '@/shared/hooks/useOutsideClick';
import { useToggle } from '@/shared/hooks/useToggle';
import { Logo } from '@/shared/ui/logo/Logo';

import { useCheckIfLoggedInQuery } from '@/features/auth/hooks/useCheckIfLoggedInQuery';
import { sendEvent } from '@/shared/lib/ga';
import { NavigatorsBar } from '@/widgets/navigatorsBar';
import { useRef } from 'react';
import { Link, useLocation } from 'react-router';
import * as S from './Navbar.styles';

type Level = 'METEOR' | 'ASTEROID' | 'COMET';

export const Navbar = () => {
  const location = useLocation();
  const currentPath = location.pathname;
  const isHomePage = currentPath === '/';
  const { data: isLoggedIn, isError, error } = useCheckIfLoggedInQuery();
  const { data: profile } = useProfileQuery({ enabled: isLoggedIn ?? false });
  const { isOpen: isMobileMenuOpen, toggle: toggleMobileMenu } = useToggle(false);
  const mobileMenuRef = useRef<HTMLDivElement>(null);
  const dropdownButtonRef = useRef<HTMLButtonElement>(null);

  if (isError) {
    console.error('checkIfLoggedInQuery error', error);
  }

  useOutsideClick({
    ref: mobileMenuRef,
    callback: () => toggleMobileMenu(),
    isActive: isMobileMenuOpen,
    excludeRefs: [dropdownButtonRef],
  });

  const handleDesktopAuthButtonClick = () => {
    sendEvent({
      category: 'HomePage',
      action: 'Click Desktop Auth Button',
      label: 'Desktop Auth Button',
    });
  };

  const handleMobileAuthButtonClick = () => {
    toggleMobileMenu();
    sendEvent({
      category: 'HomePage',
      action: 'Click Mobile Auth Button',
      label: 'Mobile Auth Button',
    });
  };

  return (
    <S.Navbar>
      <Logo />

      <S.DesktopNavItems>{!isHomePage && <NavigatorsBar $isNavBar={true} />}</S.DesktopNavItems>

      <S.DesktopAuthButton>
        {profile?.level && <S.LevelIcon src={levelMap[profile?.level as Level]} alt="level" />}
        <AuthButton onClick={handleDesktopAuthButtonClick} />
      </S.DesktopAuthButton>

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
            <AuthButton onClick={handleMobileAuthButtonClick} />
          </S.MobileNavItems>
        </S.MobileMenuContent>
      </S.MobileMenu>
    </S.Navbar>
  );
};
