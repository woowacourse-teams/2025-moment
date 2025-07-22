import { LucideIcon } from 'lucide-react';
import * as S from './Button.styles';
import { ButtonVariant } from './Button.styles';

interface ButtonProps extends React.HTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  title: string;
  onClick?: () => void;
  disabled?: boolean;
  Icon?: LucideIcon;
}

export const Button = ({
  variant = 'primary',
  title,
  onClick,
  disabled,
  Icon,
  ...props
}: ButtonProps) => {
  return (
    <S.Button variant={variant} onClick={onClick} disabled={disabled} {...props}>
      {Icon && <Icon size={16} />}
      {title}
    </S.Button>
  );
};
