import { ToastData } from '@/shared/types/toast';
import { Toast } from '@/shared/ui';
import { createContext, ReactNode, useState } from 'react';
import { createPortal } from 'react-dom';
import { ToastContainer } from '../../ui/toast/Toast.styles';

export interface ToastContextType {
  addToast: (toast: ToastData) => void;
  removeToast: () => void;
  toast: ToastData | null;
}

export const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const ToastProvider = ({ children }: { children: ReactNode }) => {
  const [toast, setToast] = useState<ToastData | null>(null);

  const addToast = (toastParams: ToastData) => {
    setToast(toastParams);
  };

  const removeToast = () => {
    setToast(null);
  };

  const contextValue: ToastContextType = {
    addToast,
    removeToast,
    toast,
  };

  return (
    <ToastContext.Provider value={contextValue}>
      {children}
      {toast &&
        createPortal(
          <ToastContainer>
            <Toast
              key="toast"
              message={toast.message}
              variant={toast.variant}
              duration={toast.duration}
              onClose={removeToast}
            />
          </ToastContainer>,
          document.body,
        )}
    </ToastContext.Provider>
  );
};
