import { ROUTES } from '@/app/routes/routes';
import { useOutsideClick } from '@/shared/hooks';
import { useDelayedVisible } from '@/shared/hooks/useDelayedVisible';
import { sendEvent } from '@/shared/lib/ga';
import { HomePageAnalyticsEvent } from '@/shared/lib/ga/analyticsEvent';
import { Button } from '@/shared/ui/button/Button';
import { Hero } from '@/widgets/hero';
import { NavigatorsBar } from '@/widgets/navigatorsBar';
import { useRef, useState } from 'react';
import { useNavigate } from 'react-router';
import * as S from './index.styles';
import { useCheckIfLoggedInQuery } from '@/features/auth/hooks/useCheckIfLoggedInQuery';

export default function HomePage() {
  const [isWidgetOpen, setIsWidgetOpen] = useState(false);
  const navigate = useNavigate();
  const { isVisible } = useDelayedVisible({ delay: 100 });
  const { data: isLoggedIn } = useCheckIfLoggedInQuery();
  const ClickMeRef = useRef<HTMLDivElement | null>(null);
  const BlackHoleRef = useRef<HTMLDivElement | null>(null);

  useOutsideClick({
    ref: BlackHoleRef,
    callback: () => {
      setIsWidgetOpen(false);
    },
    isActive: isWidgetOpen,
    excludeRefs: [ClickMeRef],
  });

  const handleClick = () => {
    sendEvent(HomePageAnalyticsEvent.ClickTodayMomentButton);
    navigate(ROUTES.TODAY_MOMENT);
  };

  const handleWidgetClick = () => {
    sendEvent(HomePageAnalyticsEvent.ClickBlackHoleButton);
    setIsWidgetOpen(!isWidgetOpen);
  };

  return (
    <S.HomePageWrapper>
      <S.HeroSection>
        <Hero />
      </S.HeroSection>
      <S.ContentSection isVisible={isVisible}>
        <Button title="모멘트 작성하기" variant="secondary" onClick={handleClick} />
      </S.ContentSection>
      {isLoggedIn && (
        <>
          <S.BlackHoleContainer ref={ClickMeRef}>
            <S.BlackHoleText>click me</S.BlackHoleText>
            <button onClick={handleWidgetClick}>
              <S.BlackHoleImage src="/images/blackHole.png" alt="네비게이션 `메뉴 열기" />
            </button>
          </S.BlackHoleContainer>
          <S.ClickMeContainer ref={BlackHoleRef} isWidgetOpen={isWidgetOpen}>
            <NavigatorsBar $isNavBar={false} />
          </S.ClickMeContainer>
        </>
      )}
    </S.HomePageWrapper>
  );
}
