import { api } from '@/app/lib/api';

interface SendMomentsData {
  content: string;
  imageUrl?: string;
  imageName?: string;
}

export const sendMoments = async (data: SendMomentsData) => {
  const payload: { content: string; imageUrl?: string; imageName?: string } = {
    content: data.content,
  };

  if (data.imageUrl && data.imageName) {
    payload.imageUrl = data.imageUrl;
    payload.imageName = data.imageName;
  }

  const response = await api.post('/moments', payload);
  return response.data;
};
