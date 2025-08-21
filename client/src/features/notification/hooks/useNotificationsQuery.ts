import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useQuery } from '@tanstack/react-query';
import { getNotifications } from '../api/getNotifications';

export const useNotificationsQuery = () => {
  const { data: isLoggedIn } = useCheckIfLoggedInQuery();

  return useQuery({
    queryKey: ['notifications'],
    queryFn: getNotifications,
    enabled: isLoggedIn ?? false,
  });
};
