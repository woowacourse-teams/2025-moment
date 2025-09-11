import { ImageUploadData } from '@/shared/types/upload';
import { useState } from 'react';
import { useSendCommentsMutation } from '../api/useSendCommentsMutation';
import { checkProfanityWord } from '@/converter/util/checkProfanityWord';
import { useToast } from '@/shared/hooks/useToast';

export const useSendComments = (momentId: number) => {
  const [comment, setComment] = useState('');
  const { showError } = useToast();
  const [imageData, setImageData] = useState<ImageUploadData | null>(null);

  const { mutateAsync: sendComments, isPending, error, isError } = useSendCommentsMutation();

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setComment(e.target.value);
  };

  const handleImageChange = (imageUploadData: ImageUploadData | null) => {
    setImageData(imageUploadData);
  };

  const handleSubmit = async () => {
    if (checkProfanityWord(comment)) {
      showError('코멘트에 부적절한 단어가 포함되어 있습니다.');
      return;
    }
    await sendComments({
      content: comment,
      momentId: momentId,
      imageUrl: imageData?.imageUrl,
      imageName: imageData?.imageName,
    });
  };

  return {
    error,
    comment,
    imageData,
    isPending,
    isError,
    handleChange,
    handleImageChange,
    handleSubmit,
  };
};
