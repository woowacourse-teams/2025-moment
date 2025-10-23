import { ROUTES } from '@/app/routes/routes';
import { useReadNotificationsQuery } from '@/features/notification/api/useReadNotificationsQuery';
import { useLocation } from 'react-router';
import * as S from './index.styles';

export const CollectionHeader = () => {
  const currentpath = useLocation().pathname;
  const { data: notifications } = useReadNotificationsQuery();

  const isMomentNotificationExisting = notifications?.data.some(
    notification => notification.targetType === 'MOMENT',
  );
  const isCommentNotificationExisting = notifications?.data.some(
    notification => notification.targetType === 'COMMENT',
  );

  return (
    <S.CollectionHeaderContainer>
      <S.CollectionHeaderLinkContainer
        to={ROUTES.COLLECTION_MYMOMENT}
        className={currentpath === ROUTES.COLLECTION_MYMOMENT ? 'active' : ''}
        $shadow={isMomentNotificationExisting}
        aria-current={currentpath === ROUTES.COLLECTION_MYMOMENT ? 'page' : undefined}
        aria-label={
          isMomentNotificationExisting
            ? '나의 모멘트 모음집 페이지 이동 (새 알림 있음)'
            : '나의 모멘트 모음집 페이지 이동'
        }
      >
        나의 모멘트 모음집
      </S.CollectionHeaderLinkContainer>
      <S.CollectionHeaderLinkContainer
        to={ROUTES.COLLECTION_MYCOMMENT}
        className={currentpath === ROUTES.COLLECTION_MYCOMMENT ? 'active' : ''}
        $shadow={isCommentNotificationExisting}
        aria-current={currentpath === ROUTES.COLLECTION_MYCOMMENT ? 'page' : undefined}
        aria-label={
          isCommentNotificationExisting
            ? '나의 코멘트 모음집 페이지 이동 (새 알림 있음)'
            : '나의 코멘트 모음집 페이지 이동'
        }
      >
        나의 코멘트 모음집
      </S.CollectionHeaderLinkContainer>
    </S.CollectionHeaderContainer>
  );
};
