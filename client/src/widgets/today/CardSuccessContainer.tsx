import { LucideIcon } from 'lucide-react';
import * as S from './CardSuccessContainer.styles';

interface CardSuccessContainer {
  Icon?: LucideIcon;
  title: string;
  subtitle: string;
}

export const CardSuccessContainer = ({ Icon, title, subtitle }: CardSuccessContainer) => {
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
