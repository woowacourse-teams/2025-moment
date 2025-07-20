import { Button } from '@/shared/ui';
import { LucideIcon, Send } from 'lucide-react';

interface RequestButtonProps {
  Icon?: LucideIcon;
  title: string;
  onClick?: () => void;
}

export const RequestButton = ({ Icon, title, onClick }: RequestButtonProps) => {
  return <Button Icon={Icon} title={title} variant="tertiary" onClick={onClick} />;
};
