import { api } from '@/app/lib/api';
import { postCommentsResponse } from '../types/postComments';

export const getComments = async (): Promise<postCommentsResponse> => {
  const response = await api.get<postCommentsResponse>('/comments/me');
  return response.data;
};
