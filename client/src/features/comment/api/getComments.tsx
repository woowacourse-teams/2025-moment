import { api } from '@/app/lib/api';
import { MyMomentsResponse } from '../types/myMoments';

export const getComments = async (): Promise<MyMomentsResponse> => {
  const response = await api.get<MyMomentsResponse>('/comments/me');
  return response.data;
};
