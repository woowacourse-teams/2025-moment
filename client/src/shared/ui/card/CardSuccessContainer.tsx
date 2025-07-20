import { theme } from '@/app/styles/theme';
import { LucideIcon } from 'lucide-react';
import * as S from './CardSuccessContainer.styles';

interface CardSuccessContainerProps {
  Icon?: LucideIcon;
  title: string;
  subtitle: string;
}

export const CardSuccessContainer = ({ Icon, title, subtitle }: CardSuccessContainerProps) => {
  return (
    <S.CardSuccessContainer>
      {Icon && <Icon size={56} color={theme.colors['yellow-500']} />}
      <S.CardSuccessTitle>{title}</S.CardSuccessTitle>
      <S.CardSuccessSubtitle>{subtitle}</S.CardSuccessSubtitle>
    </S.CardSuccessContainer>
  );
};
