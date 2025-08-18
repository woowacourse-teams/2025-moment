import { cardWidth } from './Card.styles';
import * as S from './Card.styles';
import { CardAction } from './CardAction';
import { CardContent } from './CardContent';
import { CardTitleContainer } from './CardTitleContainer';

export type CardVariant = 'primary' | 'secondary';

interface CardProps {
  children: React.ReactNode;
  width: cardWidth;
  shadow?: boolean;
  variant?: CardVariant;
}

export const Card = ({ children, width, shadow = false, variant = 'primary' }: CardProps) => {
  return (
    <S.Card $width={width} $shadow={shadow} $variant={variant}>
      {children}
    </S.Card>
  );
};

Card.TitleContainer = CardTitleContainer;
Card.Content = CardContent;
Card.Action = CardAction;
