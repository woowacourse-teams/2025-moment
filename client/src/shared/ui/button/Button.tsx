import * as S from './Button.styles';
import { ButtonVariant } from './Button.styles';

interface ButtonProps extends React.HTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  title: string;
  onClick?: () => void;
  disabled?: boolean;
}

export const Button = ({
  variant = 'primary',
  title,
  onClick,
  disabled,
  ...props
}: ButtonProps) => {
  return (
    <S.Button variant={variant} onClick={onClick} disabled={disabled} {...props}>
      {title}
    </S.Button>
  );
};
