import { useState } from 'react';
import { useMomentsMutation } from '../api/useMomentsMutation';
import { track } from '@/shared/lib/ga/track';
import { useEffect } from 'react';

export const useSendMoments = (groupId: string | undefined) => {
  const [content, setContent] = useState('');
  const [imageData, setImageData] = useState<{ imageUrl: string; imageName: string } | null>(null);

  const { mutateAsync: sendMoments, isSuccess } = useMomentsMutation(groupId || '');

  const handleContentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const newContent = e.target.value;
    setContent(newContent);
  };

  const handleImageChange = (newImageData: { imageUrl: string; imageName: string } | null) => {
    setImageData(newImageData);
  };

  const handleSendContent = async () => {
    if (!groupId) {
      console.error('No group selected');
      return;
    }

    try {
      const payload = imageData
        ? { content, imageUrl: imageData.imageUrl, imageName: imageData.imageName }
        : { content };

      await sendMoments(payload);
    } catch (error) {
      console.error('Error sending moments:', error);
    }
  };

  useEffect(() => {
    return () => {
      const typed = content.trim().length > 0 || imageData != null;
      if (!isSuccess && typed) {
        const length = content.length;
        const content_length_bucket = length <= 60 ? 's' : length <= 140 ? 'm' : 'l';
        const has_media = Boolean(imageData);
        track('abandon_composer', {
          composer: 'moment',
          has_media,
          content_length_bucket,
        });
      }
    };
  }, [content, imageData, isSuccess]);

  return {
    handleContentChange,
    handleImageChange,
    handleSendContent,
    content,
    imageData,
    isSuccess,
  };
};
