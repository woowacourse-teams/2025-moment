
import { useSendMoments } from '@/features/moment/hook/useSendMoments';
import { TodayMomentWriteContent } from '@/features/moment/ui/TodayMomentWriteContent';
import { Card } from '@/shared/ui';
import { TodayMomentSuccessContent } from './TodayMomentSuccessContent';

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
