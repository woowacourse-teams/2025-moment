import { Hero } from '@/widgets/hero';
import * as S from './index.styles';
import { Button } from '@/shared/ui/button/Button';
import { useEffect } from 'react';
import { api } from '@/app/lib/api';

export default function HomePage() {
  // 프로필 조회 테스트
  useEffect(() => {
    const getProfile = async () => {
      const response = await api.get('/users/me');
      console.log(response);
    };
    getProfile();
  }, []);

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
