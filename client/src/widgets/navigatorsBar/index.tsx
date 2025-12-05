import { ROUTES } from '@/app/routes/routes';
import { useReadNotificationsQuery } from '@/features/notification/api/useReadNotificationsQuery';
import { Picture } from '@/shared/ui/picture';
import { Link, useLocation } from 'react-router';
import * as S from './index.styles';
import { track } from '@/shared/lib/ga/track';

export const NavigatorsBar = ({ $isNavBar }: { $isNavBar?: boolean }) => {
  const { data: notifications } = useReadNotificationsQuery();
  const location = useLocation();

  const isTodayMomentActive = location.pathname.startsWith('/today-moment');
  const isTodayCommentActive = location.pathname.startsWith('/today-comment');
  const isCollectionActive = location.pathname.startsWith('/collection');

  const isNotificationExisting =
    notifications?.data.length && notifications?.data.length > 0 ? true : false;

  const handleTodayMomentClick = () => {
    track('click_navigation', { destination: 'today_moment', source: 'navbar' });
  };

  const handleTodayCommentClick = () => {
    track('click_navigation', { destination: 'today_comment', source: 'navbar' });
  };

  const handleCollectionClick = () => {
    track('click_navigation', { destination: 'collection', source: 'navbar' });
  };

  return (
    <S.NavigatorsBarContainer $isNavBar={$isNavBar}>
      <Link to={ROUTES.TODAY_MOMENT} state={{ entry: 'nav' }} onClick={handleTodayMomentClick}>
        <S.LinkContainer $isNavBar={$isNavBar} $isActive={isTodayMomentActive}>
          <Picture
            webpSrc="/images/bluePlanet.webp"
            fallbackSrc="/images/fallback/bluePlanet.png"
            alt="오늘의 모멘트 페이지로 이동 버튼"
            width="40"
            height="40"
            style={{ borderRadius: '50%' }}
          />
          <S.IconText $isActive={isTodayMomentActive}>오늘의 모멘트</S.IconText>
        </S.LinkContainer>
      </Link>

      <Link to={ROUTES.TODAY_COMMENT} state={{ entry: 'nav' }} onClick={handleTodayCommentClick}>
        <S.LinkContainer $isNavBar={$isNavBar} $isActive={isTodayCommentActive}>
          <Picture
            webpSrc="/images/orangePlanet.webp"
            fallbackSrc="/images/fallback/orangePlanet.png"
            alt="오늘의 코멘트 페이지로 이동 버튼"
            width="40"
            height="40"
            style={{ borderRadius: '50%' }}
          />
          <S.IconText $isActive={isTodayCommentActive}>오늘의 코멘트</S.IconText>
        </S.LinkContainer>
      </Link>

      <Link
        to={ROUTES.COLLECTION_MYMOMENT}
        state={{ entry: 'nav' }}
        onClick={handleCollectionClick}
      >
        <S.LinkContainer
          $isNavBar={$isNavBar}
          $isActive={isCollectionActive}
          $shadow={isNotificationExisting}
        >
          <Picture
            webpSrc="/images/starPlanet.webp"
            fallbackSrc="/images/fallback/starPlanet.png"
            alt="나만의 모음집 페이지로 이동 버튼"
            width="40"
            height="40"
            style={{ borderRadius: '50%' }}
          />
          <S.IconText $isActive={isCollectionActive}>나만의 모음집</S.IconText>
        </S.LinkContainer>
      </Link>
    </S.NavigatorsBarContainer>
  );
};
