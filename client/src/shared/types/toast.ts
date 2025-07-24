export type ToastVariant = 'success' | 'error';

export interface ToastData {
  id: string;
  message: string;
  variant: ToastVariant;
  duration?: number;
}

export interface ToastProps {
  id: string;
  message: string;
  variant: ToastVariant;
  duration?: number;
  onClose: (id: string) => void;
}

export type CreateToastParams = Omit<ToastData, 'id'>;

export interface UseToastReturn {
  showSuccess: (message: string, duration?: number) => void;
  showError: (message: string, duration?: number) => void;
  removeToast: (id: string) => void;
  toasts: ToastData[];
}
