import { useNotificationsQuery } from '@/features/notification/hooks/useNotificationsQuery';
import { useEffect, useState } from 'react';
import { useCommentsQuery } from './useCommentsQuery';
import { CommentWithNotifications } from '../types/commentsWithNotifications';

export const useCommentsWithNotifications = () => {
  const { data: comments, isLoading, error } = useCommentsQuery();
  const { data: notifications } = useNotificationsQuery();

  const [commentsWithNotifications, setCommentsWithNotifications] = useState<
    CommentWithNotifications[]
  >([]);

  useEffect(() => {
    if (!comments?.data.items) {
      return;
    }

    // 알림이 있는 경우
    if (notifications?.data && notifications.data.length > 0) {
      const commentsWithNotifications = comments.data.items.map(comment => ({
        ...comment,
        notificationId:
          notifications.data.find(
            notification =>
              notification.targetType === 'COMMENT' && notification.targetId === comment.id,
          )?.id || null,
        read: !notifications.data.find(
          notification =>
            notification.targetType === 'COMMENT' && notification.targetId === comment.id,
        ),
      }));

      setCommentsWithNotifications(commentsWithNotifications);
      return;
    }

    // 알림이 없는 경우
    const commentsWithNotifications = comments.data.items.map(comment => ({
      ...comment,
      notificationId: null,
      read: true,
    }));

    setCommentsWithNotifications(commentsWithNotifications);
  }, [comments, notifications]);

  return {
    commentsWithNotifications,
    isLoading,
    error,
  };
};
