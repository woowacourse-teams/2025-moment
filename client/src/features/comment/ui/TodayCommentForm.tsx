import { Card } from '@/shared/ui';
import { TodayCommentWriteContent } from './TodayCommentWriteContent';
import { useSend } from '../hooks/useSend';

export function TodayCommentForm() {
  const { handleSubmit } = useSend();
  return (
    <Card width="medium">
      <TodayCommentWriteContent onSubmit={handleSubmit} />
    </Card>
  );
}
