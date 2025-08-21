import { useNotificationsMutation } from './useNotificationsMutation';

export const useReadNotifications = () => {
  const { mutate: mutateNotification, isPending, error, isError } = useNotificationsMutation();
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
