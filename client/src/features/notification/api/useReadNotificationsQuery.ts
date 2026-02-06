import { api } from '@/app/lib/api';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useQuery } from '@tanstack/react-query';
import type { NotificationResponse } from '../types/notifications';

export const useReadNotificationsQuery = () => {
  const { data: isLoggedIn } = useCheckIfLoggedInQuery();

  return useQuery({
    queryKey: queryKeys.notifications.all,
    queryFn: getNotifications,
    enabled: isLoggedIn ?? false,
  });
};

const getNotifications = async (): Promise<NotificationResponse> => {
  const response = await api.get('/notifications?read=false');
  return response.data;
};
