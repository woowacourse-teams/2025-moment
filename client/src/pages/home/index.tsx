import { Hero } from '@/widgets/hero';
import { useNavigate } from 'react-router';
import * as S from './index.styles';
import { Button } from '@/shared/ui/button/Button';

export default function HomePage() {
  const navigate = useNavigate();

  const handleClick = () => {
    // navigate('/auth/signin');
  };

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
