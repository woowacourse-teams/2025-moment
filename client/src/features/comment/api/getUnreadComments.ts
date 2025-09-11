import { api } from '@/app/lib/api';
import { UnreadCommentsResponse } from '../types/comments';

interface GetUnreadComments {
  pageParam?: string | null;
}

export const getUnreadComments = async ({
  pageParam,
}: GetUnreadComments): Promise<UnreadCommentsResponse> => {
  const params = new URLSearchParams();
  if (pageParam) {
    params.append('nextCursor', pageParam);
  }
  params.append('pageSize', '10');

  const response = await api.get(`/comments/me/unread?${params.toString()}`);
  return response.data;
};
