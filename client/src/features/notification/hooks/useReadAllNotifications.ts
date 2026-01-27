import { useReadAllNotificationsMutation } from '../api/useReadAllNotificationsMutation';

export const useReadAllNotifications = (groupId?: number | string) => {
  const {
    mutate: mutateAllNotification,
    isPending,
    error,
    isError,
  } = useReadAllNotificationsMutation(groupId);
  const handleReadAllNotifications = (ids: number[]) => {
    if (isPending) return;
    mutateAllNotification(ids);
  };

  return {
    handleReadAllNotifications,
    isLoading: isPending,
    error,
    isError,
  };
};
