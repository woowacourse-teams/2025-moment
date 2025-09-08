import { useState } from 'react';
import { useMomentsMutation } from './useMomentsMutation';

export const useSendMoments = () => {
  const [content, setContent] = useState('');
  const [tagNames, setTagNames] = useState<string[]>([]);

  const { mutateAsync: sendMoments, isSuccess } = useMomentsMutation();

  const handleContentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const newContent = e.target.value;
    setContent(newContent);
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
      await sendMoments({ content: content, tagNames: tagNames });
    } catch (error) {
      console.error('Error sending moments:', error);
    }
  };

  return {
    handleContentChange,
    handleTagNameClick,
    handleSendContent,
    content,
    tagNames,
    isSuccess,
  };
};
