import { useMutation } from '@tanstack/react-query';
import { patchNotifications } from '../api/patchNotifications';
import { queryClient } from '@/app/lib/queryClient';

export const useNotificationsMutation = () => {
  return useMutation({
    mutationFn: patchNotifications,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
};
