import { api } from '@/app/lib/api';
import { EchoRequest, EchoResponse } from '@/features/echo/type/echos';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useToast } from '@/shared/hooks/useToast';
import { track } from '@/shared/lib/ga/track';

export const useEchoMutation = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: sendEcho,
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['moments'] });
      showSuccess('에코가 성공적으로 전송되었습니다!');
      track('give_empathy', {
        item_id: String(variables.commentId),
        source: 'detail',
      });
    },
    onError: () => {
      showError('에코 전송에 실패했습니다. 다시 시도해주세요.');
    },
  });
};

const sendEcho = async (echos: EchoRequest): Promise<EchoResponse> => {
  const response = await api.post('/echos', echos);
  return response.data;
};
