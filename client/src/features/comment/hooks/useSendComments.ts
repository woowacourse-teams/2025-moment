import { useState } from 'react';
import { useSendCommentsMutation } from '../api/useSendCommentsMutation';

export const useSendComments = () => {
  const [comment, setComment] = useState('');

  const { mutateAsync: sendComments, isPending, error, isError } = useSendCommentsMutation();

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setComment(e.target.value);
  };

  const handleSubmit = async (momentId: number) => {
    await sendComments({
      content: comment,
      momentId: momentId,
    });
  };

  return {
    error,
    comment,
    isPending,
    isError,
    handleChange,
    handleSubmit,
  };
};
