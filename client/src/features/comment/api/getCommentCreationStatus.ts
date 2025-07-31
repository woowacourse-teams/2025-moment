import { api } from '@/app/lib/api';
import { CommentCreationStatusResponse } from '../types/comments';

export const getCommentCreationStatus = async (): Promise<CommentCreationStatusResponse> => {
  const response = await api.get<CommentCreationStatusResponse>('/comments/me/creation-status');
  return response.data;
};
