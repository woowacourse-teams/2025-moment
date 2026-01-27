import { useReadNotifications } from '@/features/notification/hooks/useReadNotifications';
import { useShowFullImage } from '@/shared/hooks/useShowFullImage';
import { CommentItem } from '../types/comments';

export const useMyCommentsCard = (myComment: CommentItem, groupId?: number | string) => {
  const { handleReadNotifications, isLoading: isReadingNotification } =
    useReadNotifications(groupId);
  const { fullImageSrc, handleImageClick, closeFullImage, ImageOverlayPortal } = useShowFullImage();

  const handleCommentOpen = () => {
    if (myComment.commentNotification.isRead || isReadingNotification) return;
    if (myComment.commentNotification.notificationIds) {
      handleReadNotifications(myComment.commentNotification.notificationIds[0]);
    }
  };

  return {
    handleCommentOpen,
    fullImageSrc,
    handleImageClick,
    closeFullImage,
    ImageOverlayPortal,
  };
};
