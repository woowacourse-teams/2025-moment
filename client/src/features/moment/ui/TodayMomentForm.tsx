import { Card } from '@/shared/ui';
import { TodayMomentSuccessContent } from '@/features/moment/ui/TodayMomentSuccessContent';
import { TodayMomentWriteContent } from '@/features/moment/ui/TodayMomentWriteContent';
import { useSubmitted } from '@/features/moment/hook/useSubmitted';

export function TodayMomentForm() {
  const { isSubmitted, handleSubmit, handleBack } = useSubmitted();

  return (
    <Card width="medium">
      {!isSubmitted ? (
        <TodayMomentWriteContent onSubmit={handleSubmit} />
      ) : (
        <TodayMomentSuccessContent onBack={handleBack} />
      )}
    </Card>
  );
}
