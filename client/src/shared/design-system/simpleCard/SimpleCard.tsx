import { SimpleCardHeight, ColorKey } from './SimpleCard.styles';
import * as S from './SimpleCard.styles';

export interface SimpleCardProps {
  height: SimpleCardHeight;
  content: React.ReactNode;
  backgroundColor?: ColorKey;
}

export const SimpleCard = ({ height, content, backgroundColor }: SimpleCardProps) => {
  return (
    <S.SimpleCard $height={height} $backgroundColor={backgroundColor}>
      {content}
    </S.SimpleCard>
  );
};
