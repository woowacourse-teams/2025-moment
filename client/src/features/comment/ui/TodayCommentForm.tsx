import { useSubmitted } from '@/features/moment/hook/useSubmitted';
import { Card } from '@/shared/ui';
import { TodayCommentSuccessContent } from './TodayCommentSuccessContent';
import { TodayCommentWriteContent } from './TodayCommentWriteContent';

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
