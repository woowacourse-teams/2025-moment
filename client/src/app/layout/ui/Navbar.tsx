import { LEVEL_MAP, navItems } from '@/app/layout/data/navItems';
import { useProfileQuery } from '@/features/auth/api/useProfileQuery';
import { AuthButton } from '@/features/auth/ui/AuthButton';
import { useOutsideClick } from '@/shared/hooks/useOutsideClick';
import { useToggle } from '@/shared/hooks/useToggle';
import { Logo } from '@/shared/ui/logo/Logo';
import { NavigatorsBar } from '@/widgets/navigatorsBar';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useReadNotificationsQuery } from '@/features/notification/api/useReadNotificationsQuery';
import { useEffect, useRef } from 'react';
import { Link, useLocation } from 'react-router';
import { useToast } from '@/shared/hooks/useToast';
import * as S from './Navbar.styles';
import { ROUTES } from '@/app/routes/routes';
import { track } from '@/shared/lib/ga/track';

export type Level = 'METEOR' | 'ASTEROID' | 'COMET';

export const Navbar = () => {
  const location = useLocation();
  const currentPath = location.pathname;
  const isHomePage = currentPath === '/';
  const { showError } = useToast();
  const { data: isLoggedIn, isError, error } = useCheckIfLoggedInQuery();
  const {
    data: profile,
    isLoading: isProfileLoading,
    isError: isProfileError,
  } = useProfileQuery({ enabled: isLoggedIn ?? false });
  const { isOpen: isMobileMenuOpen, toggle: toggleMobileMenu } = useToggle(false);
  const mobileMenuRef = useRef<HTMLDivElement>(null);
  const dropdownButtonRef = useRef<HTMLButtonElement>(null);
  const { data: notifications } = useReadNotificationsQuery();

  const isNotificationExisting =
    notifications?.data.length && notifications?.data.length > 0 ? true : false;

  if (isError) {
    console.error('checkIfLoggedInQuery error', error);
  }

  useEffect(() => {
    if (isProfileError && isLoggedIn) {
      showError('사용자 정보를 불러오지 못했습니다. 다시 로그인해 주세요.');
    }
  }, [isProfileError, isLoggedIn, showError]);

  useOutsideClick({
    ref: mobileMenuRef,
    callback: () => toggleMobileMenu(),
    isActive: isMobileMenuOpen,
    excludeRefs: [dropdownButtonRef],
  });

  const handleDesktopAuthButtonClick = () => {
    track('click_auth', { device: 'desktop' });
  };

  const handleMobileAuthButtonClick = () => {
    track('click_auth', { device: 'mobile' });
    toggleMobileMenu();
  };

  const isActiveNavItem = (href: string) => {
    if (href === ROUTES.TODAY_MOMENT) {
      return currentPath.startsWith('/today-moment');
    }
    return currentPath === href;
  };

  return (
    <S.Navbar>
      <Logo />

      <S.DesktopNavItems>{!isHomePage && <NavigatorsBar $isNavBar={true} />}</S.DesktopNavItems>

      <S.DesktopAuthButton>
        {isProfileLoading && isLoggedIn ? (
          <S.LevelIconWrapper>
            <S.LoadingSkeleton />
          </S.LevelIconWrapper>
        ) : profile?.level ? (
          <S.LevelIconWrapper>
            <S.LevelIcon
              src={LEVEL_MAP[profile?.level as keyof typeof LEVEL_MAP]}
              alt="레벨 등급표"
            />
          </S.LevelIconWrapper>
        ) : null}
        <AuthButton
          onClick={handleDesktopAuthButtonClick}
          profile={isProfileError ? undefined : profile}
        />
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
                $isActive={isActiveNavItem(item.href)}
                $shadow={item.label === '나만의 모음집' && isNotificationExisting}
              >
                <Link to={item.href} onClick={toggleMobileMenu}>
                  <span>{item.label}</span>
                </Link>
              </S.MobileNavItem>
            ))}
            <AuthButton
              onClick={handleMobileAuthButtonClick}
              profile={isProfileError ? undefined : profile}
            />
          </S.MobileNavItems>
        </S.MobileMenuContent>
      </S.MobileMenu>
    </S.Navbar>
  );
};
