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
  onClose: () => void;
}

export type CreateToastParams = Omit<ToastData, 'id'>;

export interface UseToastReturn {
  showSuccess: (message: string, duration?: number) => void;
  showError: (message: string, duration?: number) => void;
  removeToast: () => void;
  toast: ToastData | null;
}
