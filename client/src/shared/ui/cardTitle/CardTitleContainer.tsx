import { LucideIcon } from 'lucide-react';
import * as S from './CardTitleContainer.styles';

interface CardTitleWrapperProps {
  Icon: LucideIcon;
  title: string;
  subtitle: string;
}

export const CardTitleWrapper = ({ Icon, title, subtitle }: CardTitleWrapperProps) => {
  return (
      <S.CardTitleContainer>
        <S.CardTitleWrapper>
          <Icon size={32} />
          <S.CardTitle>{title}</S.CardTitle>
        </S.CardTitleWrapper>
        <S.CardSubtitle>{subtitle}</S.CardSubtitle>
      </S.CardTitleContainer>
  );
};
