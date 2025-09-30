import { useAllNotificationsMutation } from './useAllNotificationsMutation';

export const useReadAllNotifications = () => {
  const {
    mutate: mutateAllNotification,
    isPending,
    error,
    isError,
  } = useAllNotificationsMutation();
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
