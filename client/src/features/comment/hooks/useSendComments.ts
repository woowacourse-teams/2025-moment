import { useState } from 'react';
import { useSendCommentsMutation } from './useSendCommentsMutation';
import { useMatchMomentsQuery } from '@/features/moment/hook/useMatchMomentsQuery';

export const useSendComments = () => {
  const [commentsData, setCommentsData] = useState({
    content: '',
    momentId: 0,
  });

  const [errors, setErrors] = useState({
    content: '',
    momentId: '',
  });

  const { data: momentsData } = useMatchMomentsQuery();

  const { mutateAsync: sendComments, isPending, error, isError } = useSendCommentsMutation();

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setCommentsData(prev => ({ ...prev, content: e.target.value }));
  };

  const handleSubmit = async () => {
    try {
      await sendComments({
        content: commentsData.content,
        momentId: momentsData?.data.id || 0,
      });
    } catch {
      alert('코멘트 전송에 실패했습니다.');
    }
  };

  return {
    momentsData: momentsData?.data.content,
    commentsData,
    errors,
    isPending,
    isError,
    handleChange,
    handleSubmit,
  };
};
