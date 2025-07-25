import { ROUTES } from '@/app/routes/routes';
import { Button } from '@/shared/ui/button/Button';
import { Hero } from '@/widgets/hero';
import { useNavigate } from 'react-router';
import * as S from './index.styles';

export default function HomePage() {
  const navigate = useNavigate();
  return (
    <S.HomePageWrapper>
      <S.HeroSection>
        <Hero />
      </S.HeroSection>
      <S.ContentSection>
        <Button
          title="모멘트 작성하기"
          variant="secondary"
          onClick={() => navigate(ROUTES.TODAY_MOMENT)}
        />
      </S.ContentSection>
    </S.HomePageWrapper>
  );
}
