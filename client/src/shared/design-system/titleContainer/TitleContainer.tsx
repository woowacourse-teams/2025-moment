import * as S from './TitleContainer.styles';

export interface TitleContainerProps {
  title: string;
  subtitle: string;
}

export const TitleContainer = ({ title, subtitle }: TitleContainerProps) => {
  return (
    <S.TitleContainer>
      <S.Title>{title}</S.Title>
      <S.Subtitle>{subtitle}</S.Subtitle>
    </S.TitleContainer>
  );
};
