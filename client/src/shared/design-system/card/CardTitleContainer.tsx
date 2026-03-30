import { LucideIcon } from 'lucide-react';
import * as S from './CardTitleContainer.styles';
import { useTheme } from '@emotion/react';
import React from 'react';

interface CardTitleContainerProps {
  Icon?: LucideIcon;
  title: React.ReactNode;
  subtitle: React.ReactNode;
}

export const CardTitleContainer = React.forwardRef<HTMLDivElement, CardTitleContainerProps>(
  ({ Icon, title, subtitle }, ref) => {
    const theme = useTheme();
    return (
      <S.CardTitleContainer ref={ref}>
        <S.CardTitleWrapper>
          {Icon && <Icon size={32} color={theme.semantic.color.brand.primary} />}
          <S.CardTitle>{title}</S.CardTitle>
        </S.CardTitleWrapper>
        <S.CardSubtitle>{subtitle}</S.CardSubtitle>
      </S.CardTitleContainer>
    );
  },
);

CardTitleContainer.displayName = 'CardTitleContainer';
