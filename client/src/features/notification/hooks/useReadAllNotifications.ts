import { useReadAllNotificationsMutation } from '../api/useReadAllNotificationsMutation';

export const useReadAllNotifications = () => {
  const {
    mutate: mutateAllNotification,
    isPending,
    error,
    isError,
  } = useReadAllNotificationsMutation();
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
