import { CustomTheme } from '@/app/styles/theme';
import { Button } from '@/shared/ui';

export const EchoButton = ({
  title,
  onClick: handleClick,
}: {
  title: string;
  onClick: () => void;
}) => {
  return <Button externalVariant={buttonVariant} title={title} onClick={handleClick} />;
};

const buttonVariant = (theme: CustomTheme) => `
  border: 1px solid ${theme.colors['yellow-500']};
  color: ${theme.colors['yellow-500']};
  border-radius: 25px;
  padding: 4px 20px;
  font-size: 14px;
  font-weight: bold;
`;
