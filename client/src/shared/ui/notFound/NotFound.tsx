import { Eye, LucideIcon } from 'lucide-react';
import { Card } from '../';
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
        <S.NotFoundImage src={'/images/character.png'} alt="notFound" />
      </S.NotFoundContainer>
    </S.NotFoundWrapper>
  );

  if (withCard) {
    return <Card width="medium">{content}</Card>;
  }

  return content;
};
