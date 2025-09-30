import { useToast } from '@/shared/hooks/useToast';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { patchAllNotifications } from '../api/patchNotifications';

export const useAllNotificationsMutation = () => {
  const queryClient = useQueryClient();
  const { showError } = useToast();

  return useMutation({
    mutationFn: patchAllNotifications,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['comments', 'unread'] });
      queryClient.invalidateQueries({ queryKey: ['comments'] });
      queryClient.invalidateQueries({ queryKey: ['moments'] });
    },
    onError: error => {
      console.error('알림 읽음 처리 실패:', error);
      showError('알림 처리 중 문제가 발생했습니다. 다시 시도해 주세요.');
    },
  });
};
