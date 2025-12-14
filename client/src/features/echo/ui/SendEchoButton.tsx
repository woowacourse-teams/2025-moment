import { CustomTheme, theme } from '@/app/styles/theme';
import { Button } from '@/shared/design-system/button';

interface SendEchoButtonProps {
  isDisabled: boolean;
  onClick: () => void;
}

export const SendEchoButton = ({ isDisabled, onClick: handleSendEchos }: SendEchoButtonProps) => {
  return (
    <Button
      title="전송"
      variant="primary"
      disabled={isDisabled}
      externalVariant={() => buttonVariant(theme, isDisabled)}
      onClick={handleSendEchos}
    />
  );
};

const buttonVariant = (theme: CustomTheme, isDisabled: boolean) => `
  width: 90%;
  color: ${theme.colors['gray-200']};
  background-color: ${isDisabled ? theme.colors['gray-600_20'] : theme.colors['yellow-300_30']};
  margin: 10px 0px;
  `;
