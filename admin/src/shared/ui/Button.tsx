import type { ButtonHTMLAttributes, ReactNode } from 'react';
import { StyledButton, type ButtonVariant, type ButtonSize } from './Button.styles';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  children: ReactNode;
  isLoading?: boolean;
}

export function Button({
  variant = 'primary',
  size = 'md',
  children,
  isLoading,
  disabled,
  ...props
}: ButtonProps) {
  return (
    <StyledButton variant={variant} size={size} disabled={disabled || isLoading} {...props}>
      {isLoading ? 'Loading...' : children}
    </StyledButton>
  );
}
