import { cardWidth } from './Card.styles';
import * as S from './Card.styles';

interface CardProps {
  children: React.ReactNode;
  width: cardWidth;
}

export const Card = ({ children, width }: CardProps) => {
  return <S.Card width={width}>{children}</S.Card>;
};
