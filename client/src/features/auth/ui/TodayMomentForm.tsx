import { Card } from '@/shared/ui';
import { Star } from 'lucide-react';
import { useCallback, useState } from 'react';
import { TodayMomentSuccessContent } from '@/widgets/todayMoment/TodayMomentSuccessContent';
import { TodayMomentWriteContent } from '@/widgets/todayMoment/TodayMomentWriteContent';

export function TodayMomentForm() {
  const [isSubmitted, setIsSubmitted] = useState(false);

  const handleSubmit = useCallback(() => {
    setIsSubmitted(true);
  }, []);

  const handleBack = useCallback(() => {
    setIsSubmitted(false);
  }, []);

  return (
    <Card width="medium">
      <Card.TitleContainer
        Icon={Star}
        title="모멘트 공유하기"
        subtitle="뿌듯한 순간, 위로받고 싶은 순간, 기쁜 순간 모든 모멘트를 자유롭게 적어보세요"
      ></Card.TitleContainer>
      {!isSubmitted ? (
        <TodayMomentWriteContent onSubmit={handleSubmit} />
      ) : (
        <TodayMomentSuccessContent onBack={handleBack} />
      )}
    </Card>
  );
}
