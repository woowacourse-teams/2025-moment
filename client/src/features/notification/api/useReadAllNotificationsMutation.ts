import { api } from '@/app/lib/api';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation, useQueryClient } from '@tanstack/react-query';

export const useReadAllNotificationsMutation = (groupId?: number | string) => {
  const queryClient = useQueryClient();
  const { showError } = useToast();

  return useMutation({
    mutationFn: patchReadAllNotifications,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      if (groupId) {
        const numericGroupId = Number(groupId);
        queryClient.invalidateQueries({ queryKey: ['group', numericGroupId, 'comments'] });
        queryClient.invalidateQueries({ queryKey: ['group', numericGroupId, 'moments'] });
        queryClient.invalidateQueries({ queryKey: ['group', numericGroupId, 'my-moments'] });
      }
    },
    onError: error => {
      console.error('알림 읽음 처리 실패:', error);
      showError('알림 처리 중 문제가 발생했습니다. 다시 시도해 주세요.');
    },
  });
};

const patchReadAllNotifications = async (notificationIds: number[]): Promise<void> => {
  await api.patch('/notifications/read-all', { notificationIds });
};
