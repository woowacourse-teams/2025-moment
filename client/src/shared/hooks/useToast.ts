import { useToastContext } from '@/shared/context/toast/useToastContext';
import { UseToastReturn } from '@/shared/types/toast';

export const useToast = (): UseToastReturn => {
  const { addToast, removeToast, toasts } = useToastContext();

  const showSuccess = (message: string, duration?: number) => {
    addToast({
      message,
      variant: 'success',
      duration,
    });
  };

  const showError = (message: string, duration?: number) => {
    addToast({
      message,
      variant: 'error',
      duration,
    });
  };

  return {
    showSuccess,
    showError,
    removeToast,
    toasts,
  };
};
