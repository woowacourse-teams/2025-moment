export type ToastVariant = 'success' | 'error';

export interface ToastData {
  message: string;
  variant: ToastVariant;
  duration?: number;
}

export interface ToastProps {
  message: string;
  variant: ToastVariant;
  duration?: number;
  onClose: () => void;
}

export interface UseToastReturn {
  showSuccess: (message: string, duration?: number) => void;
  showError: (message: string, duration?: number) => void;
  removeToast: () => void;
  toast: ToastData | null;
}
