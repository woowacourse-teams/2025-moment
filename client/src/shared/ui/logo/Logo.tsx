import { useNavigate } from 'react-router';
import * as S from './Logo.styles';

export const Logo = () => {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate('/');
  };

  return (
    <S.LogoButton onClick={handleClick} aria-label="모멘트 메인페이지 이동">
      <S.LogoImage webpSrc="/images/logo.webp" fallbackSrc="/images/fallback/logo.png" alt="" />
      <S.LogoText>Moment</S.LogoText>
    </S.LogoButton>
  );
};
