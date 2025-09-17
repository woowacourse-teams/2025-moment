import { Eye, LucideIcon } from 'lucide-react';
import { Card } from '../';
import { LazyImage } from '../lazyImage/LazyImage';
import * as S from './NotFound.styles';

interface NotFoundProps {
  title: string;
  subtitle: string;
  icon?: LucideIcon;
  iconSize?: number;
  size?: 'small' | 'large';
  withCard?: boolean;
}

export const NotFound = ({
  title,
  subtitle,
  icon: IconComponent = Eye,
  iconSize = 24,
  size = 'large',
  withCard = false,
}: NotFoundProps) => {
  const content = (
    <S.NotFoundWrapper $size={size}>
      <S.NotFoundIconWrapper $size={size}>
        <IconComponent size={iconSize} />
      </S.NotFoundIconWrapper>
      <S.NotFoundContainer $size={size}>
        <S.NotFoundTitle $size={size}>{title}</S.NotFoundTitle>
        <S.NotFoundSubtitle $size={size}>{subtitle}</S.NotFoundSubtitle>
        <LazyImage
          src={'/images/character.webp'}
          alt="notFound"
          variant="character"
          width="250px"
          height="auto"
        />
      </S.NotFoundContainer>
    </S.NotFoundWrapper>
  );

  if (withCard) {
    return <Card width="medium">{content}</Card>;
  }

  return content;
};
