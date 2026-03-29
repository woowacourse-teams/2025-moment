import React from 'react';
import * as S from './Button.styles';
import { ButtonVariant, ExternalVariant } from './Button.styles';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  externalVariant?: ExternalVariant;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      variant = 'primary',
      externalVariant,
      leftIcon,
      rightIcon,
      children,
      type = 'button',
      ...props
    },
    ref,
  ) => {
    return (
      <S.Button
        ref={ref}
        variant={variant}
        externalVariant={externalVariant}
        type={type}
        {...props}
      >
        {leftIcon}
        {children}
        {rightIcon}
      </S.Button>
    );
  },
);

Button.displayName = 'Button';
