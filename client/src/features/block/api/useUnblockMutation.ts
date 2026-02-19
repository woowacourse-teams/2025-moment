import { toast } from '@/shared/store/toast';
import { useMutation } from '@tanstack/react-query';
import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { queryKeys } from '@/shared/lib/queryKeys';

export const useUnblockMutation = () => {

  return useMutation({
    mutationFn: async (userId: number) => {
      const response = await api.delete(`/users/${userId}/blocks`);
      return response.data;
    },
    onSuccess: () => {
      toast.success('차단이 해제되었습니다.');
      queryClient.invalidateQueries({ queryKey: queryKeys.blocks.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.commentableMoments.all });
    },
    onError: () => {
      toast.error('차단 해제에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
