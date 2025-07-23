import { useState } from 'react';
import { useSendCommentsMutation } from './useSendCommentsMutation';

export const useSend = () => {
  const [commentsData, setCommentsData] = useState({
    comment: '',
    momentId: '',
  });

  const [errors, setErrors] = useState({
    comment: '',
    momentId: '',
  });

  const { mutateAsync: sendComments, isPending, error, isError } = useSendCommentsMutation();

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setCommentsData(prev => ({ ...prev, comment: e.target.value }));
  };

  const handleSubmit = async () => {
    try {
      await sendComments(commentsData);
    } catch (error) {
      console.error('Send comments failed:', error);
    }
  };

  return {
    commentsData,
    errors,
    isPending,
    isError,
    handleChange,
    handleSubmit,
  };
};
