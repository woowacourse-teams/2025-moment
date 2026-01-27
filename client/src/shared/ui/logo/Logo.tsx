import { Link } from 'react-router';
import { isApp } from '@/shared/utils/device';
import * as S from './Logo.styles';

export const Logo = () => {
  const content = (
    <S.LogoButton as={isApp() ? 'button' : undefined}>
      <S.LogoImage webpSrc="/images/logo.webp" fallbackSrc="/images/fallback/logo.png" alt="" />
      <S.LogoText>Moment</S.LogoText>
    </S.LogoButton>
  );

  if (isApp()) {
    return content;
  }

  return <Link to="/">{content}</Link>;
};
