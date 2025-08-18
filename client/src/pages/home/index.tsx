import { ROUTES } from '@/app/routes/routes';
import { sendEvent } from '@/shared/lib/ga';
import { Button } from '@/shared/ui/button/Button';
import { Hero } from '@/widgets/hero';
import { useDelayedVisible } from '@/shared/hooks/useDelayedVisible';
import { useNavigate } from 'react-router';
import * as S from './index.styles';
import { useState } from 'react';
import { NavigatorsBar } from '@/widgets/navigatorsBar';
import { HomePageAnalyticsEvent } from '@/shared/lib/ga/analyticsEvent';

export default function HomePage() {
  const [isWidgetOpen, setIsWidgetOpen] = useState(false);

  const navigate = useNavigate();
  const { isVisible } = useDelayedVisible({ delay: 100 });

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
      <S.BlackHoleContainer>
        <S.BlackHoleText>click me</S.BlackHoleText>
        <button onClick={handleWidgetClick}>
          <S.BlackHoleImage src="/images/blackHole.png" alt="네비게이션 메뉴 열기" />
        </button>
      </S.BlackHoleContainer>
      <S.WidgetContainer isWidgetOpen={isWidgetOpen}>
        <NavigatorsBar $isNavBar={false} />
      </S.WidgetContainer>
    </S.HomePageWrapper>
  );
}
