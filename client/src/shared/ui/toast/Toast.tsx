import { ToastProps } from '@/shared/types/toast';
import { CheckCircle, X, XCircle } from 'lucide-react';
import React, { useEffect, useState } from 'react';
import * as S from './Toast.styles';

export const Toast: React.FC<ToastProps> = ({ id, message, variant, duration = 4000, onClose }) => {
  const [isExiting, setIsExiting] = useState(false);

  useEffect(() => {
    if (duration > 0) {
      const timer = setTimeout(() => {
        handleClose();
      }, duration);

      return () => clearTimeout(timer);
    }
  }, [duration]);

  const handleClose = () => {
    setIsExiting(true);
    setTimeout(() => {
      onClose(id);
    }, 300);
  };

  const getIcon = () => {
    switch (variant) {
      case 'success':
        return <CheckCircle size={20} />;
      case 'error':
        return <XCircle size={20} />;
      default:
        return null;
    }
  };

  return (
    <S.ToastItem variant={variant} isExiting={isExiting}>
      <S.ToastIconWrapper>{getIcon()}</S.ToastIconWrapper>
      <S.ToastMessage>{message}</S.ToastMessage>
      <S.CloseButton onClick={handleClose} aria-label="토스트 닫기">
        <X size={16} />
      </S.CloseButton>
    </S.ToastItem>
  );
};
