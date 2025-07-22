import { Card } from '@/shared/ui';
import { TodayMomentSuccessContent } from '@/features/todayMoment/ui/TodayMomentSuccessContent';
import { TodayMomentWriteContent } from '@/features/todayMoment/ui/TodayMomentWriteContent';
import { useSubmitted } from '@/features/todayMoment/hook/useSubmitted';
import { useNavigateHandler } from '@/shared/hooks/useNavigateHandler';

export function TodayMomentForm() {
  const { isSubmitted, handleSubmit } = useSubmitted();
  const handlePagination = useNavigateHandler('/my-moments');

  return (
    <Card width="medium">
      {!isSubmitted ? (
        <TodayMomentWriteContent onSubmit={handleSubmit} />
      ) : (
        <TodayMomentSuccessContent onPagination={handlePagination} />
      )}
    </Card>
  );
}
