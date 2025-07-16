import { useNavigate } from 'react-router';
import * as S from './index.styles';

export const Logo = () => {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate('/');
  };

  return (
    <S.LogoButton onClick={handleClick}>
      <S.LogoImage src="/logo.webp" alt="Moment Logo Image" />
      <S.LogoText>Moment</S.LogoText>
    </S.LogoButton>
  );
};
