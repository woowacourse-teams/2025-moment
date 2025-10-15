import { ROUTES } from '@/app/routes/routes';
import { useDelayedVisible } from '@/shared/hooks/useDelayedVisible';
import { sendEvent } from '@/shared/lib/ga';
import { HomePageAnalyticsEvent } from '@/shared/lib/ga/analyticsEvent';
import { Button } from '@/shared/ui/button/Button';
import { Hero } from '@/widgets/hero';
import { useState } from 'react';
import { useNavigate } from 'react-router';
import * as S from './index.styles';
import { useScrollAnimation } from '@/shared/hooks/useScrollAnimation';

export default function HomePage() {
  const navigate = useNavigate();
  const { isVisible } = useDelayedVisible({ delay: 100 });
  const [visibleItems, setVisibleItems] = useState({
    intro1: false,
    intro2: false,
    intro3: false,
  });

  const intro1Ref = useScrollAnimation({
    onVisible: () => setVisibleItems(prev => ({ ...prev, intro1: true })),
  });

  const intro2Ref = useScrollAnimation({
    onVisible: () => setVisibleItems(prev => ({ ...prev, intro2: true })),
  });

  const intro3Ref = useScrollAnimation({
    onVisible: () => setVisibleItems(prev => ({ ...prev, intro3: true })),
  });

  const handleClick = () => {
    sendEvent(HomePageAnalyticsEvent.ClickTodayMomentButton);
    navigate(ROUTES.TODAY_MOMENT);
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
        <S.IntroTextWrapper ref={intro1Ref} isVisible={visibleItems.intro1}>
          <S.IntroImage src="/images/spaceman.png" alt="" />
          <S.IntroText>익명 속에서, 편안하게 마음을 나눌 수 있는 곳. </S.IntroText>
        </S.IntroTextWrapper>
        <S.IntroTextWrapper ref={intro2Ref} isVisible={visibleItems.intro2}>
          <S.IntroImage src="/images/rocket.png" alt="" />
          <S.IntroText>비교 대신, 따뜻한 공감이 오가는 공간.</S.IntroText>
        </S.IntroTextWrapper>
        <S.IntroTextWrapper ref={intro3Ref} isVisible={visibleItems.intro3}>
          <S.IntroImage src="/images/paperAirplane.png" alt="" />
          <S.IntroText>나의 소소한 이야기가 누군가에게 공감으로 닿는 순간.</S.IntroText>
        </S.IntroTextWrapper>
      </S.IntroSection>
    </S.HomePageWrapper>
  );
}
