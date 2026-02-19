import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { toast } from '@/shared/store/toast';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation } from '@tanstack/react-query';

export const useDeleteMomentMutation = (groupId: number | string) => {

  return useMutation({
    mutationFn: (momentId: number) => deleteMoment(groupId, momentId),
    onSuccess: () => {
      const numericGroupId = Number(groupId);
      queryClient.invalidateQueries({ queryKey: queryKeys.group.moments(numericGroupId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.group.myMoments(numericGroupId) });
      toast.success('모멘트가 삭제되었습니다.');
    },
    onError: () => {
      toast.error('모멘트 삭제에 실패했습니다.');
    },
  });
};

const deleteMoment = async (groupId: number | string, momentId: number) => {
  const response = await api.delete(`/groups/${groupId}/moments/${momentId}`);
  return response.data;
};
