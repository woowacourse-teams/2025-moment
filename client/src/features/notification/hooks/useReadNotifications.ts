import { useReadNotificationsMutation } from '../api/useReadNotificationsMutation';

export const useReadNotifications = (groupId?: number | string) => {
  const {
    mutate: mutateNotification,
    isPending,
    error,
    isError,
  } = useReadNotificationsMutation(groupId);
  const handleReadNotifications = (id: number) => {
    if (isPending) return;
    mutateNotification(id);
  };

  return {
    handleReadNotifications,
    isLoading: isPending,
    error,
    isError,
  };
};
