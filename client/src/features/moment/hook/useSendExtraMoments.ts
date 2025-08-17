import { useState } from 'react';
import { useMomentsMutation } from './useMomentsMutation';

export const useSendExtraMoments = () => {
  const [content, setContent] = useState('');

  const { mutateAsync: sendExtraMoments, isSuccess } = useMomentsMutation();

  const handleExtraContentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const newContent = e.target.value;
    setContent(newContent);
  };

  const handleSendExtraContent = async () => {
    try {
      await sendExtraMoments(content);
    } catch (error) {
      console.error('Error sending moments:', error);
    }
  };

  return {
    handleExtraContentChange,
    handleSendExtraContent,
    content,
    isSuccess,
  };
};
