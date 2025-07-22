import { Card } from '@/shared/ui';
import { useSubmitted } from '@/features/todayMoment/hook/useSubmitted';
import { TodayCommentWriteContent } from './TodayCommentWriteContent';
import { TodayCommentSuccessContent } from './TodayCommentSuccessContent';

export function TodayCommentForm() {
  const { isSubmitted, handleSubmit, handleBack } = useSubmitted();

  return (
    <Card width="medium">
      {!isSubmitted ? (
        <TodayCommentWriteContent onSubmit={handleSubmit} />
      ) : (
        <TodayCommentSuccessContent onBack={handleBack} />
      )}
    </Card>
  );
}
