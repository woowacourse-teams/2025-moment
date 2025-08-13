import { ROUTES } from '@/app/routes/routes';
import { sendEvent } from '@/shared/lib/ga';
import { Button } from '@/shared/ui/button/Button';
import { Hero } from '@/widgets/hero';
import { useDelayedVisible } from '@/shared/hooks/useDelayedVisible';
import { useNavigate } from 'react-router';
import * as S from './index.styles';
import { useState } from 'react';
import { NavigatorsBar } from '@/widgets/navigatorsBar';
import { LevelTable } from '@/widgets/levelTable/LevelTable';

export default function HomePage() {
  const [isWidgetOpen, setIsWidgetOpen] = useState(false);

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
          <S.BlackHoleImage src="/blackHole.png" alt="blackHole" />
        </button>
      </S.BlackHoleContainer>
      <S.WidgetContainer isWidgetOpen={isWidgetOpen}>
        <NavigatorsBar $isNavBar={false} />
      </S.WidgetContainer>
    </S.HomePageWrapper>
  );
}
