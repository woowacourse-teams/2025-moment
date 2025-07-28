import { CustomTheme } from '@/app/styles/theme';
import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';

export type ToastVariant = 'success' | 'error';

const slideIn = keyframes`
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
`;

const slideOut = keyframes`
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(100%);
    opacity: 0;
  }
`;

const toastVariants = {
  success: (theme: CustomTheme) => `
    background-color: ${theme.colors['emerald-50']};
    border-left: 4px solid ${theme.colors['emerald-500']};
    color: ${theme.colors['emerald-600']};
  `,
  error: (theme: CustomTheme) => `
    background-color: color-mix(in srgb, ${theme.colors['red-500']} 10%, transparent);
    border-left: 4px solid ${theme.colors['red-500']};
    color: ${theme.colors['red-500']};
  `,
};

export const ToastContainer = styled.div`
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 400px;
  width: 100%;

  @media (max-width: 768px) {
    right: 10px;
    left: 10px;
    max-width: none;
  }
`;

export const ToastItem = styled.div<{
  variant: ToastVariant;
  isExiting?: boolean;
}>`
  ${({ theme, variant }) => toastVariants[variant](theme)};
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  font-weight: 500;
  position: relative;
  animation: ${({ isExiting }) => (isExiting ? slideOut : slideIn)} 0.3s ease-out forwards;
  backdrop-filter: blur(8px);
  min-height: 60px;

  @media (max-width: 768px) {
    padding: 14px 16px;
    font-size: 13px;
    min-height: 56px;
  }
`;

export const ToastIconWrapper = styled.div`
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
`;

export const ToastMessage = styled.div`
  flex: 1;
  line-height: 1.4;
`;

export const CloseButton = styled.button`
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.7;
  transition: opacity 0.2s ease;
  flex-shrink: 0;

  &:hover {
    opacity: 1;
  }

  &:focus {
    outline: 2px solid currentColor;
    outline-offset: 2px;
  }
`;
