import { ROUTES } from '@/app/routes/routes';
import { useReadNotificationsQuery } from '@/features/notification/api/useReadNotificationsQuery';
import { useLocation, useParams } from 'react-router';
import * as S from './index.styles';

export const CollectionHeader = () => {
  const { groupId } = useParams<{ groupId: string }>();
  const currentpath = useLocation().pathname;
  const { data: notifications } = useReadNotificationsQuery();

  const isMomentNotificationExisting = notifications?.data.some(
    notification => notification.targetType === 'MOMENT',
  );
  const isCommentNotificationExisting = notifications?.data.some(
    notification => notification.targetType === 'COMMENT',
  );

  const momentCollectionPath = ROUTES.COLLECTION_MYMOMENT.replace(':groupId', groupId || '');
  const commentCollectionPath = ROUTES.COLLECTION_MYCOMMENT.replace(':groupId', groupId || '');

  return (
    <S.CollectionHeaderContainer>
      <S.CollectionHeaderLinkContainer
        to={momentCollectionPath}
        className={currentpath === momentCollectionPath ? 'active' : ''}
        $shadow={isMomentNotificationExisting}
        aria-current={currentpath === momentCollectionPath ? 'page' : undefined}
        aria-label={
          isMomentNotificationExisting
            ? '나의 모멘트 모음집 페이지 이동 (새 알림 있음)'
            : '나의 모멘트 모음집 페이지 이동'
        }
      >
        나의 모멘트 모음집
      </S.CollectionHeaderLinkContainer>
      <S.CollectionHeaderLinkContainer
        to={commentCollectionPath}
        className={currentpath === commentCollectionPath ? 'active' : ''}
        $shadow={isCommentNotificationExisting}
        aria-current={currentpath === commentCollectionPath ? 'page' : undefined}
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
