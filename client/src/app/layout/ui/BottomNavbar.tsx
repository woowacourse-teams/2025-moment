import { ROUTES } from '@/app/routes/routes';
import { Picture } from '@/shared/design-system/picture';
import { useReadNotificationsQuery } from '@/features/notification/api/useReadNotificationsQuery';
import { useGroupsQuery } from '@/features/group/api/useGroupsQuery';
import { usePendingMembersQuery } from '@/features/group/api/usePendingMembersQuery';
import { Link, useLocation, useParams } from 'react-router';
import { isApp } from '@/shared/utils/device';
import * as S from './BottomNavbar.styles';

export const BottomNavbar = () => {
  const location = useLocation();
  const { groupId } = useParams<{ groupId: string }>();
  const { data: notifications } = useReadNotificationsQuery();
  const { data: groupsData } = useGroupsQuery();
  const { data: pendingMembers } = usePendingMembersQuery(groupId || '');

  const currentPath = location.pathname;

  const replaceGroupId = (path: string) => {
    if (!groupId) return '#';
    return path.replace(':groupId', groupId);
  };

  const isOwner = groupsData?.data.find(g => g.groupId === Number(groupId))?.isOwner;

  const isNotificationExisting =
    (notifications?.data && notifications.data.length > 0) ||
    (isOwner && pendingMembers?.data && pendingMembers.data.length > 0);

  const isActive = (path: string) => {
    if (path === ROUTES.TODAY_MOMENT && currentPath.includes('/today-moment')) return true;
    if (path === ROUTES.TODAY_COMMENT && currentPath.includes('/today-comment')) return true;
    if (path === ROUTES.COLLECTION_MYMOMENT && currentPath.includes('/collection')) return true;
    if (path === ROUTES.MY && currentPath.startsWith('/my')) return true;
    return false;
  };

  if (!groupId || isApp()) return null;

  return (
    <S.BottomNavContainer>
      <S.NavItem $isActive={isActive(ROUTES.TODAY_MOMENT)}>
        <Link to={replaceGroupId(ROUTES.TODAY_MOMENT)}>
          <S.IconWrapper>
            <Picture
              webpSrc="/images/paperAirplane.webp"
              fallbackSrc="/images/fallback/paperAirplane.png"
              alt="모멘트"
              width="24"
              height="24"
            />
          </S.IconWrapper>
          <S.Label>모멘트</S.Label>
        </Link>
      </S.NavItem>

      <S.NavItem $isActive={isActive(ROUTES.TODAY_COMMENT)}>
        <Link to={replaceGroupId(ROUTES.TODAY_COMMENT)}>
          <S.IconWrapper>
            <Picture
              webpSrc="/images/bluePlanet.webp"
              fallbackSrc="/images/fallback/bluePlanet.png"
              alt="코멘트"
              width="24"
              height="24"
            />
          </S.IconWrapper>
          <S.Label>코멘트</S.Label>
        </Link>
      </S.NavItem>

      <S.NavItem $isActive={currentPath === ROUTES.ROOT}>
        <Link to={ROUTES.ROOT}>
          <S.IconWrapper>
            <Picture
              webpSrc="/images/rocket.webp"
              fallbackSrc="/images/fallback/rocket.png"
              alt="홈"
              width="24"
              height="24"
            />
          </S.IconWrapper>
          <S.Label>홈</S.Label>
        </Link>
      </S.NavItem>

      <S.NavItem $isActive={isActive(ROUTES.COLLECTION_MYMOMENT)} $shadow={isNotificationExisting}>
        <Link to={replaceGroupId(ROUTES.COLLECTION_MYMOMENT)}>
          <S.IconWrapper>
            <Picture
              webpSrc="/images/starPlanet.webp"
              fallbackSrc="/images/fallback/starPlanet.png"
              alt="모음집"
              width="24"
              height="24"
            />
          </S.IconWrapper>
          <S.Label>모음집</S.Label>
        </Link>
      </S.NavItem>

      <S.NavItem $isActive={isActive(ROUTES.MY)}>
        <Link to={ROUTES.MY}>
          <S.IconWrapper>
            <Picture
              webpSrc="/images/spaceMan.webp"
              fallbackSrc="/images/fallback/spaceMan.png"
              alt="마이페이지"
              width="24"
              height="24"
            />
          </S.IconWrapper>
          <S.Label>마이페이지</S.Label>
        </Link>
      </S.NavItem>
    </S.BottomNavContainer>
  );
};
