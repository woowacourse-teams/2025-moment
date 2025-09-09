import { api } from '@/app/lib/api';

interface SendExtraMomentsData {
  content: string;
  imageUrl?: string;
  imageName?: string;
}

export const sendExtraMoments = async (data: SendExtraMomentsData) => {
  const payload: { content: string; imageUrl?: string; imageName?: string } = {
    content: data.content,
  };

  if (data.imageUrl && data.imageName) {
    payload.imageUrl = data.imageUrl;
    payload.imageName = data.imageName;
  }

  const response = await api.post('/moments/extra', payload);
  return response.data;
};
