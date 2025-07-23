import { TodayMomentSuccessContent } from '@/features/moment/ui/TodayMomentSuccessContent';
import { TodayMomentWriteContent } from '@/features/moment/ui/TodayMomentWriteContent';
import { Card } from '@/shared/ui';
import { useSendMoments } from '../hook/useSendMoments';

export function TodayMomentForm() {
  const { handleContentChange, handleSendContent, content, isSuccess, handleReset } =
    useSendMoments();

  return (
    <Card width="medium">
      {!isSuccess ? (
        <TodayMomentWriteContent
          handleContentChange={handleContentChange}
          handleSendContent={handleSendContent}
          content={content}
        />
      ) : (
        <TodayMomentSuccessContent onBack={handleReset} />
      )}
    </Card>
  );
}
