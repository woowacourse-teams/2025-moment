import { useProfileQuery } from '@/features/auth/api/useProfileQuery';
import { AuthButton } from '@/features/auth/ui/AuthButton';
import { Logo } from '@/shared/ui/logo/Logo';
import { NavigatorsBar } from '@/widgets/navigatorsBar';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useEffect } from 'react';
import { useLocation } from 'react-router';
import { toast } from '@/shared/store/toast';
import { isApp } from '@/shared/utils/device';
import * as S from './Navbar.styles';
import { track } from '@/shared/lib/ga/track';

export const Navbar = () => {
  const location = useLocation();
  if (isApp()) return null;

  const currentPath = location.pathname;
  const isHomePage = currentPath === '/';
  const { data: isLoggedIn, isError, error } = useCheckIfLoggedInQuery();
  const {
    data: profile,
    isLoading: isProfileLoading,
    isError: isProfileError,
  } = useProfileQuery({ enabled: isLoggedIn ?? false });

  if (isError) {
    console.error('checkIfLoggedInQuery error', error);
  }

  useEffect(() => {
    if (isProfileError && isLoggedIn) {
      toast.error('사용자 정보를 불러오지 못했습니다. 다시 로그인해 주세요.');
    }
  }, [isProfileError, isLoggedIn]);

  const handleDesktopAuthButtonClick = () => {
    track('click_auth', { device: 'desktop' });
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
        ) : null}
        <AuthButton
          onClick={handleDesktopAuthButtonClick}
          profile={isProfileError ? undefined : profile}
        />
      </S.DesktopAuthButton>
    </S.Navbar>
  );
};
