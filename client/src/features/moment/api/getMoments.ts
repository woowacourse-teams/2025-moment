import { api } from '@/app/lib/api';
import { MomentsResponse } from '../types/moments';

export const getMoments = async (): Promise<MomentsResponse> => {
  const response = await api.get('/moments/me');
  return response.data;
};
