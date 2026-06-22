import { useReadAllNotifications } from '@/features/notification/hooks/useReadAllNotifications';
import { useShowFullImage } from '@/shared/hooks/useShowFullImage';
import { CommentItem } from '../types/comments';

export const useMyCommentsCard = (myComment: CommentItem, groupId?: number | string) => {
  const { handleReadAllNotifications, isLoading: isReadingNotification } =
    useReadAllNotifications(groupId);
  const { fullImageSrc, handleImageClick, closeFullImage, ImageOverlayPortal } = useShowFullImage();

  const handleCommentOpen = () => {
    if (myComment.commentNotification.isRead || isReadingNotification) return;
    if (myComment.commentNotification.notificationIds.length > 0) {
      handleReadAllNotifications(myComment.commentNotification.notificationIds);
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
