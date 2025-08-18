import { useQuery } from '@tanstack/react-query';
import { getNotifications } from '../api/getNotifications';

interface UseNotificationsQueryOptions {
  enabled?: boolean;
}

export const useNotificationsQuery = (options?: UseNotificationsQueryOptions) => {
  return useQuery({
    queryKey: ['notifications'],
    queryFn: getNotifications,
    enabled: options?.enabled ?? true,
  });
};
