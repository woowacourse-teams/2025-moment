import { Link } from 'react-router';
import * as S from './IconBar.styles';
import { ROUTES } from '@/app/routes/routes';

export const IconBar = ({ $isNavBar }: { $isNavBar?: boolean }) => {
  return (
    <S.IconBarContainer $isNavBar={$isNavBar}>
      <Link to={ROUTES.TODAY_MOMENT}>
        <S.LinkContainer $isNavBar={$isNavBar}>
          <S.IconImage src="/bluePlanet.png" alt="bluePlanet"></S.IconImage>
          <S.IconText>오늘의 모멘트</S.IconText>
        </S.LinkContainer>
      </Link>

      <Link to={ROUTES.TODAY_COMMENT}>
        <S.LinkContainer $isNavBar={$isNavBar}>
          <S.IconImage src="/orangePlanet.png" alt="orangePlanet"></S.IconImage>
          <S.IconText>오늘의 코멘트</S.IconText>
        </S.LinkContainer>
      </Link>

      <Link to={ROUTES.COLLECTION}>
        <S.LinkContainer $isNavBar={$isNavBar}>
          <S.IconImage src="/starPlanet.png" alt="starPlanet"></S.IconImage>
          <S.IconText>나만의 모음집</S.IconText>
        </S.LinkContainer>
      </Link>
    </S.IconBarContainer>
  );
};
