import * as S from './Logo.styles';

export const Logo = () => {
  return (
    <S.LogoButton>
      <S.LogoImage webpSrc="/images/logo.webp" fallbackSrc="/images/fallback/logo.png" alt="" />
      <S.LogoText>Moment</S.LogoText>
    </S.LogoButton>
  );
};
