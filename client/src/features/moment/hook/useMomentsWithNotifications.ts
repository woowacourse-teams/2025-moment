import { useNotificationsQuery } from '@/features/notification/hooks/useNotificationsQuery';
import { useMomentsQuery } from './useMomentsQuery';
import notificationsData from '@/features/notification/mocks/notifications.json'; // TODO: 임시 MOCK 데이터. 추후 API 연결 시 삭제.
import { useEffect, useState, useMemo } from 'react';
import { MomentWithNotifications } from '../types/momentsWithNotifications';

export const useMomentsWithNotifications = () => {
  const { data: moments, isLoading } = useMomentsQuery();
  //   const { data: notifications } = useNotificationsQuery(); // TODO: 추후 API 연결 시 해당 데이터로 사용
  const notifications = useMemo(() => notificationsData.map(item => item.data), []);
  const [momentWithNotifications, setMomentWithNotifications] = useState<MomentWithNotifications[]>(
    [],
  );

  useEffect(() => {
    if (moments?.data.items) {
      const momentWithNotifications = moments.data.items.map(moment => ({
        ...moment,
        read: !notifications?.find(
          notification =>
            notification.target_type === 'MOMENT' && notification.target_id === moment.id,
        ),
      }));

      setMomentWithNotifications(momentWithNotifications);
    }
  }, [moments, notifications]);

  return { momentWithNotifications, isLoading };
};
