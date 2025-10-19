import { ImageUploadData } from '@/shared/types/upload';
import { useState } from 'react';
import { useSendCommentsMutation } from '../api/useSendCommentsMutation';
import { checkProfanityWord } from '@/converter/util/checkProfanityWord';
import { useToast } from '@/shared/hooks/useToast';
import { useEffect } from 'react';
import { track } from '@/shared/lib/ga/track';

export const useSendComments = (momentId: number) => {
  const [comment, setComment] = useState('');
  const { showError } = useToast();
  const [imageData, setImageData] = useState<ImageUploadData | null>(null);

  const {
    mutateAsync: sendComments,
    isPending,
    error,
    isError,
    isSuccess,
  } = useSendCommentsMutation();

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

  useEffect(() => {
    return () => {
      const typed = comment.trim().length > 0 || imageData != null;
      if (!isSuccess && typed) {
        const len = comment.length;
        const length_bucket = len <= 60 ? 's' : len <= 140 ? 'm' : 'l';
        track('abandon_composer', {
          stage: 'typed',
          composer: 'comment',
          content_length_bucket: length_bucket,
          has_media: Boolean(imageData),
        });
      }
    };
  }, [comment, imageData, isSuccess]);

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
