import { useSendMoments } from '@/features/moment/hook/useSendMoments';
import { TodayMomentWriteContent } from '@/features/moment/ui/TodayMomentWriteContent';
import { Card } from '@/shared/ui';
import { useCheckMomentsQuery } from '../hook/useCheckMomentsQuery';
import { TodayMomentAllowedContent } from './TodayMomentAllowedContent';

export function TodayMomentForm() {
  const { handleContentChange, handleSendContent, content } = useSendMoments();
  const { data: checkMomentsData } = useCheckMomentsQuery();

  return (
    <Card width="medium">
      {checkMomentsData?.data?.status === 'ALLOWED' ? (
        <TodayMomentWriteContent
          handleContentChange={handleContentChange}
          handleSendContent={handleSendContent}
          content={content}
        />
      ) : (
        <TodayMomentAllowedContent />
      )}
    </Card>
  );
}
