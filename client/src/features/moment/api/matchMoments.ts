import { api } from '@/app/lib/api';
import { MatchMomentsResponse } from '../types/moments';

export const matchMoments = async (): Promise<MatchMomentsResponse> => {
  const response = await api.get<MatchMomentsResponse>('/moments/matching');
  return response.data;
};
