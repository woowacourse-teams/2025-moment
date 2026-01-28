import styled from "@emotion/styled";
import { css } from "@emotion/react";

export type ButtonVariant = "primary" | "secondary" | "danger" | "ghost";
export type ButtonSize = "sm" | "md" | "lg";

export const variantStyles = {
  primary: css`
    background-color: #3b82f6;
    color: white;
    &:hover:not(:disabled) {
      background-color: #2563eb;
    }
  `,
  secondary: css`
    background-color: #4b5563;
    color: white;
    &:hover:not(:disabled) {
      background-color: #374151;
    }
  `,
  danger: css`
    background-color: #ef4444;
    color: white;
    &:hover:not(:disabled) {
      background-color: #dc2626;
    }
  `,
  ghost: css`
    background-color: transparent;
    color: #9ca3af;
    &:hover:not(:disabled) {
      background-color: #374151;
    }
  `,
};

export const sizeStyles = {
  sm: css`
    padding: 0.375rem 0.75rem;
    font-size: 0.875rem;
  `,
  md: css`
    padding: 0.5rem 1rem;
    font-size: 1rem;
  `,
  lg: css`
    padding: 0.75rem 1.5rem;
    font-size: 1.125rem;
  `,
};

export const StyledButton = styled.button<{
  variant: ButtonVariant;
  size: ButtonSize;
}>`
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  border: none;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  ${({ variant }) => variantStyles[variant]}
  ${({ size }) => sizeStyles[size]}
`;
