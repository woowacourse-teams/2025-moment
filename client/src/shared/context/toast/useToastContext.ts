import { useContext } from 'react';
import { ToastContext, ToastContextType } from './ToastProvider';

export const useToastContext = (): ToastContextType => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToastContext must be used within a ToastProvider');
  }
  return context;
};
