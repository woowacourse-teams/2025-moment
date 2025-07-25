import { useMatchMomentsQuery } from '@/features/moment/hook/useMatchMomentsQuery';
import { useState } from 'react';
import { useSendCommentsMutation } from './useSendCommentsMutation';

export const useSendComments = () => {
  const [comment, setComment] = useState('');

  const [errors, setErrors] = useState({
    content: '',
    momentId: '',
  });

  const { data: momentsData } = useMatchMomentsQuery();

  const { mutateAsync: sendComments, isPending, error, isError } = useSendCommentsMutation();

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setComment(e.target.value);
  };

  const handleSubmit = async () => {
    try {
      await sendComments({
        content: comment,
        momentId: momentsData?.data.id || 0,
      });
    } catch (error) {
      console.error(error);
    }
  };

  return {
    momentsData: momentsData?.data.content,
    comment,
    errors,
    isPending,
    isError,
    handleChange,
    handleSubmit,
  };
};
