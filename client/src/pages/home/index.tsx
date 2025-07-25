import { Button } from '@/shared/ui/button/Button';
import { Hero } from '@/widgets/hero';
import * as S from './index.styles';

export default function HomePage() {
  return (
    <S.HomePageWrapper>
      <S.HeroSection>
        <Hero />
      </S.HeroSection>
      <S.ContentSection>
        <Button title="지금 시작하기" variant="secondary" />
      </S.ContentSection>
    </S.HomePageWrapper>
  );
}
