import { useState } from 'react';
import { useMomentsMutation } from './useMomentsMutation';

export const useSendMoments = () => {
  const [content, setContent] = useState('');

  const { mutateAsync: sendMoments, isSuccess } = useMomentsMutation();

  const handleContentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const newContent = e.target.value;
    setContent(newContent);
  };

  const handleSendContent = async () => {
    try {
      await sendMoments(content);
    } catch (error) {
      console.error('Error sending moments:', error);
    }
  };

  return {
    handleContentChange,
    handleSendContent,
    content,
    isSuccess,
  };
};
