import { api } from '@/app/lib/api';
import { ComplaintFormData } from '../types/complaintType';

export const momentComplaint = async ({ targetId, reason }: ComplaintFormData) => {
  const response = await api.post(`/moments/${targetId}/reports`, { reason });
  return response.data;
};
