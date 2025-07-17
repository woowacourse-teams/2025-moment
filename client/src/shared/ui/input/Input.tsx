import * as S from './Input.styles';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    placeholder: string;
    type: 'text' | 'password' | 'email';
}

export const Input = ({ placeholder, type, ...props }: InputProps) => {
  return <S.Input placeholder={placeholder} type={type} {...props}/>;
};