import { Button } from '@/shared/ui';
import { LucideIcon } from 'lucide-react';

interface RequestButtonProps {
  Icon?: LucideIcon;
  title: string;
  onClick?: () => void;
  disabled?: boolean;
}

export const YellowSquareButton = ({ Icon, title, onClick, disabled }: RequestButtonProps) => {
  return (
    <Button Icon={Icon} title={title} variant="tertiary" onClick={onClick} disabled={disabled} />
  );
};
