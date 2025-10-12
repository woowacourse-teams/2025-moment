import { ROUTES } from '@/app/routes/routes';
import { useOutsideClick } from '@/shared/hooks';
import { useDelayedVisible } from '@/shared/hooks/useDelayedVisible';
import { sendEvent } from '@/shared/lib/ga';
import { HomePageAnalyticsEvent } from '@/shared/lib/ga/analyticsEvent';
import { Button } from '@/shared/ui/button/Button';
import { LazyImage } from '@/shared/ui/lazyImage/LazyImage';
import { Hero } from '@/widgets/hero';
import { NavigatorsBar } from '@/widgets/navigatorsBar';
import { useRef, useState } from 'react';
import { useNavigate } from 'react-router';
import * as S from './index.styles';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';

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
      <S.MainContainer>
        <S.HeroSection>
          <Hero />
        </S.HeroSection>
        <S.ContentSection isVisible={isVisible}>
          <Button title="모멘트 작성하기" variant="secondary" onClick={handleClick} />
        </S.ContentSection>
      </S.MainContainer>
      <S.IntroSection>
        <S.IntroTextWrapper>
          <S.IntroImage src="/images/character.webp" alt="" />
          <S.IntroText>익명 속에서, 편안하게 마음을 나눌 수 있는 곳. </S.IntroText>
        </S.IntroTextWrapper>
        <S.IntroTextWrapper>
          <S.IntroImage src="/images/character.webp" alt="" />
          <S.IntroText>비교 대신, 따뜻한 공감이 오가는 공간.</S.IntroText>
        </S.IntroTextWrapper>
        <S.IntroTextWrapper>
          <S.IntroImage src="/images/character.webp" alt="" />
          <S.IntroText>나의 소소한 이야기가 누군가에게 공감으로 닿는 순간.</S.IntroText>
        </S.IntroTextWrapper>
      </S.IntroSection>
      {isLoggedIn && (
        <>
          <S.BlackHoleContainer ref={ClickMeRef}>
            <S.BlackHoleText>click me</S.BlackHoleText>
            <button onClick={handleWidgetClick}>
              <LazyImage
                src="/images/blackHole.webp"
                alt="네비게이션 `메뉴 열기"
                variant="blackHole"
                width="150px"
                height="150px"
                borderRadius="50%"
              />
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
