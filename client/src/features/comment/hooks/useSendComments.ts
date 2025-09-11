import { ImageUploadData } from '@/shared/types/upload';
import { useState } from 'react';
import { useSendCommentsMutation } from '../api/useSendCommentsMutation';

export const useSendComments = (momentId: number) => {
  const [comment, setComment] = useState('');
  const [imageData, setImageData] = useState<ImageUploadData | null>(null);

  const { mutateAsync: sendComments, isPending, error, isError } = useSendCommentsMutation();

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setComment(e.target.value);
  };

  const handleImageChange = (imageUploadData: ImageUploadData | null) => {
    setImageData(imageUploadData);
  };

  const handleSubmit = async () => {
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
