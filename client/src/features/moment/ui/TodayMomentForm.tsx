import { useSendMoments } from '@/features/moment/hook/useSendMoments';
import { TodayMomentWriteContent } from '@/features/moment/ui/TodayMomentWriteContent';
import { Card } from '@/shared/ui';
import { useMomentWritingStatusQuery } from '../hook/useMomentWritingStatusQuery';
import { TodayMomentSuccessContent } from './TodayMomentSuccessContent';

export function TodayMomentForm() {
  const { handleContentChange, handleSendContent, content } = useSendMoments();
  const { data: momentWritingStatusData } = useMomentWritingStatusQuery();

  const MomentResult = momentWritingStatusData?.data?.status;

  const MomentResultRender = () => {
    switch (MomentResult) {
      case 'ALLOWED':
        return (
          <TodayMomentWriteContent
            handleContentChange={handleContentChange}
            handleSendContent={handleSendContent}
            content={content}
          />
        );
      case 'DENIED':
        return <TodayMomentSuccessContent />;
      default:
        return (
          <TodayMomentWriteContent
            handleContentChange={handleContentChange}
            handleSendContent={handleSendContent}
            content={content}
          />
        );
    }
  };

  return <Card width="medium">{MomentResultRender()}</Card>;
}
