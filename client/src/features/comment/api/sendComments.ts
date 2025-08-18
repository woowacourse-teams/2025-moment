import { api } from '@/app/lib/api';
import { SendCommentsData, SendCommentsResponse } from '../types/comments';

export const sendComments = async (
  commentsData: SendCommentsData,
): Promise<SendCommentsResponse> => {
  const response = await api.post('/comments', commentsData);
  return response.data;
};
