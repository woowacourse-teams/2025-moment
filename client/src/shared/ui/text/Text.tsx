import { textHeight } from './Text.styles';
import * as S from './Text.styles';

interface TextProps {
  height: textHeight;
  content: string;
}

export const Text = ({ height, content }: TextProps) => {
  return <S.Text $height={height}>{content}</S.Text>;
};
