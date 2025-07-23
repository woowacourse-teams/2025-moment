import { useState } from 'react';
import { useMomentsMutation } from './useMomentsMutation';

export const useSendMoments = () => {
  const [content, setContent] = useState('');

  const { mutateAsync: sendMoments, isSuccess, reset } = useMomentsMutation();

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

  const handleReset = () => {
    reset();
    setContent('');
  };

  return {
    handleContentChange,
    handleSendContent,
    handleReset,
    content,
    isSuccess,
  };
};
