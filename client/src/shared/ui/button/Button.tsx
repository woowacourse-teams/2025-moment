import { LucideIcon } from 'lucide-react';
import * as S from './Button.styles';
import { ButtonVariant, ExternalVariant } from './Button.styles';

interface ButtonProps extends React.HTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  externalVariant?: ExternalVariant;
  title: string;
  onClick?: () => void;
  disabled?: boolean;
  Icon?: LucideIcon;
  type?: 'button' | 'submit' | 'reset';
}

export const Button = ({
  variant = 'primary',
  externalVariant,
  title,
  onClick,
  disabled,
  Icon,
  type = 'button',
  ...props
}: ButtonProps) => {
  return (
    <S.Button
      variant={variant}
      externalVariant={externalVariant}
      onClick={onClick}
      disabled={disabled}
      type={type}
      {...props}
    >
      {Icon && <Icon size={16} />}
      {title}
    </S.Button>
  );
};
