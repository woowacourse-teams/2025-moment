import { useMatchMomentsQuery } from '@/features/moment/hook/useMatchMomentsQuery';
import { useState } from 'react';
import { useSendCommentsMutation } from './useSendCommentsMutation';

export const useSendComments = () => {
  const [comment, setComment] = useState('');

  const { data: momentsData } = useMatchMomentsQuery();

  const { mutateAsync: sendComments, isPending, error, isError } = useSendCommentsMutation();

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setComment(e.target.value);
  };

  const handleSubmit = async () => {
    await sendComments({
      content: comment,
      momentId: momentsData?.data.id || 0,
    });
  };

  return {
    momentsData: momentsData?.data.content,
    error,
    comment,
    isPending,
    isError,
    handleChange,
    handleSubmit,
  };
};
