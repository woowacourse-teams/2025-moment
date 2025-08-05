import { api } from '@/app/lib/api';
import type { MomentsResponse } from '../types/moments';

export const getMoments = async (): Promise<MomentsResponse> => {
  const response = await api.get('/moments/me');
  return response.data;
};
