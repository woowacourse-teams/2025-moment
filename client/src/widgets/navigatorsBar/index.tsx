import { ROUTES } from '@/app/routes/routes';
import { Link } from 'react-router';
import * as S from './index.styles';
import { sendEvent } from '@/shared/lib/ga';
import { NavigatorsBarAnalyticsEvent } from '@/shared/lib/ga/analyticsEvent';

export const NavigatorsBar = ({ $isNavBar }: { $isNavBar?: boolean }) => {
  const handleTodayMomentClick = () => {
    sendEvent(NavigatorsBarAnalyticsEvent.ClickTodayMomentButton);
  };

  const handleTodayCommentClick = () => {
    sendEvent(NavigatorsBarAnalyticsEvent.ClickTodayCommentButton);
  };

  const handleCollectionClick = () => {
    sendEvent(NavigatorsBarAnalyticsEvent.ClickCollectionButton);
  };

  return (
    <S.NavigatorsBarContainer $isNavBar={$isNavBar}>
      <Link to={ROUTES.TODAY_MOMENT} onClick={handleTodayMomentClick}>
        <S.LinkContainer $isNavBar={$isNavBar}>
          <S.IconImage src="/bluePlanet.png" alt="오늘의 모멘트 페이지로 이동 버튼"></S.IconImage>
          <S.IconText>오늘의 모멘트</S.IconText>
        </S.LinkContainer>
      </Link>

      <Link to={ROUTES.TODAY_COMMENT} onClick={handleTodayCommentClick}>
        <S.LinkContainer $isNavBar={$isNavBar}>
          <S.IconImage src="/orangePlanet.png" alt="오늘의 코멘트 페이지로 이동 버튼"></S.IconImage>
          <S.IconText>오늘의 코멘트</S.IconText>
        </S.LinkContainer>
      </Link>

      <Link to={ROUTES.COLLECTION_MYMOMENT} onClick={handleCollectionClick}>
        <S.LinkContainer $isNavBar={$isNavBar}>
          <S.IconImage src="/starPlanet.png" alt="나만의 모음집 페이지로 이동 버튼"></S.IconImage>
          <S.IconText>나만의 모음집</S.IconText>
        </S.LinkContainer>
      </Link>
    </S.NavigatorsBarContainer>
  );
};
