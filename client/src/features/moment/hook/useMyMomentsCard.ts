import { useModal } from '@/shared/design-system/modal';
import { useShowFullImage } from '@/shared/hooks/useShowFullImage';
import { useMemo, useState } from 'react';
import { useReadAllNotifications } from '../../notification/hooks/useReadAllNotifications';
import { useSendComplaint } from '@/features/complaint/hooks/useSendComplaint';
import { useCommentNavigation } from './useCommentNavigation';
import { MyMomentsItem } from '../types/moments';

export const useMyMomentsCard = (myMoment: MyMomentsItem, groupId?: number | string) => {
  const [complainedCommentId, setComplainedCommentId] = useState<Set<number>>(new Set());

  const { handleReadAllNotifications, isLoading: isReadingNotification } =
    useReadAllNotifications(groupId);
  const { handleOpen, handleClose, isOpen } = useModal();
  const {
    handleOpen: handleComplaintOpen,
    handleClose: handleComplaintClose,
    isOpen: isComplaintOpen,
  } = useModal();

  const filteredComments = useMemo(() => {
    return myMoment.comments?.filter(comment => !complainedCommentId.has(comment.id)) || [];
  }, [myMoment.comments, complainedCommentId]);

  const { fullImageSrc, handleImageClick, closeFullImage, ImageOverlayPortal } = useShowFullImage();
  const sortedComments = useMemo(() => {
    return filteredComments?.slice().reverse() || [];
  }, [filteredComments]);

  const navigation = useCommentNavigation(sortedComments?.length || 0);
  const currentComment = sortedComments?.[navigation.currentIndex];

  const { handleComplaintSubmit } = useSendComplaint(() => {
    handleComplaintClose();

    if (currentComment) {
      setComplainedCommentId(prev => new Set([...prev, currentComment.id]));
    }

    if (filteredComments.length <= 1) {
      handleModalClose();
    } else if (navigation.currentIndex >= filteredComments.length - 1) {
      navigation.goToPrevious();
    }
  });

  const handleModalClose = () => {
    navigation.reset();
    handleClose();
  };

  const handleMomentClick = () => {
    handleOpen();
    navigation.reset();
    if (myMoment.momentNotification.isRead || isReadingNotification) return;

    handleReadAllNotifications(myMoment.momentNotification.notificationIds);
  };

  return {
    isOpen,
    isComplaintOpen,
    currentComment,
    fullImageSrc,
    sortedComments,
    navigation,
    handleModalClose,
    handleMomentClick,
    handleComplaintOpen,
    handleComplaintClose,
    handleComplaintSubmit,
    handleImageClick,
    closeFullImage,
    ImageOverlayPortal,
  };
};
