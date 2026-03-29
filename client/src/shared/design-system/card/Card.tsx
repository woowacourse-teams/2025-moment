import React from 'react';
import { cardWidth } from './Card.styles';
import * as S from './Card.styles';
import { CardAction } from './CardAction';
import { CardContent } from './CardContent';
import { CardTitleContainer } from './CardTitleContainer';

export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  width: cardWidth;
  shadow?: boolean;
}

type CardComponent = React.ForwardRefExoticComponent<
  CardProps & React.RefAttributes<HTMLDivElement>
> & {
  TitleContainer: typeof CardTitleContainer;
  Content: typeof CardContent;
  Action: typeof CardAction;
};

const CardBase = React.forwardRef<HTMLDivElement, CardProps>(
  ({ children, width, shadow = false, ...props }, ref) => {
    return (
      <S.Card ref={ref} $width={width} $shadow={shadow} {...props}>
        {children}
      </S.Card>
    );
  },
);

CardBase.displayName = 'Card';

export const Card = CardBase as CardComponent;
Card.TitleContainer = CardTitleContainer;
Card.Content = CardContent;
Card.Action = CardAction;
