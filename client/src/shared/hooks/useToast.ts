import { toasts } from '@/shared/store/toast';
import { ToastRouteType, UseToastReturn } from '@/shared/types/toast';

export const useToast = (): UseToastReturn => {
  return {
    showSuccess: (message: string, duration?: number) => toasts.success(message, duration),
    showError: (message: string, duration?: number) => toasts.error(message, duration),
    showWarning: (message: string, duration?: number) => toasts.warning(message, duration),
    showMessage: (message: string, routeType?: ToastRouteType, duration?: number) =>
      toasts.message(message, routeType, duration),
    removeToast: () => toasts.clear(),
  };
};
