import { api } from '@/app/lib/api';
import { useQuery } from '@tanstack/react-query';

export interface GroupMoment {
  id: number;
  userId: number;
  nickname: string;
  content: string;
  createdAt: string;
  imageUrl?: string | null;
  likeCount: number;
  commentCount: number;
  isLiked: boolean;
  tagNames: string[];
}

export interface GroupMomentsResponse {
  status: number;
  data: {
    items: GroupMoment[];
    nextCursor: string | null;
    hasNextPage: boolean;
    pageSize: number;
  };
}

export const useGroupMomentsQuery = (groupId: number | string) => {
  return useQuery({
    queryKey: ['group', groupId, 'moments'],
    queryFn: async (): Promise<GroupMomentsResponse> => {
      const response = await api.get(`/v2/groups/${groupId}/moments`);
      return response.data;
    },
    enabled: !!groupId,
  });
};
