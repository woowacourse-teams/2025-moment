import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';

export interface CreateGroupMomentRequest {
  content: string;
  tagNames?: string[];
  imageUrl?: string;
}

export interface CreateGroupMomentResponse {
  status: number;
  data: {
    id: number;
    content: string;
    createdAt: string;
  };
}

export const useCreateGroupMomentMutation = (groupId: number | string) => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: async (data: CreateGroupMomentRequest): Promise<CreateGroupMomentResponse> => {
      const response = await api.post(`/v2/groups/${groupId}/moments`, data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'moments'] });
      queryClient.invalidateQueries({ queryKey: ['group', groupId, 'my-moments'] });
      showSuccess('모멘트가 작성되었습니다!');
    },
    onError: () => {
      showError('모멘트 작성에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
