import { queryClient } from '@/app/lib/queryClient';
import { sendMoments } from '../api/sendMoments';
import { useMutation } from '@tanstack/react-query';

export const useMomonetsMutation = () => {
  return useMutation({
    mutationFn: sendMoments,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['moments'] });
    },
  });
};
