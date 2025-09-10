import { useState } from 'react';
import { useMomentsExtraMutation } from './useMomentsExtraMutation';

export const useSendExtraMoments = () => {
  const [content, setContent] = useState('');
  const [tagNames, setTagNames] = useState<string[]>([]);

  const { mutateAsync: sendExtraMoments, isSuccess } = useMomentsExtraMutation();

  const handleExtraContentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
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

  const handleSendExtraContent = async () => {
    try {
      await sendExtraMoments({ content: content, tagNames: tagNames });
    } catch (error) {
      console.error('Error sending moments:', error);
    }
  };

  return {
    handleExtraContentChange,
    handleTagNameClick,
    handleSendExtraContent,
    content,
    tagNames,
    isSuccess,
  };
};
