import { useDelayedVisible } from '@/shared/hooks/useDelayedVisible';
import * as S from './Hero.styles';

interface HeroProps {
  title?: string[];
  subtitle?: string[];
}

export default function Hero({
  title = ['나의 모든 이야기가', '공감이 되는 순간'],
  subtitle = [
    '오늘 하루 공감이 필요한 사람들을 위해,',
    '그 순간을 기록하고 따뜻한 공감을 받을 수 있는 공간',
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
