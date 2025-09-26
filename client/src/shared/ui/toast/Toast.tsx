import { toasts, useToasts } from '@/shared/store/toast';
import { ToastData } from '@/shared/types/toast';
import { KeyRound, CheckCircle, Mail, X, XCircle } from 'lucide-react';
import React, { useState } from 'react';
import { useNavigate } from 'react-router';
import { createPortal } from 'react-dom';
import * as S from './Toast.styles';

interface ToastItemProps {
  toast: ToastData;
  onClose: (id: string) => void;
}

const ToastItem: React.FC<ToastItemProps> = ({ toast, onClose }) => {
  const [isExiting, setIsExiting] = useState(false);
  const navigate = useNavigate();

  const handleClose = (e?: React.MouseEvent) => {
    e?.stopPropagation();
    setIsExiting(true);
    setTimeout(() => {
      onClose(toast.id);
    }, 300);
  };

  const handleToastClick = () => {
    if (toast.variant === 'message' && toast.routeType) {
      const route =
        toast.routeType === 'moment' ? '/collection/my-moment' : '/collection/my-comment';
      navigate(route);
      handleClose();
    }
  };

  const getIcon = () => {
    switch (toast.variant) {
      case 'success':
        return <CheckCircle size={20} />;
      case 'error':
        return <XCircle size={20} />;
      case 'warning':
        return <KeyRound size={20} />;
      case 'message':
        return <Mail size={20} />;
      default:
        return null;
    }
  };

  return (
    <S.ToastItem
      variant={toast.variant}
      isExiting={isExiting}
      onClick={handleToastClick}
      $isClickable={toast.variant === 'message' && !!toast.routeType}
    >
      <S.ToastIconWrapper>{getIcon()}</S.ToastIconWrapper>
      <S.ToastMessage>{toast.message}</S.ToastMessage>
      <S.CloseButton onClick={e => handleClose(e)} aria-label="토스트 닫기">
        <X size={16} />
      </S.CloseButton>
    </S.ToastItem>
  );
};

export const Toast: React.FC = () => {
  const { toasts: activeToasts } = useToasts();

  if (activeToasts.length === 0) {
    return null;
  }

  return createPortal(
    <S.ToastContainer>
      {activeToasts.map(toast => (
        <ToastItem key={toast.id} toast={toast} onClose={toasts.hide} />
      ))}
    </S.ToastContainer>,
    document.body,
  );
};
