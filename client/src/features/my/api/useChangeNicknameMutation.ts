import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { toast } from '@/shared/store/toast';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useMutation } from '@tanstack/react-query';
import { ChangeNicknameRequest } from '../types/changeNickname';

export const useChangeNicknameMutation = () => {

  return useMutation({
    mutationFn: ({ newNickname }: ChangeNicknameRequest) => changeNickname({ newNickname }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.auth.profile });
      queryClient.invalidateQueries({ queryKey: queryKeys.my.profile });
      toast.success('닉네임이 변경되었습니다.');
    },
    onError: () => {
      toast.error('닉네임 변경에 실패했습니다. 다시 시도해주세요.');
    },
  });
};

const changeNickname = async ({ newNickname }: ChangeNicknameRequest) => {
  const response = await api.post('/me/nickname', { newNickname });
  return response.data;
};
