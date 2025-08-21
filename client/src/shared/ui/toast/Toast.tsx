import { ToastProps } from '@/shared/types/toast';
import { CheckCircle, Mail, X, XCircle } from 'lucide-react';
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import * as S from './Toast.styles';

export const Toast: React.FC<ToastProps> = ({
  message,
  variant,
  duration = 3000,
  routeType,
  onClose,
}) => {
  const [isExiting, setIsExiting] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (duration > 0) {
      const timer = setTimeout(() => {
        handleClose();
      }, duration);

      return () => clearTimeout(timer);
    }
  }, [duration]);

  const handleClose = (e?: React.MouseEvent) => {
    e?.stopPropagation();
    setIsExiting(true);
    setTimeout(() => {
      onClose();
    }, 300);
  };

  const handleToastClick = () => {
    if (variant === 'message' && routeType) {
      const route = routeType === 'moment' ? '/collection/my-moment' : '/collection/my-comment';
      navigate(route);
      handleClose();
    }
  };

  const getIcon = () => {
    switch (variant) {
      case 'success':
        return <CheckCircle size={20} />;
      case 'error':
        return <XCircle size={20} />;
      case 'message':
        return <Mail size={20} />;
      default:
        return null;
    }
  };

  return (
    <S.ToastItem
      variant={variant}
      isExiting={isExiting}
      onClick={handleToastClick}
      $isClickable={variant === 'message' && !!routeType}
    >
      <S.ToastIconWrapper>{getIcon()}</S.ToastIconWrapper>
      <S.ToastMessage>{message}</S.ToastMessage>
      <S.CloseButton onClick={e => handleClose(e)} aria-label="토스트 닫기">
        <X size={16} />
      </S.CloseButton>
    </S.ToastItem>
  );
};
