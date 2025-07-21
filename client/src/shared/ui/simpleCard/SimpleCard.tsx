import { SimpleCardHeight } from './SimpleCard.styles';
import * as S from './SimpleCard.styles';

interface SimpleCardProps {
  height: SimpleCardHeight;
  content: React.ReactNode;
}

export const SimpleCard = ({ height, content }: SimpleCardProps) => {
  return <S.SimpleCard $height={height}>{content}</S.SimpleCard>;
};
