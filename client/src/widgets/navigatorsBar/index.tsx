import { ROUTES } from '@/app/routes/routes';
import { useNotificationsQuery } from '@/features/notification/hooks/useNotificationsQuery';
import { sendEvent } from '@/shared/lib/ga';
import { NavigatorsBarAnalyticsEvent } from '@/shared/lib/ga/analyticsEvent';
import { Link, useLocation } from 'react-router';
import * as S from './index.styles';

export const NavigatorsBar = ({ $isNavBar }: { $isNavBar?: boolean }) => {
  const { data: notifications } = useNotificationsQuery();
  const location = useLocation();

  const isTodayMomentActive = location.pathname.startsWith('/today-moment');
  const isTodayCommentActive = location.pathname.startsWith('/today-comment');
  const isCollectionActive = location.pathname.startsWith('/collection');

  const isNotificationExisting =
    notifications?.data.length && notifications?.data.length > 0 ? true : false;

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
        <S.LinkContainer $isNavBar={$isNavBar} $isActive={isTodayMomentActive}>
          <S.IconImage src="/images/bluePlanet.png" alt="오늘의 모멘트 페이지로 이동 버튼" />
          <S.IconText $isActive={isTodayMomentActive}>오늘의 모멘트</S.IconText>
        </S.LinkContainer>
      </Link>

      <Link to={ROUTES.TODAY_COMMENT} onClick={handleTodayCommentClick}>
        <S.LinkContainer $isNavBar={$isNavBar} $isActive={isTodayCommentActive}>
          <S.IconImage src="/images/orangePlanet.png" alt="오늘의 코멘트 페이지로 이동 버튼" />
          <S.IconText $isActive={isTodayCommentActive}>오늘의 코멘트</S.IconText>
        </S.LinkContainer>
      </Link>

      <Link to={ROUTES.COLLECTION_MYMOMENT} onClick={handleCollectionClick}>
        <S.LinkContainer $isNavBar={$isNavBar} $isActive={isCollectionActive}>
          <S.IconImage src="/images/starPlanet.png" alt="나만의 모음집 페이지로 이동 버튼" />
          <S.IconText $isActive={isCollectionActive}>나만의 모음집</S.IconText>
        </S.LinkContainer>
      </Link>
    </S.NavigatorsBarContainer>
  );
};
