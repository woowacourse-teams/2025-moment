import { useNotificationsQuery } from '@/features/notification/hooks/useNotificationsQuery';
import { useEffect, useMemo, useState } from 'react';
import { useCommentsQuery } from './useCommentsQuery';
import notificationsData from '@/features/notification/mocks/notifications.json'; // TODO: 임시 MOCK 데이터. 추후 API 연결 시 삭제.
import { CommentWithNotifications } from '../types/commentsWithNotifications';

export const useCommentsWithNotifications = () => {
  const { data: comments, isLoading, error } = useCommentsQuery();
  //   const { data: notifications } = useNotificationsQuery(); // TODO: 추후 API 연결 시 해당 데이터로 사용

  const notifications = useMemo(() => notificationsData.map(item => item.data), []);
  const [commentsWithNotifications, setCommentsWithNotifications] = useState<
    CommentWithNotifications[]
  >([]);

  useEffect(() => {
    if (comments?.data.items) {
      const commentsWithNotifications = comments.data.items.map(comment => ({
        ...comment,
        read: !notifications?.find(notification => notification.target_id === comment.id),
      }));

      setCommentsWithNotifications(commentsWithNotifications);
    }
  }, [comments, notifications]);
  return {
    commentsWithNotifications,
    isLoading,
    error,
  };
};
