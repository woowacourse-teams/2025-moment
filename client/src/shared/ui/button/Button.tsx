import { ButtonVariant } from './Button.styles';
import * as S from './Button.styles';

interface ButtonProps extends React.HTMLAttributes<HTMLButtonElement> {
  variant: ButtonVariant;
  title: string;
  onClick?: () => void;
}

export const Button = ({ variant, title, onClick, ...props }: ButtonProps) => {
  return (
    <S.Button variant={variant} onClick={onClick} {...props}>
      {title}
    </S.Button>
  );
};
