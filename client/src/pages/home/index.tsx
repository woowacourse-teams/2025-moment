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
      <S.IntroTextWrapper ref={intro1Ref} isVisible={visibleItems.intro1}>
        <S.IntroSection>
          <S.IntroLogo src="/images/momentLogo.png" alt="" />
          "모멘트(Moment)"는 사용자들이 삶의 모든 순간(Moment)을 공유하며 서로에게 따뜻한 칭찬과
          위로를 건네는 소셜 네트워크 서비스입니다. <br />
          힘든 순간, 뿌듯한 순간, 위로받고 싶은 순간, 칭찬받고 싶은 모든 순간을 짧은 기록으로
          남기고, 긍정적인 상호작용을 통해 정서적 지지와 유대감을 나눌 수 있는 공간을 제공합니다.
          <S.IntroImagesWrapper>
            <S.IntroImage src="/images/rocket.png" alt="" />
            <S.IntroImage src="/images/paperAirplane.png" alt="" />
            <S.IntroImage src="/images/spaceman.png" alt="" />
          </S.IntroImagesWrapper>
        </S.IntroSection>
      </S.IntroTextWrapper>
    </S.HomePageWrapper>
  );
}
