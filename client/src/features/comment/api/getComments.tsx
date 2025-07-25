import { api } from '@/app/lib/api';
import { CommentsResponse } from '../types/comments';

export const getComments = async (): Promise<CommentsResponse> => {
  const response = await api.get<CommentsResponse>('/comments/me');
  return response.data;
};
