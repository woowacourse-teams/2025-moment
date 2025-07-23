import { useSubmitted } from '@/features/moment/hook/useSubmitted';
import { TodayMomentSuccessContent } from '@/features/moment/ui/TodayMomentSuccessContent';
import { TodayMomentWriteContent } from '@/features/moment/ui/TodayMomentWriteContent';
import { Card } from '@/shared/ui';

export function TodayMomentForm() {
  const { isSubmitted, handleSubmit } = useSubmitted();

  return (
    <Card width="medium">
      {!isSubmitted ? (
        <TodayMomentWriteContent onSubmit={handleSubmit} />
      ) : (
        <TodayMomentSuccessContent />
      )}
    </Card>
  );
}
