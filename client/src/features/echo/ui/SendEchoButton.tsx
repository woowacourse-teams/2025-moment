import { CustomTheme, theme } from '@/app/styles/theme';
import { useEchoMutation } from '../hooks/useEchoMutation';
import { Button } from '@/shared/ui';
import { EchoTypeKey } from '../type/echos';

interface SendEchoButtonProps {
  commentId: number;
  selectedEchos: Set<EchoTypeKey>;
  hasSelection: boolean;
  isDisabled?: boolean;
}

export const SendEchoButton = ({
  commentId,
  selectedEchos,
  hasSelection,
  isDisabled,
}: SendEchoButtonProps) => {
  const { mutateAsync: sendEchos } = useEchoMutation();

  const handleSendEchos = () => {
    sendEchos({ echoTypes: Array.from(selectedEchos), commentId: commentId });
  };

  return (
    <Button
      title="전송"
      variant="primary"
      disabled={!hasSelection || isDisabled}
      externalVariant={() => buttonVariant(theme, hasSelection)}
      onClick={handleSendEchos}
    />
  );
};

const buttonVariant = (theme: CustomTheme, isSelected: boolean) => `
  width: 100%;
  color: ${theme.colors['white']};
  background-color: ${isSelected ? theme.colors['gray-600'] : theme.colors['gray-600_20']};
`;
