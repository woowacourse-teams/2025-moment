import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { sendMoments } from '../api/sendMoments';

export const useMomentsMutation = () => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: sendMoments,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['moments'] });
      queryClient.invalidateQueries({ queryKey: ['momentWritingStatus'] });
      showSuccess('모멘트가 성공적으로 등록되었습니다!');
    },
    onError: () => {
      const errorMessage = '모멘트 등록에 실패했습니다. 다시 시도해주세요.';
      showError(errorMessage);
    },
  });
};
