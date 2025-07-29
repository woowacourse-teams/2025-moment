import { useSendMoments } from '@/features/moment/hook/useSendMoments';
import { TodayMomentWriteContent } from '@/features/moment/ui/TodayMomentWriteContent';
import { Card } from '@/shared/ui';
import { useCheckMomentsQuery } from '../hook/useCheckMomentsQuery';
import { TodayMomentSuccessContent } from './TodayMomentSuccessContent';

export function TodayMomentForm() {
  const { handleContentChange, handleSendContent, content } = useSendMoments();
  const { data: checkMomentsData } = useCheckMomentsQuery();

  const MomentResult = checkMomentsData?.data?.status;

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
