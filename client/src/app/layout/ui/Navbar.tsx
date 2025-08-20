import { LEVEL_MAP, navItems } from '@/app/layout/data/navItems';
import { useProfileQuery } from '@/features/auth/api/useProfileQuery';
import { AuthButton } from '@/features/auth/ui/AuthButton';
import { useOutsideClick } from '@/shared/hooks/useOutsideClick';
import { useToggle } from '@/shared/hooks/useToggle';
import { sendEvent } from '@/shared/lib/ga';
import { Logo } from '@/shared/ui/logo/Logo';
import { NavigatorsBar } from '@/widgets/navigatorsBar';

import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useNotificationsQuery } from '@/features/notification/hooks/useNotificationsQuery';
import { HomePageAnalyticsEvent } from '@/shared/lib/ga/analyticsEvent';
import { EXPBar } from '@/widgets/EXPBar/EXPBar';
import { useRef } from 'react';
import { Link, useLocation } from 'react-router';
import * as S from './Navbar.styles';

export type Level = 'METEOR' | 'ASTEROID' | 'COMET';

export const Navbar = () => {
  const location = useLocation();
  const currentPath = location.pathname;
  const isHomePage = currentPath === '/';
  const { data: isLoggedIn, isError, error } = useCheckIfLoggedInQuery();
  const { data: profile } = useProfileQuery({ enabled: isLoggedIn ?? false });
  const { isOpen: isMobileMenuOpen, toggle: toggleMobileMenu } = useToggle(false);
  const mobileMenuRef = useRef<HTMLDivElement>(null);
  const dropdownButtonRef = useRef<HTMLButtonElement>(null);
  const { data: notifications } = useNotificationsQuery();

  const isNotificationExisting =
    notifications?.data.length && notifications?.data.length > 0 ? true : false;

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
    sendEvent(HomePageAnalyticsEvent.ClickDesktopAuthButton);
  };

  const handleMobileAuthButtonClick = () => {
    toggleMobileMenu();
    sendEvent(HomePageAnalyticsEvent.ClickMobileAuthButton);
  };

  const expStar = profile?.expStar ?? 0;
  const nextStepExp = profile?.nextStepExp ?? 0;
  const EXPBarProgress = (expStar / (nextStepExp + expStar)) * 100;

  return (
    <S.Navbar>
      <Logo />

      <S.DesktopNavItems>{!isHomePage && <NavigatorsBar $isNavBar={true} />}</S.DesktopNavItems>

      <S.DesktopAuthButton>
        {profile?.level ? (
          <S.LevelIconWrapper>
            <S.LevelIcon
              src={LEVEL_MAP[profile?.level as keyof typeof LEVEL_MAP]}
              alt="레벨 등급표"
            />
            <S.EXPBarTooltip>
              <EXPBar progress={EXPBarProgress} />
            </S.EXPBarTooltip>
          </S.LevelIconWrapper>
        ) : null}
        <AuthButton onClick={handleDesktopAuthButtonClick} profile={profile} />
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
              <S.MobileNavItem
                key={item.href}
                $isActive={currentPath === item.href}
                $shadow={item.label === '나만의 모음집' && isNotificationExisting}
              >
                <Link to={item.href} onClick={toggleMobileMenu}>
                  {item.label}
                </Link>
              </S.MobileNavItem>
            ))}
            <AuthButton onClick={handleMobileAuthButtonClick} profile={profile} />
          </S.MobileNavItems>
        </S.MobileMenuContent>
      </S.MobileMenu>
    </S.Navbar>
  );
};
