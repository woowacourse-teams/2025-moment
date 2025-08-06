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
    if (moments?.data.items && notifications && notifications.length > 0) {
      const momentWithNotifications = moments.data.items.map(moment => ({
        ...moment,
        read: !notifications.find(
          notificationResponse =>
            notificationResponse.data.target_type === 'MOMENT' &&
            notificationResponse.data.target_id === moment.id,
        ),
      }));

      setMomentWithNotifications(momentWithNotifications);
    } else if (moments?.data.items) {
      const momentWithNotifications = moments.data.items.map(moment => ({
        ...moment,
        read: true,
      }));

      setMomentWithNotifications(momentWithNotifications);
    }
  }, [moments, notifications]);

  return { momentWithNotifications, isLoading };
};
