import { api } from '@/app/lib/api';
import { matchMomentsResponse } from '../types/moments';

export const matchMoments = async (): Promise<matchMomentsResponse> => {
  const response = await api.get<matchMomentsResponse>('/moments/matching');
  return response.data;
};
