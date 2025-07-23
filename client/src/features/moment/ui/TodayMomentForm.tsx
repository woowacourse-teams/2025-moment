import { TodayMomentSuccessContent } from '@/features/moment/ui/TodayMomentSuccessContent';
import { TodayMomentWriteContent } from '@/features/moment/ui/TodayMomentWriteContent';
import { Card } from '@/shared/ui';
import { useSendMoments } from '../hook/useSendMoments';

export function TodayMomentForm() {
  const { handleContentChange, handleSendContent, content, isSuccess } = useSendMoments();

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
