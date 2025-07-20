import { Card } from '@/shared/ui';
import { Star } from 'lucide-react';
import { useCallback, useState } from 'react';
import { TodaySuccessContent } from '@/widgets/today/TodaySuccessContent';
import { TodayWriteContent } from '@/widgets/today/TodayWriteContent';

export function TodayForm() {
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
        <TodayWriteContent onSubmit={handleSubmit} />
      ) : (
        <TodaySuccessContent onBack={handleBack} />
      )}
    </Card>
  );
}
