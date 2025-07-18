import { Button } from '@/shared/ui';
import { LucideIcon, Send } from 'lucide-react';

interface RequestButtonProps {
  Icon?: LucideIcon;
}

export const RequestButton = ({ Icon }: RequestButtonProps) => {
  return <Button Icon={Send} title="모멘트 공유하기" variant="tertiary" />;
};
