import { useModal } from '@/shared/design-system/modal';
import { useSendComplaint } from '@/features/complaint/hooks/useSendComplaint';
import { useShowFullImage } from '@/shared/hooks/useShowFullImage';
import { GetCommentableMoments } from '../types/comments';

interface UseTodayCommentFormProps {
  momentData?: GetCommentableMoments;
}

export const useTodayCommentForm = ({ momentData }: UseTodayCommentFormProps) => {
  const { fullImageSrc, handleImageClick, closeFullImage, ImageOverlayPortal } = useShowFullImage();

  const {
    handleOpen: handleComplaintOpen,
    handleClose: handleComplaintClose,
    isOpen: isComplaintOpen,
  } = useModal();

  const { handleComplaintSubmit } = useSendComplaint(handleComplaintClose);

  return {
    fullImageSrc,
    handleImageClick,
    closeFullImage,
    ImageOverlayPortal,
    handleComplaintOpen,
    handleComplaintClose,
    handleComplaintSubmit,
    isComplaintOpen,
  };
};
