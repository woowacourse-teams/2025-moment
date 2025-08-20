export type ToastVariant = 'success' | 'error' | 'message';
export type ToastRouteType = 'moment' | 'comment';

export interface ToastData {
  message: string;
  variant: ToastVariant;
  duration?: number;
  routeType?: ToastRouteType;
}

export interface ToastProps {
  message: string;
  variant: ToastVariant;
  duration?: number;
  routeType?: ToastRouteType;
  onClose: () => void;
}

export interface UseToastReturn {
  showSuccess: (message: string, duration?: number) => void;
  showError: (message: string, duration?: number) => void;
  showMessage: (message: string, routeType?: ToastRouteType, duration?: number) => void;
  removeToast: () => void;
  toast: ToastData | null;
}
