import React from 'react';
import * as S from './TitleContainer.styles';

export interface TitleContainerProps extends React.HTMLAttributes<HTMLElement> {
  title: string;
  subtitle: string;
}

export const TitleContainer = React.forwardRef<HTMLElement, TitleContainerProps>(
  ({ title, subtitle, ...props }, ref) => {
    return (
      <S.TitleContainer ref={ref} {...props}>
        <S.Title>{title}</S.Title>
        <S.Subtitle>{subtitle}</S.Subtitle>
      </S.TitleContainer>
    );
  },
);

TitleContainer.displayName = 'TitleContainer';
