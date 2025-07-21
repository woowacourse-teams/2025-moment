import { Card } from '@/shared/ui';
import { TodayMomentSuccessContent } from '@/widgets/todayMoment/TodayMomentSuccessContent';
import { TodayMomentWriteContent } from '@/widgets/todayMoment/TodayMomentWriteContent';
import { useSubmitted } from '@/features/TodayMoment/hook/useSubmitted';

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
