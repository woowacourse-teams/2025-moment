import { useState } from 'react';
import { useMomentsMutation } from './useMomentsMutation';

export const useSendMoments = () => {
  const [content, setContent] = useState('');
  const [imageData, setImageData] = useState<{ imageUrl: string; imageName: string } | null>(null);

  const { mutateAsync: sendMoments, isSuccess } = useMomentsMutation();

  const handleContentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const newContent = e.target.value;
    setContent(newContent);
  };

  const handleImageChange = (newImageData: { imageUrl: string; imageName: string } | null) => {
    setImageData(newImageData);
  };

  const handleSendContent = async () => {
    try {
      const payload = imageData
        ? { content, imageUrl: imageData.imageUrl, imageName: imageData.imageName }
        : { content };

      await sendMoments(payload);
    } catch (error) {
      console.error('Error sending moments:', error);
    }
  };

  return {
    handleContentChange,
    handleImageChange,
    handleSendContent,
    content,
    imageData,
    isSuccess,
  };
};
