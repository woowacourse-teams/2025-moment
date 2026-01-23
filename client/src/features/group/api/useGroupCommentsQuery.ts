import { api } from '@/app/lib/api';
import { useQuery } from '@tanstack/react-query';

export interface GroupComment {
  id: number;
  userId: number;
  nickname: string;
  content: string;
  createdAt: string;
  imageUrl?: string | null;
  likeCount: number;
  isLiked: boolean;
  level?: string;
}

export interface GroupCommentsResponse {
  status: number;
  data: GroupComment[];
}

export const useGroupCommentsQuery = (groupId: number | string, momentId: number) => {
  return useQuery({
    queryKey: ['group', groupId, 'moment', momentId, 'comments'],
    queryFn: async (): Promise<GroupCommentsResponse> => {
      const response = await api.get(`/v2/groups/${groupId}/moments/${momentId}/comments`);
      return response.data;
    },
    enabled: !!groupId && !!momentId,
  });
};
