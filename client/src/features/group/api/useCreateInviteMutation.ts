import { api } from '@/app/lib/api';
import { toast } from '@/shared/store/toast';
import { useMutation } from '@tanstack/react-query';
import { track } from '@/shared/lib/ga/track';
import { InviteResponse } from '../types/group';

export const useCreateInviteMutation = (groupId: number | string) => {

  return useMutation({
    mutationFn: async (): Promise<InviteResponse> => {
      const response = await api.post(`/groups/${groupId}/invite`);
      return response.data;
    },
    onSuccess: () => {
      track('invite_member', {});
      toast.success('초대 링크가 생성되었습니다!');
    },
    onError: () => {
      toast.error('초대 링크 생성에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
