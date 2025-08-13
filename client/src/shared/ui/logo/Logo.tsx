import { useNavigate } from 'react-router';
import * as S from './Logo.styles';

export const Logo = () => {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate('/');
  };

  return (
    <S.LogoButton onClick={handleClick}>
      <S.LogoImage src="/logo.webp" alt="모멘트 메인페이지 이동" />
      <S.LogoText>Moment</S.LogoText>
    </S.LogoButton>
  );
};
