import { TodayMomentSuccessContent } from '@/features/moment/ui/TodayMomentSuccessContent';
import { TodayMomentWriteContent } from '@/features/moment/ui/TodayMomentWriteContent';
import { Card } from '@/shared/ui';

export function TodayMomentForm() {
  const { isSubmitted, handleSubmit } = useSubmitted();

  return (
    <Card width="medium">
      {!isSuccess ? (
        <TodayMomentWriteContent
          handleContentChange={handleContentChange}
          handleSendContent={handleSendContent}
          content={content}
        />
      ) : (
        <TodayMomentSuccessContent />
      )}
    </Card>
  );
}
