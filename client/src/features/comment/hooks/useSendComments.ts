import { useState } from 'react';
import { useSendCommentsMutation } from '../api/useSendCommentsMutation';

export const useSendComments = (momentId: number) => {
  const [comment, setComment] = useState('');
  const [isSuccess, setIsSuccess] = useState(false);

  const { mutateAsync: sendComments, isPending, error, isError } = useSendCommentsMutation();

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setComment(e.target.value);
  };

  const handleSubmit = async () => {
    await sendComments({
      content: comment,
      momentId: momentId,
    });
    setIsSuccess(true);
  };

  return {
    error,
    comment,
    isSuccess,
    isPending,
    isError,
    handleChange,
    handleSubmit,
  };
};
