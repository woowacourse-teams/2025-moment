import { ROUTES } from '@/app/routes/routes';
import { Link } from 'react-router';
import * as S from './index.styles';

export const NavigatorsBar = () => {
  return (
    <S.NavigatorsBarContainer>
      <Link to={ROUTES.TODAY_MOMENT}>
        <S.LinkContainer>
          <S.IconImage src="/bluePlanet.png" alt="bluePlanet"></S.IconImage>
          <S.IconText>오늘의 모멘트</S.IconText>
        </S.LinkContainer>
      </Link>

      <Link to={ROUTES.TODAY_COMMENT}>
        <S.LinkContainer>
          <S.IconImage src="/orangePlanet.png" alt="orangePlanet"></S.IconImage>
          <S.IconText>오늘의 코멘트</S.IconText>
        </S.LinkContainer>
      </Link>

      <Link to={ROUTES.COLLECTION_MYMOMENT}>
        <S.LinkContainer>
          <S.IconImage src="/starPlanet.png" alt="starPlanet"></S.IconImage>
          <S.IconText>나만의 모음집</S.IconText>
        </S.LinkContainer>
      </Link>
    </S.NavigatorsBarContainer>
  );
};
