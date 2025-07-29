import { useSendMoments } from '@/features/moment/hook/useSendMoments';
import { TodayMomentWriteContent } from '@/features/moment/ui/TodayMomentWriteContent';
import { Card } from '@/shared/ui';

import { useCheckMomentsQuery } from '../hook/useCheckMomentsQuery';
import { TodayMomentSuccessContent } from './TodayMomentSuccessContent';

export function TodayMomentForm() {
  const { handleContentChange, handleSendContent, content } = useSendMoments();
  const { data: checkMomentsData } = useCheckMomentsQuery();

  const isAllowed = checkMomentsData?.data?.status === 'ALLOWED';

  return (
    <Card width="medium">
      {isAllowed ? (
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
