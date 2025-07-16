import { colors } from '@/app/styles/colors';
import Hero from '@/pages/home/Hero';
import { Button } from '@/shared/ui/Button';
import { css } from '@emotion/react';
import { useNavigate } from 'react-router';
import * as S from './index.styles';

const ButtonStyle = css`
  background-color: ${colors.secondary};
  color: black;
  padding: 18px 30px;
  border-radius: 50px;
  font-size: 24px;
  font-weight: bold;
  transition: all 0.3s ease;

  @media (max-width: 768px) {
    padding: 16px 24px;
    font-size: 20px;
  }

  @media (max-width: 480px) {
    padding: 14px 20px;
    font-size: 18px;
  }

  &:hover {
    filter: brightness(1.1);
    box-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
    transform: translateY(-2px);
  }
`;

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
        <Button title="지금 시작하기" css={ButtonStyle} />
      </S.ContentSection>
    </S.HomePageWrapper>
  );
}
