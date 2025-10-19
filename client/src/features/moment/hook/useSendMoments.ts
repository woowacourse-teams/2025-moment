import { useState } from 'react';
import { useMomentsMutation } from './useMomentsMutation';
import { track } from '@/shared/lib/ga/track';
import { useEffect } from 'react';

export const useSendMoments = () => {
  const [content, setContent] = useState('');
  const [imageData, setImageData] = useState<{ imageUrl: string; imageName: string } | null>(null);
  const [tagNames, setTagNames] = useState<string[]>([]);

  const { mutateAsync: sendMoments, isSuccess } = useMomentsMutation();

  const handleContentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const newContent = e.target.value;
    setContent(newContent);
  };

  const handleImageChange = (newImageData: { imageUrl: string; imageName: string } | null) => {
    setImageData(newImageData);
  };
  const handleTagNameClick = (tagName: string) => {
    if (tagNames.includes(tagName)) {
      setTagNames(tagNames.filter(tag => tag !== tagName));
      return;
    }
    setTagNames([...tagNames, tagName]);
  };

  const handleSendContent = async () => {
    try {
      const payload = imageData
        ? { content, tagNames, imageUrl: imageData.imageUrl, imageName: imageData.imageName }
        : { content, tagNames };

      await sendMoments(payload);
    } catch (error) {
      console.error('Error sending moments:', error);
    }
  };

  useEffect(() => {
    return () => {
      const typed = content.trim().length > 0 || imageData != null || tagNames.length > 0;
      if (!isSuccess && typed) {
        const len = content.length;
        const content_length_bucket = len <= 60 ? 's' : len <= 140 ? 'm' : 'l';
        const has_media = Boolean(imageData);
        const mood_tag = tagNames?.[0];
        track('abandon_composer', {
          stage: 'typed',
          composer: 'moment',
          has_media,
          content_length_bucket,
          ...(mood_tag ? { mood_tag } : {}),
        });
      }
    };
  }, [content, imageData, tagNames, isSuccess]);

  return {
    handleContentChange,
    handleImageChange,
    handleSendContent,
    handleTagNameClick,
    content,
    imageData,
    tagNames,
    isSuccess,
  };
};
