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
    if (comments?.data.items && notifications && notifications.length > 0) {
      const commentsWithNotifications = comments.data.items.map(comment => ({
        ...comment,
        read: !notifications.find(
          notificationResponse => notificationResponse.data.target_id === comment.id,
        ),
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
