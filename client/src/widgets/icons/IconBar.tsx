import * as S from './IconBar.styles';

export const IconBar = () => {
  return (
    <S.IconBarContainer>
      <S.IconWrapper>
        <S.IconImage src="/bluePlanet.png" alt="bluePlanet"></S.IconImage>
        <S.IconText>오늘의 모멘트</S.IconText>
      </S.IconWrapper>
      <S.IconWrapper>
        <S.IconImage src="/orangePlanet.png" alt="orangePlanet"></S.IconImage>
        <S.IconText>오늘의 코멘트</S.IconText>
      </S.IconWrapper>
      <S.IconWrapper>
        <S.IconImage src="/starPlanet.png" alt="starPlanet"></S.IconImage>
        <S.IconText>모음집</S.IconText>
      </S.IconWrapper>
    </S.IconBarContainer>
  );
};
