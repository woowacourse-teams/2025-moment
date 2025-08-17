import { api } from '@/app/lib/api';
import { useQuery } from '@tanstack/react-query';

interface GetCommentableMomentsResponse {
  status: number;
  data: GetCommentableMoments;
}

interface GetCommentableMoments {
  id: number;
  nickname: string;
  level: string;
  content: string;
  createdAt: string;
}

export const useCommentableMomentsQuery = () => {
  return useQuery({
    queryKey: ['commentableMoments'],
    queryFn: getCommentableMoments,
  });
};

const getCommentableMoments = async (): Promise<GetCommentableMoments> => {
  const response = await api.get<GetCommentableMomentsResponse>('/moments/commentable');
  return response.data.data;
};
