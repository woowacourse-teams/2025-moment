import { useState } from 'react';
import { useMomentsExtraMutation } from './useMomentsExtraMutation';

export const useSendExtraMoments = () => {
  const [content, setContent] = useState('');
  const [imageData, setImageData] = useState<{ imageUrl: string; imageName: string } | null>(null);
  const [tagNames, setTagNames] = useState<string[]>([]);

  const { mutateAsync: sendExtraMoments, isSuccess } = useMomentsExtraMutation();

  const handleExtraContentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const newContent = e.target.value;
    setContent(newContent);
  };

  const handleExtraImageChange = (newImageData: { imageUrl: string; imageName: string } | null) => {
    setImageData(newImageData);
  };
  const handleTagNameClick = (tagName: string) => {
    setTagNames([...tagNames, tagName]);
  };

  const handleSendExtraContent = async () => {
    try {
      const payload = imageData
        ? { content, tagNames, imageUrl: imageData.imageUrl, imageName: imageData.imageName }
        : { content, tagNames };

      await sendExtraMoments(payload);
    } catch (error) {
      console.error('Error sending moments:', error);
    }
  };

  return {
    handleExtraContentChange,
    handleExtraImageChange,
    handleSendExtraContent,
    handleTagNameClick,
    content,
    imageData,
    tagNames,
    isSuccess,
  };
};
