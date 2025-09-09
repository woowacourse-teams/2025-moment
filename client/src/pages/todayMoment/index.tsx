import { ROUTES } from '@/app/routes/routes';
import { useMomentWritingStatusQuery } from '@/features/moment/hook/useMomentWritingStatusQuery';
import { useSendMoments } from '@/features/moment/hook/useSendMoments';
import { TodayMomentForm } from '@/features/moment/ui/TodayMomentForm';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import { useEffect } from 'react';
import { useNavigate } from 'react-router';
import * as S from './index.styles';

export default function TodayMomentPage() {
  const { handleContentChange, handleImageChange, handleSendContent, content } = useSendMoments();
  const { data: momentWritingStatusData } = useMomentWritingStatusQuery();
  const momentBasicWritable = momentWritingStatusData?.data?.status;
  const navigate = useNavigate();

  useEffect(() => {
    if (momentBasicWritable === 'DENIED') {
      navigate(ROUTES.TODAY_MOMENT_SUCCESS, { replace: true });
    }
  }, [momentBasicWritable]);

  return (
    <S.TodayPageWrapper>
      <TitleContainer
        title="오늘의 모멘트"
        subtitle="하루에 한 번, 당신의 특별한 모멘트를 공유해보세요"
      />
      <TodayMomentForm
        handleContentChange={handleContentChange}
        handleImageChange={handleImageChange}
        handleSendContent={handleSendContent}
        content={content}
      />
    </S.TodayPageWrapper>
  );
}
