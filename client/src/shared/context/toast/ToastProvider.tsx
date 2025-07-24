import { CreateToastParams, ToastData } from '@/shared/types/toast';
import { Toast } from '@/shared/ui';
import { createContext, ReactNode, useState } from 'react';
import { createPortal } from 'react-dom';
import { ToastContainer } from '../../ui/toast/Toast.styles';

export interface ToastContextType {
  addToast: (toast: CreateToastParams) => void;
  removeToast: (id: string) => void;
  toasts: ToastData[];
}

export const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const ToastProvider = ({ children }: { children: ReactNode }) => {
  const [toasts, setToasts] = useState<ToastData[]>([]);

  const addToast = (toast: CreateToastParams) => {
    const id = Math.random().toString(36).slice(2, 11);
    const newToast: ToastData = { ...toast, id };

    setToasts(prevToasts => [...prevToasts, newToast]);
  };

  const removeToast = (id: string) => {
    setToasts(prevToasts => prevToasts.filter(toast => toast.id !== id));
  };

  const contextValue: ToastContextType = {
    addToast,
    removeToast,
    toasts,
  };

  return (
    <ToastContext.Provider value={contextValue}>
      {children}
      {toasts.length > 0 &&
        createPortal(
          <ToastContainer>
            {toasts.map(toast => (
              <Toast
                key={toast.id}
                id={toast.id}
                message={toast.message}
                variant={toast.variant}
                duration={toast.duration}
                onClose={removeToast}
              />
            ))}
          </ToastContainer>,
          document.body,
        )}
    </ToastContext.Provider>
  );
};
