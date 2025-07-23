import { Card } from '@/shared/ui';
import { useSubmitted } from '@/features/todayMoment/hook/useSubmitted';

import { useNavigateHandler } from '@/shared/hooks/useNavigateHandler';
import { TodayCommentWriteContent } from './TodayCommentWriteContent';
import { TodayCommentSuccessContent } from './TodayCommentSuccessContent';

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
