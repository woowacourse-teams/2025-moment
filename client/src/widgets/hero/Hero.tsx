import { useDelayedVisible } from '@/shared/hooks/useDelayedVisible';
import * as S from './Hero.styles';

interface HeroProps {
  title?: string[];
  subtitle?: string[];
}

export default function Hero({
  title = ['나의 모든 이야기가', '공감이 되는 순간'],
  subtitle = ['당신의 이야기를 기록하고 따뜻한 공감을 받아보세요'],
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

      <S.HighlightedTextContainer isVisible={isVisible}>
        <S.HighlightedText>" 비교 없는 따뜻한 소통의 공간 "</S.HighlightedText>
        <S.HighlightedText>" 익명 속에서 편안하게 마음을 나눌 수 있는 곳 "</S.HighlightedText>
      </S.HighlightedTextContainer>
    </S.HeroWrapper>
  );
}
