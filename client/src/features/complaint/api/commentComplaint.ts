import { api } from '@/app/lib/api';
import { ComplaintFormData } from '../types/complaintType';

export const commentComplaint = async ({ targetId, reason }: ComplaintFormData) => {
  const response = await api.post(`/comments/${targetId}/reports`, { reason });
  return response.data;
};
