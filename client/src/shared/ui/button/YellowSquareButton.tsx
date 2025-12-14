import { Button } from '@/shared/design-system/button';
import { LucideIcon } from 'lucide-react';

interface RequestButtonProps {
  Icon?: LucideIcon;
  title: string;
  onClick?: () => void;
  disabled?: boolean;
  type?: 'button' | 'submit' | 'reset';
}

export const YellowSquareButton = ({
  Icon,
  title,
  onClick,
  disabled,
  type,
}: RequestButtonProps) => {
  return (
    <Button
      Icon={Icon}
      title={title}
      variant="tertiary"
      onClick={onClick}
      disabled={disabled}
      type={type}
    />
  );
};
