import { useSubmitted } from '@/features/moment/hook/useSubmitted';
import { Card } from '@/shared/ui';
import { TodayCommentSuccessContent } from './TodayCommentSuccessContent';
import { TodayCommentWriteContent } from './TodayCommentWriteContent';
import { useNavigateHandler } from '@/shared/hooks/useNavigateHandler';

export function TodayCommentForm() {
  const { isSubmitted, handleSubmit } = useSubmitted();

  const handlePagination = useNavigateHandler('/post-comments');

  return (
    <Card width="medium">
      {!isSubmitted ? (
        <TodayCommentWriteContent onSubmit={handleSubmit} />
      ) : (
        <TodayCommentSuccessContent onPagination={handlePagination} />
      )}
    </Card>
  );
}
