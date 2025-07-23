import { api } from '@/app/lib/api';

export const sendMoments = async (content: string) => {
  const response = await api.post('/moments', {
    content,
  });
  return response.data;
};
