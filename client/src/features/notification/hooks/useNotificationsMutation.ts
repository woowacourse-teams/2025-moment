import { useMutation, useQueryClient } from '@tanstack/react-query';
import { patchNotifications } from '../api/patchNotifications';
import { useToast } from '@/shared/hooks/useToast';

export const useNotificationsMutation = () => {
  const queryClient = useQueryClient();
  const { showError } = useToast();

  return useMutation({
    mutationFn: patchNotifications,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
    onError: error => {
      console.error('알림 읽음 처리 실패:', error);
      showError('알림 처리 중 문제가 발생했습니다. 다시 시도해 주세요.');
    },
  });
};
