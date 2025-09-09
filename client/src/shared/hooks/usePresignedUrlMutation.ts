import { api } from '@/app/lib/api';
import { useMutation } from '@tanstack/react-query';

interface PresignedUrlRequest {
  imageName: string;
  imageType: string;
}

interface PresignedUrlResponse {
  presignedUrl: string;
  filePath: string;
}

export const usePresignedUrlMutation = () => {
  return useMutation({
    mutationFn: (data: PresignedUrlRequest) => getPresignedUrl(data),
    onError: error => {
      console.error('Failed to get presigned URL:', error);
    },
  });
};

const getPresignedUrl = async (data: PresignedUrlRequest): Promise<PresignedUrlResponse> => {
  const response = await api.post('/storage/upload-url', data);
  return response.data.data;
};
