import { useReadNotificationsMutation } from '../api/useReadNotificationsMutation';

export const useReadNotifications = () => {
  const { mutate: mutateNotification, isPending, error, isError } = useReadNotificationsMutation();
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
