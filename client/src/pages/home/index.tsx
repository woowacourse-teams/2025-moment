import { ROUTES } from '@/app/routes/routes';
import { useNotificationsQuery } from '@/features/notification/hooks/useNotificationsQuery';
import { useDelayedVisible } from '@/shared/hooks/useDelayedVisible';
import { sendEvent } from '@/shared/lib/ga';
import { HomePageAnalyticsEvent } from '@/shared/lib/ga/analyticsEvent';
import { Button } from '@/shared/ui/button/Button';
import { Hero } from '@/widgets/hero';
import { NavigatorsBar } from '@/widgets/navigatorsBar';
import { useState } from 'react';
import { useNavigate } from 'react-router';
import * as S from './index.styles';

export default function HomePage() {
  const [isWidgetOpen, setIsWidgetOpen] = useState(false);
  const { data: notifications } = useNotificationsQuery();
  const navigate = useNavigate();
  const { isVisible } = useDelayedVisible({ delay: 100 });

  if (!notifications) {
    return null;
  }
  const isNotificationExisting = notifications?.data.length > 0;

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
          <S.BlackHoleImage src="/blackHole.png" alt="네비게이션 메뉴 열기" />
        </button>
      </S.BlackHoleContainer>
      <S.ClickMeContainer isWidgetOpen={isWidgetOpen}>
        <NavigatorsBar $isNavBar={false} />
      </S.ClickMeContainer>
    </S.HomePageWrapper>
  );
}
