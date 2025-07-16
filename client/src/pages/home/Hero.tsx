import { useEffect, useState } from 'react';
import * as S from './Hero.styles';

interface HeroProps {
  title?: string[];
  subtitle?: string[];
  delay?: number;
}

export default function Hero({
  title = ['나의 모든 이야기가', '공감이 되는 순간'],
  subtitle = [
    '삶의 다양한 순간을 공유하며 서로 진실된 공감을 건네는',
    '특별한 소셜 네트워크 서비스입니다',
  ],
  delay = 100,
}: HeroProps) {
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsVisible(true);
    }, delay);

    return () => clearTimeout(timer);
  }, [delay]);

  return (
    <S.HeroWrapper isVisible={isVisible}>
      <S.TitleContainer isVisible={isVisible}>
        {title.map((text, index) => (
          <S.Title key={index}>{text}</S.Title>
        ))}
      </S.TitleContainer>

      <S.AccentLine isVisible={isVisible} />

      <S.SubtitleContainer isVisible={isVisible}>
        {subtitle.map((text, index) => (
          <S.Subtitle key={index}>{text}</S.Subtitle>
        ))}
      </S.SubtitleContainer>
    </S.HeroWrapper>
  );
}
