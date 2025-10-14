import { useToastContext } from '@/shared/context/toast/useToastContext';
import { ToastRouteType, UseToastReturn } from '@/shared/types/toast';

export const useToast = (): UseToastReturn => {
  const { addToast, removeToast, toast } = useToastContext();

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

  const showWarning = (message: string, duration?: number) => {
    addToast({
      message,
      variant: 'warning',
      duration,
    });
  };

  const showMessage = (message: string, routeType?: ToastRouteType, duration?: number) => {
    addToast({
      message,
      variant: 'message',
      duration,
      routeType,
    });
  };

  return {
    showSuccess,
    showError,
    showWarning,
    showMessage,
    removeToast,
    toast,
  };
};
