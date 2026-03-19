import { ImageUploadData } from '@/shared/types/upload';
import { useState } from 'react';
import { useSendCommentsMutation } from '../api/useSendCommentsMutation';
import { checkProfanityWord } from '@/shared/utils/checkProfanityWord';
import { toast } from '@/shared/store/toast';
import { useEffect } from 'react';
import { track } from '@/shared/lib/ga/track';

interface UseSendCommentsProps {
  groupId: number | string;
  momentId: number;
}

export const useSendComments = ({ groupId, momentId }: UseSendCommentsProps) => {
  const [comment, setComment] = useState('');
  const [imageData, setImageData] = useState<ImageUploadData | null>(null);

  const {
    mutateAsync: sendComments,
    isPending,
    error,
    isError,
    isSuccess,
  } = useSendCommentsMutation(groupId, momentId);

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setComment(e.target.value);
  };

  const handleImageChange = (imageUploadData: ImageUploadData | null) => {
    setImageData(imageUploadData);
  };

  const handleSubmit = async () => {
    if (checkProfanityWord(comment)) {
      toast.error('코멘트에 부적절한 단어가 포함되어 있습니다.');
      return;
    }
    await sendComments({
      content: comment,
      imageUrl: imageData?.imageUrl,
      imageName: imageData?.imageName,
    });
  };

  useEffect(() => {
    return () => {
      const typed = comment.trim().length > 0 || imageData != null;
      if (!isSuccess && typed) {
        const length = comment.length;
        const content_length_bucket = length <= 60 ? 's' : length <= 140 ? 'm' : 'l';
        const has_media = Boolean(imageData);
        track('abandon_composer', {
          composer: 'comment',
          has_media,
          content_length_bucket,
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
