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
      {Icon && (
        <S.CardSuccessIcon>
          <Icon />
        </S.CardSuccessIcon>
      )}
      <S.CardSuccessTitle>{title}</S.CardSuccessTitle>
      <S.CardSuccessSubtitle>{subtitle}</S.CardSuccessSubtitle>
    </S.CardSuccessContainer>
  );
};
