import { cardWidth } from './Card.styles';
import * as S from './Card.styles';
import { CardAction } from './CardAction';
import { CardContent } from './CardContent';
import { CardTitleContainer } from './CardTitleContainer';

interface CardProps {
  children: React.ReactNode;
  width: cardWidth;
}

export const Card = ({ children, width }: CardProps) => {
  return <S.Card $width={width}>{children}</S.Card>;
};

Card.TitleContainer = CardTitleContainer;
Card.Content = CardContent;
Card.Action = CardAction;
