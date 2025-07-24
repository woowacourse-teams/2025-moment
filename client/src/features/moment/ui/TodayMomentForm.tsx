import { useSubmitted } from '@/features/moment/hook/useSubmitted';

import { TodayMomentWriteContent } from '@/features/moment/ui/TodayMomentWriteContent';
import { Card } from '@/shared/ui';
import { TodayMomentSuccessContent } from './TodayMomentSuccessContent';

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
