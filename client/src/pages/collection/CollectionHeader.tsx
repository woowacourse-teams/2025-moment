import { ROUTES } from '@/app/routes/routes';
import { useNotificationsQuery } from '@/features/notification/hooks/useNotificationsQuery';
import { useLocation } from 'react-router';
import * as S from './index.styles';

export const CollectionHeader = () => {
  const currentpath = useLocation().pathname;
  const { data: notifications } = useNotificationsQuery();

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
      >
        나의 모멘트 모음집
      </S.CollectionHeaderLinkContainer>
      <S.CollectionHeaderLinkContainer
        to={ROUTES.COLLECTION_MYCOMMENT}
        className={currentpath === ROUTES.COLLECTION_MYCOMMENT ? 'active' : ''}
        $shadow={isCommentNotificationExisting}
      >
        나의 코멘트 모음집
      </S.CollectionHeaderLinkContainer>
    </S.CollectionHeaderContainer>
  );
};
