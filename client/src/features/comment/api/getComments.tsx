import { api } from '@/app/lib/api';
import { CommentsResponse } from '../types/comments';

interface GetComments {
  pageParam?: string | null;
}

export const getComments = async ({ pageParam }: GetComments): Promise<CommentsResponse> => {
  const params = new URLSearchParams();
  if (pageParam) {
    params.append('nextCursor', pageParam);
  }
  params.append('pageSize', '10');

  const response = await api.get<CommentsResponse>(`/comments/me?${params.toString()}`);
  return response.data;
};
