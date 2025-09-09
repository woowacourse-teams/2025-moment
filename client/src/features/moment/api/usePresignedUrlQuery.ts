import { useMutation } from '@tanstack/react-query';
import { getPresignedUrl, PresignedUrlRequest } from './getPresignedUrl';

export const usePresignedUrlMutation = () => {
  return useMutation({
    mutationFn: (data: PresignedUrlRequest) => getPresignedUrl(data),
    onError: error => {
      console.error('Failed to get presigned URL:', error);
    },
  });
};
