import { ROUTES } from '@/app/routes/routes';
import { useNotificationsQuery } from '@/features/notification/hooks/useNotificationsQuery';
import { useDelayedVisible } from '@/shared/hooks/useDelayedVisible';
import { sendEvent } from '@/shared/lib/ga';
import { Button } from '@/shared/ui/button/Button';
import { Hero } from '@/widgets/hero';
import { NavigatorsBar } from '@/widgets/navigatorsBar';
import { useState } from 'react';
import { useNavigate } from 'react-router';
import * as S from './index.styles';

export default function HomePage() {
  const [isWidgetOpen, setIsWidgetOpen] = useState(false);
  const { data: notifications } = useNotificationsQuery();

  if (!notifications) {
    return null;
  }
  const isNotificationExisting = notifications?.data.length > 0;

  const navigate = useNavigate();
  const { isVisible } = useDelayedVisible({ delay: 100 });

  const handleClick = () => {
    sendEvent({
      category: 'HomePage',
      action: 'Click TodayMomentButton',
      label: 'TodayMomentButton',
    });
    navigate(ROUTES.TODAY_MOMENT);
  };

  const handleWidgetClick = () => {
    sendEvent({
      category: 'HomePage',
      action: 'Click BlackHole Button',
      label: 'BlackHole Button',
    });
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
      <S.ClickMeContainer isWidgetOpen={isWidgetOpen} $shadow={isNotificationExisting}>
        <NavigatorsBar $isNavBar={false} />
      </S.ClickMeContainer>
    </S.HomePageWrapper>
  );
}
