import { ROUTES } from '@/app/routes/routes';
import { sendEvent } from '@/shared/lib/ga';
import { Button } from '@/shared/ui/button/Button';
import { Hero } from '@/widgets/hero';
import { useDelayedVisible } from '@/shared/hooks/useDelayedVisible';
import { useNavigate } from 'react-router';
import * as S from './index.styles';
import { IconBar } from '@/widgets/icons/IconBar';

export default function HomePage() {
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

  return (
    <S.HomePageWrapper>
      <S.HeroSection>
        <Hero />
      </S.HeroSection>
      <S.ContentSection isVisible={isVisible}>
        <Button title="모멘트 작성하기" variant="secondary" onClick={handleClick} />
      </S.ContentSection>
      <S.IconBarAside>
        <IconBar />
      </S.IconBarAside>
    </S.HomePageWrapper>
  );
}
