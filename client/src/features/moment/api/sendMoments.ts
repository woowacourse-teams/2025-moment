import { api } from '@/app/lib/api';
import type { MomentsRequest } from '../types/moments';

export const sendMoments = async ({ content, tagNames }: MomentsRequest) => {
  const response = await api.post('/moments', {
    content,
    tagNames,
  });
  return response.data;
};
