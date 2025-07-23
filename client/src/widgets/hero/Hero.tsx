import { useDelayedVisible } from '@/shared/hooks/useDelayedVisible';
import * as S from './Hero.styles';

interface HeroProps {
  title?: string[];
  subtitle?: string[];
}

export default function Hero({
  title = ['나의 모든 이야기가', '공감이 되는 순간'],
  subtitle = [
    '삶의 다양한 순간을 공유하며 서로 진실된 공감을 건네는',
    '특별한 소셜 네트워크 서비스입니다',
  ],
}: HeroProps) {
  const { isVisible } = useDelayedVisible({ delay: 100 });

  return (
    <S.HeroWrapper isVisible={isVisible}>
      <S.TitleContainer isVisible={isVisible}>
        {title.map((text, index) => (
          <S.Title key={`hero-title-${index}`}>{text}</S.Title>
        ))}
      </S.TitleContainer>

      <S.AccentLine isVisible={isVisible} />

      <S.SubtitleContainer isVisible={isVisible}>
        {subtitle.map((text, index) => (
          <S.Subtitle key={`hero-subtitle-${index}`}>{text}</S.Subtitle>
        ))}
      </S.SubtitleContainer>
    </S.HeroWrapper>
  );
}
