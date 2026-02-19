import { api } from '@/app/lib/api';
import { toast } from '@/shared/store/toast';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation, useQueryClient } from '@tanstack/react-query';

export const useReadNotificationsMutation = (groupId?: number | string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: patchReadNotifications,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications.all });
      if (groupId) {
        const numericGroupId = Number(groupId);
        queryClient.invalidateQueries({ queryKey: queryKeys.group.comments(numericGroupId) });
        queryClient.invalidateQueries({
          queryKey: queryKeys.group.commentsUnread(numericGroupId),
        });
      }
    },
    onError: error => {
      console.error('알림 읽음 처리 실패:', error);
      toast.error('알림 처리 중 문제가 발생했습니다. 다시 시도해 주세요.');
    },
  });
};

const patchReadNotifications = async (notificationId: number): Promise<void> => {
  await api.patch(`/notifications/${notificationId}/read`);
};
