import { useNotificationsQuery } from '@/features/notification/hooks/useNotificationsQuery';
import { useMomentsQuery } from './useMomentsQuery';
import { useEffect, useState } from 'react';
import { MomentWithNotifications } from '../types/momentsWithNotifications';

export const useMomentsWithNotifications = () => {
  const { data: moments, isLoading } = useMomentsQuery();
  const { data: notifications } = useNotificationsQuery();

  const [momentWithNotifications, setMomentWithNotifications] = useState<MomentWithNotifications[]>(
    [],
  );

  useEffect(() => {
    if (!moments?.data.items) {
      return;
    }

    // 알림이 있는 경우
    if (notifications?.data && notifications.data.length > 0) {
      const momentWithNotifications = moments.data.items.map(moment => ({
        ...moment,
        notificationId:
          notifications.data.find(
            notification =>
              notification.targetType === 'MOMENT' && notification.targetId === moment.id,
          )?.id || null,
        read: !notifications.data.find(
          notification =>
            notification.targetType === 'MOMENT' && notification.targetId === moment.id,
        ),
      }));

      setMomentWithNotifications(momentWithNotifications);
      return;
    }

    // 알림이 없는 경우
    const momentWithNotifications = moments.data.items.map(moment => ({
      ...moment,
      notificationId: null,
      read: true,
    }));

    setMomentWithNotifications(momentWithNotifications);
  }, [moments, notifications]);

  return { momentWithNotifications, isLoading };
};
