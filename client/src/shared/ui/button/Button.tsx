import { LucideIcon } from 'lucide-react';
import * as S from './Button.styles';
import { ButtonVariant, ExternalButtonStyles } from './Button.styles';

interface ButtonProps extends React.HTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  externalButtonStyles?: ExternalButtonStyles;
  title: string;
  onClick?: () => void;
  disabled?: boolean;
  Icon?: LucideIcon;
}

export const Button = ({
  variant = 'primary',
  externalButtonStyles,
  title,
  onClick,
  disabled,
  Icon,
  ...props
}: ButtonProps) => {
  return (
    <S.Button
      variant={variant}
      externalButtonStyles={externalButtonStyles}
      onClick={onClick}
      disabled={disabled}
      {...props}
    >
      {Icon && <Icon size={16} />}
      {title}
    </S.Button>
  );
};
