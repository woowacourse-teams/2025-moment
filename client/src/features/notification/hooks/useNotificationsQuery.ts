import { useQuery } from '@tanstack/react-query';
import { getNotifications } from '../api/getNotifications';

export const useNotificationsQuery = () => {
  return useQuery({
    queryKey: ['notifications'],
    queryFn: getNotifications,
  });
};
