import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { ComplaintFormData } from '../types/complaintType';
import { momentComplaint } from './momentComplaint';
import { commentComplaint } from './commentComplaint';

export const useComplaintMutation = () => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: async (data: ComplaintFormData) => {
      if (data.targetType === 'MOMENT') {
        return await momentComplaint(data);
      } else {
        return await commentComplaint(data);
      }
    },
    onSuccess: () => {
      showSuccess('신고가 접수되었습니다.');
    },
    onError: () => {
      showError('신고에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
