import React from 'react';
import { textAreaHeight } from './TextArea.styles';
import * as S from './TextArea.styles';

export interface TextAreaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  placeholder: string;
  height: textAreaHeight;
}

export const TextArea = React.forwardRef<HTMLTextAreaElement, TextAreaProps>(
  ({ height, ...props }, ref) => {
    return <S.TextArea ref={ref} $height={height} {...props} />;
  },
);

TextArea.displayName = 'TextArea';
