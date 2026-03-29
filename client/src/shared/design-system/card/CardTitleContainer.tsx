import { LucideIcon } from 'lucide-react';
import * as S from './CardTitleContainer.styles';
import { useTheme } from '@emotion/react';

interface CardTitleContainerProps {
  Icon?: LucideIcon;
  title: React.ReactNode;
  subtitle: React.ReactNode;
}

export const CardTitleContainer = ({ Icon, title, subtitle }: CardTitleContainerProps) => {
  const theme = useTheme();
  return (
    <S.CardTitleContainer>
      <S.CardTitleWrapper>
        {Icon && <Icon size={32} color={theme.colors['yellow-500']} />}
        <S.CardTitle>{title}</S.CardTitle>
      </S.CardTitleWrapper>
      <S.CardSubtitle>{subtitle}</S.CardSubtitle>
    </S.CardTitleContainer>
  );
};
