import { ROUTES } from '@/app/routes/routes';
import { sendEvent } from '@/shared/lib/ga';
import { Button } from '@/shared/ui/button/Button';
import { Hero } from '@/widgets/hero';
import { useNavigate } from 'react-router';
import * as S from './index.styles';

export default function HomePage() {
  const navigate = useNavigate();

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
      <S.ContentSection>
        <Button title="모멘트 작성하기" variant="secondary" onClick={handleClick} />
      </S.ContentSection>
    </S.HomePageWrapper>
  );
}
