import { toast } from '@/shared/store/toast';
import { useMutation } from '@tanstack/react-query';
import { ComplaintFormData } from '../types/complaintType';
import { api } from '@/app/lib/api';

export const useComplaintMutation = () => {

  return useMutation({
    mutationFn: async (data: ComplaintFormData) => {
      if (data.targetType === 'MOMENT') {
        return await momentComplaint(data);
      } else if (data.targetType === 'COMMENT') {
        return await commentComplaint(data);
      }
    },
    onSuccess: () => {
      toast.success('신고가 접수되었습니다.');
    },
    onError: () => {
      toast.error('신고에 실패했습니다. 다시 시도해주세요.');
    },
  });
};

const commentComplaint = async ({ targetId, reason }: ComplaintFormData) => {
  const response = await api.post(`/comments/${targetId}/reports`, { reason });
  return response.data;
};

const momentComplaint = async ({ targetId, reason }: ComplaintFormData) => {
  const response = await api.post(`/moments/${targetId}/reports`, { reason });
  return response.data;
};
