import React from 'react';
import { SimpleCardHeight, ColorKey } from './SimpleCard.styles';
import * as S from './SimpleCard.styles';

export interface SimpleCardProps extends React.HTMLAttributes<HTMLDivElement> {
  height: SimpleCardHeight;
  backgroundColor?: ColorKey;
}

export const SimpleCard = React.forwardRef<HTMLDivElement, SimpleCardProps>(
  ({ height, backgroundColor, children, ...props }, ref) => {
    return (
      <S.SimpleCard ref={ref} $height={height} $backgroundColor={backgroundColor} {...props}>
        {children}
      </S.SimpleCard>
    );
  },
);

SimpleCard.displayName = 'SimpleCard';
