import { useMutation } from '@tanstack/react-query';
import { changeNickname } from '../api/changeNickname';
import { queryClient } from '@/app/lib/queryClient';
import { ChangeNicknameRequest } from '../types/changeNickname';
import { useToast } from '@/shared/hooks';

export const useChangeNicknameMutation = () => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: ({ newNickname }: ChangeNicknameRequest) => changeNickname({ newNickname }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my', 'profile'] });
      showSuccess('닉네임이 변경되었습니다.');
    },
    onError: () => {
      showError('닉네임 변경에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
