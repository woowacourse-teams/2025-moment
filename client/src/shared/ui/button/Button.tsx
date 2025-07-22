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
}

export const Button = ({
  variant = 'primary',
  externalVariant,
  title,
  onClick,
  disabled,
  Icon,
  ...props
}: ButtonProps) => {
  return (
    <S.Button
      variant={variant}
      externalVariant={externalVariant}
      onClick={onClick}
      disabled={disabled}
      {...props}
    >
      {Icon && <Icon size={16} />}
      {title}
    </S.Button>
  );
};
