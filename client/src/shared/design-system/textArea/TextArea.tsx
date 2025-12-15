import { textAreaHeight } from './TextArea.styles';
import * as S from './TextArea.styles';

export interface TextAreaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  placeholder: string;
  height: textAreaHeight;
}

export const TextArea = ({ placeholder, height, ...props }: TextAreaProps) => {
  return <S.TextArea placeholder={placeholder} $height={height} {...props} />;
};
