import { api } from '@/app/lib/api';

export const sendExtraMoments = async (content: string) => {
  const response = await api.post('/moments/extra', { content });
  return response.data;
};
