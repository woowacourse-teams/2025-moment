import { api } from '@/app/lib/api';
import { sendCommentsData, sendCommentsResponse } from '../types/sendComments';

export const sendComments = async (
  commentsData: sendCommentsData,
): Promise<sendCommentsResponse> => {
  const response = await api.post('/comments', { commentsData });
  return response.data;
};
