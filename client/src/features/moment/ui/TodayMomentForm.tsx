import { useSubmitted } from '@/features/moment/hook/useSubmitted';
import { TodayMomentSuccessContent } from '@/features/moment/ui/TodayMomentSuccessContent';
import { TodayMomentWriteContent } from '@/features/moment/ui/TodayMomentWriteContent';
import { useNavigateHandler } from '@/shared/hooks/useNavigateHandler';
import { Card } from '@/shared/ui';

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
