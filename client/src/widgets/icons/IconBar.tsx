import { Link } from 'react-router';
import * as S from './IconBar.styles';
import { ROUTES } from '@/app/routes/routes';

export const IconBar = () => {
  return (
    <S.IconBarAside>
      <S.IconBarContainer>
        <S.IconWrapper>
          <Link to={ROUTES.TODAY_MOMENT}>
            <S.IconImage src="/bluePlanet.png" alt="bluePlanet"></S.IconImage>
            <S.IconText>오늘의 모멘트</S.IconText>
          </Link>
        </S.IconWrapper>
        <S.IconWrapper>
          <Link to={ROUTES.TODAY_COMMENT}>
            <S.IconImage src="/orangePlanet.png" alt="orangePlanet"></S.IconImage>
            <S.IconText>오늘의 코멘트</S.IconText>
          </Link>
        </S.IconWrapper>
        <S.IconWrapper>
          <Link to={ROUTES.COLLECTION}>
            <S.IconImage src="/starPlanet.png" alt="starPlanet"></S.IconImage>
            <S.IconText>나만의 모음집</S.IconText>
          </Link>
        </S.IconWrapper>
      </S.IconBarContainer>
    </S.IconBarAside>
  );
};
