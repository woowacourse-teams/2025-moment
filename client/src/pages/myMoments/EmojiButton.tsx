import { CustomTheme } from '@/app/styles/theme';
import { Button } from '@/shared/ui';

export const EmojiButton = () => {
  return <Button externalButtonStyles={externalButtonStyles} title="+ 스티커 보내기" />;
};

const externalButtonStyles = (theme: CustomTheme) => `
  border: 1px solid ${theme.colors['yellow-500']};
  color: ${theme.colors['yellow-500']};
  height: 20px;
  border-radius: 25px;
  padding: 0 16px;
  font-size: 12px;
  font-weight: bold;
`;
