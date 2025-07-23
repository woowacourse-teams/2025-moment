import { useSubmitted } from '@/features/moment/hook/useSubmitted';
import { TodayMomentSuccessContent } from '@/features/moment/ui/TodayMomentSuccessContent';
import { TodayMomentWriteContent } from '@/features/moment/ui/TodayMomentWriteContent';
import { Card } from '@/shared/ui';
import { useNavigate } from 'react-router';

export function TodayMomentForm() {
  const { isSubmitted, handleSubmit } = useSubmitted();
  const navigate = useNavigate();

  const handlePagination = () => navigate('/my-moments');

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
