import { api } from '@/app/lib/api';
import { CommentsResponse, GetComments } from '../types/comments';

export const getUnreadComments = async ({ pageParam }: GetComments): Promise<CommentsResponse> => {
  const params = new URLSearchParams();
  if (pageParam) {
    params.append('nextCursor', pageParam);
  }
  params.append('pageSize', '10');

  const response = await api.get(`/comments/me/unread?${params.toString()}`);
  return response.data;
};
