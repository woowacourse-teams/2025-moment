import { useState } from 'react';
import { useSendCommentsMutation } from './useSendCommentsMutation';

export const useSend = () => {
  const [commentsData, setCommentsData] = useState({
    content: '',
    momentId: 11,
  });

  const [errors, setErrors] = useState({
    content: '',
    momentId: '',
  });

  const { mutateAsync: sendComments, isPending, error, isError } = useSendCommentsMutation();

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setCommentsData(prev => ({ ...prev, content: e.target.value }));
  };

  const handleSubmit = async () => {
    try {
      await sendComments({
        content: commentsData.content,
        momentId: commentsData.momentId,
      });
    } catch {
      alert('코멘트 전송에 실패했습니다.');
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
