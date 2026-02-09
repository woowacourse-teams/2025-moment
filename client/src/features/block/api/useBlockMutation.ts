import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { queryKeys } from '@/shared/lib/queryKeys';

export const useBlockMutation = () => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: async (userId: number) => {
      const response = await api.post(`/users/${userId}/blocks`);
      return response.data;
    },
    onSuccess: () => {
      showSuccess('사용자가 차단되었습니다.');
      queryClient.invalidateQueries({ queryKey: queryKeys.blocks.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.commentableMoments.all });
    },
    onError: () => {
      showError('차단에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
