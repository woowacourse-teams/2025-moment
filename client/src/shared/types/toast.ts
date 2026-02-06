import { ReactNode } from 'react';

export type ToastVariant = 'success' | 'error' | 'warning' | 'message';
export type ToastRouteType = 'moment' | 'comment';

export interface ToastData {
  id: string;
  message: ReactNode;
  variant: ToastVariant;
  duration?: number;
  routeType?: ToastRouteType;
  groupId?: number;
  link?: string;
}

export interface UseToastReturn {
  showSuccess: (message: string, duration?: number) => void;
  showError: (message: string, duration?: number) => void;
  showWarning: (message: string, duration?: number) => void;
  showMessage: (message: string, routeType?: ToastRouteType, duration?: number) => void;
  removeToast: () => void;
}

export interface ToastsState {
  toasts: ToastData[];
}
