import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { sendExtraMoments } from '../api/sendExtraMoments';

export const useMomentsExtraMutation = () => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: sendExtraMoments,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['moments'] });
      // TODO: 추후 별조각 포인트 invalidateQueries 추가해야 함
      showSuccess('추가 모멘트가 성공적으로 등록되었습니다!');
    },
    onError: () => {
      const errorMessage = '추가 모멘트 등록에 실패했습니다. 다시 시도해주세요.';
      showError(errorMessage);
    },
  });
};
