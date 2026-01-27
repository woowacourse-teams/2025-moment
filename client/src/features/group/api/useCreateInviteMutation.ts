import { api } from '@/app/lib/api';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { InviteResponse } from '../types/group';

export const useCreateInviteMutation = (groupId: number | string) => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: async (): Promise<InviteResponse> => {
      const response = await api.post(`/groups/${groupId}/invite`);
      return response.data;
    },
    onSuccess: () => {
      showSuccess('초대 링크가 생성되었습니다!');
    },
    onError: () => {
      showError('초대 링크 생성에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
