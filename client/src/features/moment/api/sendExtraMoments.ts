import { api } from '@/app/lib/api';
import type { MomentsRequest } from '../types/moments';

export const sendExtraMoments = async ({ content, tagNames }: MomentsRequest) => {
  const response = await api.post('/moments/extra', {
    content,
    tagNames,
  });
  return response.data;
};
