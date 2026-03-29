import React from 'react';
import * as S from './Input.styles';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  placeholder: string;
  type?: 'text' | 'password' | 'email';
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>((props, ref) => {
  return <S.Input ref={ref} {...props} />;
});

Input.displayName = 'Input';
