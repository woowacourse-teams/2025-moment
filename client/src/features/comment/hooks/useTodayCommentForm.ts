import { useModal } from '@/shared/design-system/modal';
import { useSendComplaint } from '@/features/complaint/hooks/useSendComplaint';
import { useShowFullImage } from '@/shared/hooks/useShowFullImage';

interface UseTodayCommentFormProps {}

export const useTodayCommentForm = ({}: UseTodayCommentFormProps) => {
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
